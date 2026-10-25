package io.slothmode.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class HybridSourceTest {

    @Test
    void mergesCalendarAndManual(@TempDir Path tmp) {
        CalendarSource cal = new CalendarSource(tmp.resolve("hy.ics"));
        ManualSource manual = new ManualSource();
        HybridSource hybrid = new HybridSource(cal, manual);

        Break a = new Break(LocalDateTime.of(2026, 10, 24, 10, 0),
                LocalDateTime.of(2026, 10, 24, 10, 10), Break.Source.CALENDAR);
        Break b = new Break(LocalDateTime.of(2026, 10, 24, 9, 0),
                LocalDateTime.of(2026, 10, 24, 9, 10), Break.Source.MANUAL);
        cal.addBreak(a);
        manual.addBreak(b);

        assertThat(hybrid.listBreaks()).hasSize(2);
        // Sorted by start
        assertThat(hybrid.listBreaks().get(0).start()).isEqualTo(b.start());
        assertThat(hybrid.listBreaks().get(1).start()).isEqualTo(a.start());
    }
}
