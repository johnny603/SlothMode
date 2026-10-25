package io.slothmode.command;

/**
 * MEMENTO: an opaque snapshot of another object's internal state.
 *
 * Only the originator that produced it may interpret the payload.
 * Callers just carry it around and hand it back.
 */
public interface Memento {
    // marker interface — the payload is private to the originator
}
