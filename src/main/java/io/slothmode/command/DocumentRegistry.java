package io.slothmode.command;

import java.util.ArrayList;
import java.util.List;

/**
 * The set of "open documents" the lockout must snapshot before the break.
 *
 * Stub for M3: M4 will replace the in-memory list with real open-file discovery.
 */
public final class DocumentRegistry {

    private final List<String> openDocuments = new ArrayList<>();

    public void open(String path) {
        openDocuments.add(path);
    }

    public void close(String path) {
        openDocuments.remove(path);
    }

    public List<String> snapshot() {
        return List.copyOf(openDocuments);
    }

    public void restore(List<String> paths) {
        openDocuments.clear();
        openDocuments.addAll(paths);
    }

    public int size() {
        return openDocuments.size();
    }
}
