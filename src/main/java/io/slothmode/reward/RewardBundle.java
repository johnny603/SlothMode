package io.slothmode.reward;

import java.util.ArrayList;
import java.util.List;

/**
 * COMPOSITE: A bundle of multiple rewards.
 */
public class RewardBundle implements RewardComponent {
    private final List<RewardComponent> rewards = new ArrayList<>();

    public void addReward(RewardComponent reward) {
        rewards.add(reward);
    }

    @Override
    public String name() {
        return "Sloth Reward Bundle";
    }

    @Override
    public void render() {
        for (RewardComponent reward : rewards) {
            reward.render();
        }
    }
}
