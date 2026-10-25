package io.slothmode.reward;

import io.slothmode.command.Command;
import io.slothmode.session.SessionContext;

/**
 * COMMAND: Encapsulate granting a reward and its undo.
 */
public final class AwardRewardCommand implements Command {
    private final SessionContext ctx;
    private final int delta;
    private AwardMemento memento;

    public AwardRewardCommand(SessionContext ctx, int delta) {
        this.ctx = ctx;
        this.delta = delta;
    }

    @Override
    public void execute() {
        memento = new AwardMemento(ctx.points());
        ctx.addPoints(delta);
    }

    @Override
    public void unexecute() {
        if (memento == null) {
            throw new IllegalStateException("AwardRewardCommand.unexecute() before execute()");
        }
        ctx.setPoints(memento.priorPoints());
    }

    @Override
    public String name() {
        return "AwardReward(" + delta + " pts)";
    }
}
