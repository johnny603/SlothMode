package io.slothmode.calendar;

import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE FACTORY: combines a calendar source and a manual source.
 */
public final class HybridSource implements BreakSource {

    private final BreakSource calendar;
    private final BreakSource manual;

    public HybridSource(BreakSource calendar, BreakSource manual) {
        this.calendar = calendar;
        this.manual = manual;
    }

    @Override
    public List<Break> listBreaks() {
        List<Break> all = new ArrayList<>(calendar.listBreaks());
        all.addAll(manual.listBreaks());
        all.sort((a, b) -> a.start().compareTo(b.start()));
        return all;
    }

    @Override
    public void addBreak(Break b) {
        manual.addBreak(b);
    }

    @Override
    public void removeBreak(Break b) {
        manual.removeBreak(b);
    }

    @Override
    public String name() {
        return "Hybrid(" + calendar.name() + " + " + manual.name() + ")";
    }
}
