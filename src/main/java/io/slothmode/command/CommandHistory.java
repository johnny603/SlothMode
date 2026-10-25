package io.slothmode.command;

import java.util.ArrayList;
import java.util.List;

/**
 * COMMAND history + ITERATOR: the executed commands in order, with
 * a cursor that can walk backward (undo) and forward (redo).
 *
 * Traversal methods First/Next/IsDone/CurrentItem expose the ITERATOR pattern.
 */
public final class CommandHistory {

    private final List<Command> commands = new ArrayList<>();
    private int cursor = 0;   // next index to undo

    public void execute(Command command) {
        // Drop any redone commands past the cursor.
        while (commands.size() > cursor) {
            commands.remove(commands.size() - 1);
        }
        command.execute();
        commands.add(command);
        cursor++;
    }

    public boolean canUndo() {
        return cursor > 0;
    }

    public boolean canRedo() {
        return cursor < commands.size();
    }

    public Command undo() {
        if (!canUndo()) {
            throw new IllegalStateException("nothing to undo");
        }
        cursor--;
        Command c = commands.get(cursor);
        c.unexecute();
        return c;
    }

    public Command redo() {
        if (!canRedo()) {
            throw new IllegalStateException("nothing to redo");
        }
        Command c = commands.get(cursor);
        c.execute();
        cursor++;
        return c;
    }

    // ITERATOR traversal over the executed commands.
    public int size() { return commands.size(); }
    public Command get(int index) { return commands.get(index); }
}
