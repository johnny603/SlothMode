package io.slothmode.command;

/**
 * COMMAND: encapsulate a request as an object.
 *
 * Concrete commands know how to do the thing and how to undo it.
 */
public interface Command {

    /** Carry out the request. */
    void execute();

    /** Reverse the effect of the most recent execute(). */
    void unexecute();

    /** False if this command cannot be undone (e.g. quit). */
    default boolean isReversible() {
        return true;
    }

    /** Short label for logs. */
    String name();
}
