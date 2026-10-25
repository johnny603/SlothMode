package io.slothmode.model;

/**
 * A mock model for testing purposes.
 * Returns canned responses to avoid needing Ollama in CI.
 */
public class MockModel implements SlothModel {
    private final String name;
    private String cannedResponse = "This is a mock response from the SlothModel.";

    public MockModel(String name) {
        this.name = name;
    }

    public void setCannedResponse(String response) {
        this.cannedResponse = response;
    }

    @Override
    public String generate(String prompt) {
        return cannedResponse;
    }

    @Override
    public String getName() {
        return name;
    }
}
