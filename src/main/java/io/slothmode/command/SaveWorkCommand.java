package io.slothmode.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * COMMAND + MEMENTO: snapshot the open documents before a lockout,
 * so undo can restore the exact set.
 */
public final class SaveWorkCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(SaveWorkCommand.class);

    private final DocumentRegistry registry;
    private SaveWorkMemento snapshot;

    public SaveWorkCommand(DocumentRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void execute() {
        this.snapshot = new SaveWorkMemento(registry.snapshot());
        log.info("Saved work: {} open document(s) snapshotted", snapshot.openDocuments().size());
    }

    @Override
    public void unexecute() {
        if (snapshot == null) {
            throw new IllegalStateException("SaveWorkCommand.unexecute() before execute()");
        }
        registry.restore(snapshot.openDocuments());
        log.info("Restored {} open document(s)", snapshot.openDocuments().size());
    }

    @Override
    public String name() {
        return "SaveWork(" + registry.size() + " docs)";
    }
}
