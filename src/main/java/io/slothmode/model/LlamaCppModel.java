package io.slothmode.model;

import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.LlamaOutput;
import de.kherud.llama.ModelParameters;
import java.nio.file.Path;

public final class LlamaCppModel implements SlothModel {
    private final LlamaModel llama;

    public LlamaCppModel(Path modelPath) {
        try {
            ModelParameters params = new ModelParameters()
                    .setModel(modelPath.toString());
            this.llama = new LlamaModel(params);
        } catch (Exception e) {

            throw new RuntimeException("Failed to initialize llama.cpp native model", e);
        }
    }

    @Override
    public String generate(String prompt) throws ModelException {
        try {
            StringBuilder sb = new StringBuilder();
            for (LlamaOutput output : llama.generate(new InferenceParameters(prompt))) {
                sb.append(output);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new ModelException("Native llama.cpp generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "llama.cpp";
    }
}
