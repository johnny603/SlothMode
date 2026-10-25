package io.slothmode.model;

import java.nio.file.Files;
import java.nio.file.Path;

public final class LlamaCppModelFactory implements ModelFactory {
    private final Path modelPath;

    public LlamaCppModelFactory(Path modelPath) {
        this.modelPath = modelPath;
    }

    @Override
    public SlothModel createModel() throws ModelException {
        if (modelPath == null || !Files.exists(modelPath)) {
            throw new ModelException(String.format(
                    "llama.cpp model file not found: %s. Download tinyllama-1.1b-chat-Q4_K_M.gguf from Hugging Face and set model.path in ~/.slothmode/config.toml.",
                    modelPath == null ? "null" : modelPath));
        }
        return new LlamaCppModel(modelPath);
    }
}
