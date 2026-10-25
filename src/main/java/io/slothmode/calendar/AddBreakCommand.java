package io.slothmode.calendar;

import io.slothmode.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * COMMAND + MEMENTO: add a break to the calendar, undoably.
 *
 * Execute snapshots the current .ics, writes the break, then notifies
 * the repository. Unexecute restores the pre-command snapshot.
 */
public final class AddBreakCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(AddBreakCommand.class);

    private final CalendarSource calendar;
    private final BreakRepository repository;
    private final Break toAdd;

    private IcsMemento snapshot;

    public AddBreakCommand(CalendarSource calendar, BreakRepository repository, Break toAdd) {
        this.calendar = calendar;
        this.repository = repository;
        this.toAdd = toAdd;
    }

    @Override
    public void execute() {
        this.snapshot = new IcsMemento(calendar.listBreaks());
        calendar.addBreak(toAdd);
        log.info("Added break {} -> {} ({})", toAdd.start(), toAdd.end(), toAdd.source());
        repository.invalidate();
    }

    @Override
    public void unexecute() {
        if (snapshot == null) {
            throw new IllegalStateException("AddBreakCommand.unexecute() before execute()");
        }
        // Restore by rewriting the file from the snapshot.
        List<Break> current = calendar.listBreaks();
        for (Break b : current) {
            calendar.removeBreak(b);
        }
        for (Break b : snapshot.breaks()) {
            calendar.addBreak(b);
        }
        log.info("Removed break {} (restored {} pre-command break(s))",
                toAdd.start(), snapshot.breaks().size());
        repository.invalidate();
    }

    @Override
    public String name() {
        return "AddBreak(" + toAdd.start() + ")";
    }
}
