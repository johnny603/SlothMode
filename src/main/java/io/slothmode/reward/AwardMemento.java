package io.slothmode.reward;

/**
 * Snapshot of points before a reward is granted.
 */
public record AwardMemento(int priorPoints) implements io.slothmode.command.Memento {}
