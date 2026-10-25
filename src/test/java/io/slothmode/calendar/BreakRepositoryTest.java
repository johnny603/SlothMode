package io.slothmode.calendar;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class BreakRepositoryTest {

    @Test
    void cachesUntilInvalidated() {
        ManualSource src = new ManualSource();
        BreakRepository repo = new BreakRepository(src);

        assertThat(repo.breaks()).isEmpty();
        src.addBreak(new Break(LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), Break.Source.MANUAL));

        // Still cached — addBreak to the source doesn't invalidate by itself.
        assertThat(repo.breaks()).isEmpty();

        repo.invalidate();
        assertThat(repo.breaks()).hasSize(1);
    }

    @Test
    void invalidate_notifiesObservers() {
        BreakRepository repo = new BreakRepository(new ManualSource());
        AtomicInteger pings = new AtomicInteger();
        repo.attach(pings::incrementAndGet);

        repo.invalidate();
        repo.invalidate();

        assertThat(pings.get()).isEqualTo(2);
    }

    @Test
    void misbehavingObserver_doesNotStopOthers() {
        BreakRepository repo = new BreakRepository(new ManualSource());
        AtomicInteger pings = new AtomicInteger();
        repo.attach(() -> { throw new RuntimeException("boom"); });
        repo.attach(pings::incrementAndGet);

        repo.invalidate();

        assertThat(pings.get()).isEqualTo(1);
    }
}
