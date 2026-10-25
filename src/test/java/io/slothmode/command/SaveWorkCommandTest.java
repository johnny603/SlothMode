package io.slothmode.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SaveWorkCommandTest {

    @Test
    void execute_snapshotsOpenDocuments() {
        DocumentRegistry registry = new DocumentRegistry();
        registry.open("notes.md");
        registry.open("todo.txt");

        SaveWorkCommand cmd = new SaveWorkCommand(registry);
        cmd.execute();

        assertThat(registry.size()).isEqualTo(2);
    }

    @Test
    void unexecute_restoresSnapshot() {
        DocumentRegistry registry = new DocumentRegistry();
        registry.open("notes.md");
        registry.open("todo.txt");

        SaveWorkCommand cmd = new SaveWorkCommand(registry);
        cmd.execute();

        // After the lockout, the user might close or open more docs.
        registry.close("notes.md");
        registry.open("after-lockout.md");

        cmd.unexecute();

        assertThat(registry.snapshot()).containsExactly("notes.md", "todo.txt");
    }

    @Test
    void unexecute_beforeExecute_throws() {
        SaveWorkCommand cmd = new SaveWorkCommand(new DocumentRegistry());
        org.assertj.core.api.Assertions
                .assertThatThrownBy(cmd::unexecute)
                .isInstanceOf(IllegalStateException.class);
    }
}
