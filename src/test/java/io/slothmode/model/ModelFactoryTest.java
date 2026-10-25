package io.slothmode.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ModelFactoryTest {
    @Test
    void mockFactoryReturnsMockModel() {
        ModelFactory factory = () -> new MockModel("test-model");
        SlothModel model = factory.createModel();

        assertThat(model).isInstanceOf(MockModel.class);
        assertThat(model.getName()).isEqualTo("test-model");
        assertThat(model.generate("Hello")).isEqualTo("This is a mock response from the SlothModel.");
    }

    @Test
    void agentWrapsModelPrompt() {
        MockModel mock = new MockModel("test-model");
        ModelFactory factory = () -> mock;

        SlothModel agent = factory.createAgent("You are a helpful sloth.");

        // We can't easily check the internal prompt of the agent without making the agent a class,
        // but we can verify it returns the model's response.
        assertThat(agent.getName()).contains("(Agent)");
        assertThat(agent.generate("Hello")).isEqualTo("This is a mock response from the SlothModel.");
    }
}
