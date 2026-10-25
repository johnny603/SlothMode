package io.slothmode.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PluginLoaderTest {

    @TempDir
    Path tempDir;

    private Path buildFixtureJar(Path pluginsDir, String pluginId) throws Exception {
        Files.createDirectories(pluginsDir);

        String pkg = "io.slothmode.plugin.test";
        String className = "FixturePlugin";
        String fqcn = pkg + "." + className;

        String source = "package " + pkg + ";\n"
                + "import io.slothmode.plugin.SlothPlugin;\n"
                + "import io.slothmode.calendar.BreakSource;\n"
                + "import io.slothmode.config.SlothConfig;\n"
                + "import io.slothmode.calendar.Break;\n"
                + "import java.util.List;\n"
                + "public final class " + className + " implements SlothPlugin {\n"
                + "  public String id() { return \"" + pluginId + "\"; }\n"
                + "  public BreakSource createBreakSource(SlothConfig cfg) {\n"
                + "    return new BreakSource() {\n"
                + "      public List<Break> listBreaks() { return List.of(); }\n"
                + "      public void addBreak(Break b) {}\n"
                + "      public void removeBreak(Break b) {}\n"
                + "      public String name() { return \"fixture\"; }\n"
                + "    };\n"
                + "  }\n"
                + "}\n";

        Path srcDir = tempDir.resolve("fixture-src");
        Path classesDir = tempDir.resolve("fixture-classes");
        Files.createDirectories(srcDir);
        Files.createDirectories(classesDir);

        Path sourceFile = srcDir.resolve(className + ".java");
        Files.writeString(sourceFile, source, StandardCharsets.UTF_8);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("JDK compiler available").isNotNull();

        String classpath = System.getProperty("java.class.path");
        int rc = compiler.run(null, null, null,
                "-classpath", classpath,
                "-d", classesDir.toString(),
                sourceFile.toString());
        assertThat(rc).as("fixture compilation exit code").isZero();

        Path classFile = classesDir.resolve(pkg.replace('.', '/')).resolve(className + ".class");
        assertThat(classFile).as("compiled fixture class").exists();

        Path jar = pluginsDir.resolve("fixture-plugin.jar");
        try (OutputStream fos = Files.newOutputStream(jar);
             JarOutputStream jos = new JarOutputStream(fos)) {

            // Jar every .class produced by the compiler (includes inner/anonymous classes
            // such as FixturePlugin$1).
            try (Stream<Path> classes = Files.walk(classesDir)) {
                for (Path cls : classes.filter(p -> p.toString().endsWith(".class")).toList()) {
                    String entryName = classesDir.relativize(cls).toString().replace('\\', '/');
                    jos.putNextEntry(new JarEntry(entryName));
                    Files.copy(cls, jos);
                    jos.closeEntry();
                }
            }

            jos.putNextEntry(new JarEntry("META-INF/services/io.slothmode.plugin.SlothPlugin"));
            jos.write((fqcn + "\n").getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
        return jar;
    }

    @Test
    void loadsPluginFromJar() throws Exception {
        Path pluginsDir = tempDir.resolve("plugins");
        buildFixtureJar(pluginsDir, "fixture");

        List<SlothPlugin> plugins = PluginLoader.load(pluginsDir);

        assertThat(plugins).hasSize(1);
        assertThat(plugins.get(0).id()).isEqualTo("fixture");
        assertThat(plugins.get(0).createBreakSource(null)).isNotNull();
    }

    @Test
    void skipsCorruptJar() throws Exception {
        Path pluginsDir = tempDir.resolve("plugins");
        buildFixtureJar(pluginsDir, "fixture");
        Files.write(pluginsDir.resolve("broken.jar"), new byte[]{1, 2, 3, 4});

        List<SlothPlugin> plugins = PluginLoader.load(pluginsDir);

        assertThat(plugins).extracting(SlothPlugin::id).contains("fixture");
    }

    @Test
    void emptyDirReturnsEmptyList() throws Exception {
        Path pluginsDir = tempDir.resolve("empty-plugins");
        Files.createDirectories(pluginsDir);

        assertThat(PluginLoader.load(pluginsDir)).isEmpty();
    }

    @Test
    void missingDirReturnsEmptyList() {
        Path missing = Path.of("/nonexistent-plugins-" + UUID.randomUUID());
        assertThat(PluginLoader.load(missing)).isEmpty();
    }

    @Test
    void nullDirReturnsEmptyList() {
        assertThat(PluginLoader.load(null)).isEmpty();
    }
}
