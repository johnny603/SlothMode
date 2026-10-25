package io.slothmode.session;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates events into a prompt for the local model.
 */
public class SummaryVisitor implements EventVisitor {
    private final List<String> accumulated = new ArrayList<>();

    @Override
    public void visit(String eventLine) {
        accumulated.add(eventLine);
    }

    public String prompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are SlothMode's morning digest. Summarize the last\n");
        sb.append("session in exactly two calm sentences. Do not use bullet\n");
        sb.append("points. Do not use emojis.\n\n");
        sb.append("Session events:\n");
        for (String line : accumulated) {
            sb.append(line).append("\n");
        }
        sb.append("\nWrite the two-line summary now.");
        return sb.toString();
    }
}
