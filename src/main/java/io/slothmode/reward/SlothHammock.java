package io.slothmode.reward;

/**
 * Leaf reward: a tiny hammock.
 */
public class SlothHammock implements RewardComponent {
    @Override
    public String name() {
        return "Tiny Sloth Hammock";
    }

    @Override
    public void render() {
        System.out.println("[Render] Drawing tiny hammock under sloth...");
    }
}
