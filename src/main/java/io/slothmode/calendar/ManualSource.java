package io.slothmode.calendar;

import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE FACTORY: an in-memory source.
 *
 * Used by tests and by users who don't want a calendar file.
 */
public final class ManualSource implements BreakSource {

    private final List<Break> breaks = new ArrayList<>();

    @Override
    public List<Break> listBreaks() {
        return List.copyOf(breaks);
    }

    @Override
    public void addBreak(Break b) {
        breaks.add(b);
    }

    @Override
    public void removeBreak(Break b) {
        breaks.remove(b);
    }

    @Override
    public String name() {
        return "ManualSource";
    }
}
