package io.slothmode.session;

/**
 * Visitor for session events.
 */
public interface EventVisitor {
    void visit(String eventLine);
}
