package io.slothmode.model;

/**
 * Abstract Factory for creating local LLM models and agents.
 */
public interface ModelFactory {
    SlothModel createModel();

    // Agents can be defined as specialized wrappers around models
    // but for v0 we can keep it simple.
    default SlothModel createAgent(String systemPrompt) {
        SlothModel model = createModel();
        return new SlothModel() {
            @Override
            public String generate(String prompt) {
                return model.generate(systemPrompt + "\n\nUser: " + prompt);
            }

            @Override
            public String getName() {
                return model.getName() + " (Agent)";
            }
        };
    }
}
