package io.slothmode.reward;

/**
 * DECORATOR: Adds a runtime visual effect to any RewardComponent.
 */
public final class RewardDecorator implements RewardComponent {
    private final RewardComponent wrapped;

    public RewardDecorator(RewardComponent wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String name() {
        return wrapped.name() + " (glowing)";
    }

    @Override
    public void render() {
        wrapped.render();
        System.out.println("[Decorator] A soft glow surrounds " + wrapped.name());
    }
}
