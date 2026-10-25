package io.slothmode.model;

/**
 * Interface for a local LLM model.
 */
public interface SlothModel {
    String generate(String prompt) throws ModelException;
    String getName();
}
