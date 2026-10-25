package io.slothmode.command;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandHistoryTest {

    /** A trivial command that appends to a list, for exercising the history. */
    static final class AppendCommand implements Command {
        private final List<String> target;
        private final String value;
        AppendCommand(List<String> target, String value) {
            this.target = target;
            this.value = value;
        }
        @Override public void execute()    { target.add(value); }
        @Override public void unexecute()  { target.remove(target.size() - 1); }
        @Override public String name()     { return "append(" + value + ")"; }
    }

    @Test
    void execute_thenUndo_thenRedo() {
        List<String> target = new ArrayList<>();
        CommandHistory history = new CommandHistory();

        history.execute(new AppendCommand(target, "a"));
        history.execute(new AppendCommand(target, "b"));
        assertThat(target).containsExactly("a", "b");

        history.undo();
        assertThat(target).containsExactly("a");

        history.redo();
        assertThat(target).containsExactly("a", "b");
    }

    @Test
    void undo_pastStart_throws() {
        CommandHistory history = new CommandHistory();
        assertThatThrownBy(history::undo).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void redo_pastEnd_throws() {
        CommandHistory history = new CommandHistory();
        history.execute(new AppendCommand(new ArrayList<>(), "x"));
        assertThatThrownBy(history::redo).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void newCommand_dropsRedoStack() {
        List<String> target = new ArrayList<>();
        CommandHistory history = new CommandHistory();
        history.execute(new AppendCommand(target, "a"));
        history.execute(new AppendCommand(target, "b"));
        history.undo();                       // cursor is between a and b
        history.execute(new AppendCommand(target, "c"));   // drops b, appends c
        assertThat(target).containsExactly("a", "c");
        assertThat(history.canRedo()).isFalse();
    }
}
