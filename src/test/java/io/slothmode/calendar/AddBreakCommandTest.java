package io.slothmode.calendar;

import io.slothmode.command.CommandHistory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AddBreakCommandTest {

    @Test
    void execute_addsBreak_andUnexecute_restores(@TempDir Path tmp) throws Exception {
        CalendarSource calendar = new CalendarSource(tmp.resolve("test.ics"));
        BreakRepository repo = new BreakRepository(calendar);
        Break b = new Break(
                LocalDateTime.of(2026, 10, 24, 10, 0),
                LocalDateTime.of(2026, 10, 24, 10, 10),
                Break.Source.MANUAL);

        CommandHistory history = new CommandHistory();
        history.execute(new AddBreakCommand(calendar, repo, b));

        assertThat(calendar.listBreaks()).hasSize(1);
        assertThat(repo.breaks()).hasSize(1);
        assertThat(tmp.resolve("test.ics")).exists();

        history.undo();

        assertThat(calendar.listBreaks()).isEmpty();
        assertThat(repo.breaks()).isEmpty();
    }

    @Test
    void unexecute_beforeExecute_throws(@TempDir Path tmp) {
        CalendarSource calendar = new CalendarSource(tmp.resolve("x.ics"));
        BreakRepository repo = new BreakRepository(calendar);
        AddBreakCommand cmd = new AddBreakCommand(calendar, repo,
                new Break(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10), Break.Source.MANUAL));

        org.assertj.core.api.Assertions
                .assertThatThrownBy(cmd::unexecute)
                .isInstanceOf(IllegalStateException.class);
    }
}
