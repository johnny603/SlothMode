package io.slothmode.model;

import io.slothmode.config.ConfigManager;

/**
 * Production factory that creates an Ollama model based on config.
 */
public class OllamaModelFactory implements ModelFactory {
    @Override
    public SlothModel createModel() {
        String modelName = ConfigManager.getInstance().loadConfig().modelName();
        return new OllamaModel(modelName);
    }
}

/**
 * Concrete implementation of a model using Ollama.
 * Note: In a real implementation, this would use ollama4j or a similar client.
 * For the scaffold, we implement the logic as a placeholder.
 */
class OllamaModel implements SlothModel {
    private final String modelName;

    public OllamaModel(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public String generate(String prompt) throws ModelException {
        // TODO: Implement real Ollama API call via HTTP/ollama4j
        return "[Ollama " + modelName + "] Generated response to: " + prompt;
    }

    @Override
    public String getName() {
        return modelName;
    }
}
