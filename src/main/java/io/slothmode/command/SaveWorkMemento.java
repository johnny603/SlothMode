package io.slothmode.command;

import java.util.List;

/**
 * MEMENTO payload: the list of open documents at the moment of the snapshot.
 *
 * A record because it's immutable and we want value equality in tests.
 */
public record SaveWorkMemento(List<String> openDocuments) implements Memento {

    public SaveWorkMemento {
        openDocuments = List.copyOf(openDocuments);
    }
}
