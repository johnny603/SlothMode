package io.slothmode.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarSourceTest {

    @Test
    void emptyFile_returnsEmptyList(@TempDir Path tmp) {
        CalendarSource src = new CalendarSource(tmp.resolve("empty.ics"));
        assertThat(src.listBreaks()).isEmpty();
    }

    @Test
    void addBreak_thenReadBack(@TempDir Path tmp) {
        CalendarSource src = new CalendarSource(tmp.resolve("one.ics"));
        Break b = new Break(
                LocalDateTime.of(2026, 10, 24, 10, 0),
                LocalDateTime.of(2026, 10, 24, 10, 10),
                Break.Source.CALENDAR);
        src.addBreak(b);

        assertThat(tmp.resolve("one.ics")).exists();
        List<Break> read = src.listBreaks();
        assertThat(read).hasSize(1);
        assertThat(read.get(0).start()).isEqualTo(b.start());
        assertThat(read.get(0).end()).isEqualTo(b.end());
    }

    @Test
    void removeBreak_deletesFromFile(@TempDir Path tmp) {
        CalendarSource src = new CalendarSource(tmp.resolve("two.ics"));
        Break a = new Break(LocalDateTime.of(2026, 10, 24, 10, 0),
                LocalDateTime.of(2026, 10, 24, 10, 10), Break.Source.CALENDAR);
        Break b = new Break(LocalDateTime.of(2026, 10, 24, 11, 0),
                LocalDateTime.of(2026, 10, 24, 11, 10), Break.Source.CALENDAR);
        src.addBreak(a);
        src.addBreak(b);
        assertThat(src.listBreaks()).hasSize(2);

        src.removeBreak(a);
        assertThat(src.listBreaks()).hasSize(1);
        assertThat(src.listBreaks().get(0).start()).isEqualTo(b.start());
    }
}
