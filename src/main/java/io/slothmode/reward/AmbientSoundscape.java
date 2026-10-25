package io.slothmode.reward;

/**
 * Leaf reward: a calming soundscape.
 */
public class AmbientSoundscape implements RewardComponent {
    @Override
    public String name() {
        return "Calming Rainforest Soundscape";
    }

    @Override
    public void render() {
        System.out.println("[Render] Playing calming rainforest sounds...");
    }
}
