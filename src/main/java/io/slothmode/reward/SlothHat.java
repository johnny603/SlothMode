package io.slothmode.reward;

/**
 * Leaf reward: a cozy hat for the sloth.
 */
public class SlothHat implements RewardComponent {
    @Override
    public String name() {
        return "Cozy Sloth Hat";
    }

    @Override
    public void render() {
        System.out.println("[Render] Drawing cozy hat on sloth...");
    }
}
