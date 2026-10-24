package io.slothmode.session;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SessionSubjectTest {

    @Test
    void attach_detach_count() {
        SessionSubject subject = new SessionSubject();
        SessionObserver obs = (o, n) -> {};
        assertThat(subject.observerCount()).isEqualTo(0);
        subject.attach(obs);
        assertThat(subject.observerCount()).isEqualTo(1);
        subject.detach(obs);
        assertThat(subject.observerCount()).isEqualTo(0);
    }

    @Test
    void notify_deliversToAllObservers() {
        SessionSubject subject = new SessionSubject();
        List<String> calls = new ArrayList<>();
        subject.attach((o, n) -> calls.add("A:" + n.name()));
        subject.attach((o, n) -> calls.add("B:" + n.name()));

        subject.notifyStateChanged(new IdleState(), new FocusState(5));

        assertThat(calls).containsExactly("A:Focus(5s)", "B:Focus(5s)");
    }

    @Test
    void misbehavingObserver_doesNotStopOthers() {
        SessionSubject subject = new SessionSubject();
        List<String> calls = new ArrayList<>();
        subject.attach((o, n) -> { throw new RuntimeException("boom"); });
        subject.attach((o, n) -> calls.add("survived"));

        subject.notifyStateChanged(new IdleState(), new FocusState(5));

        assertThat(calls).containsExactly("survived");
    }
}
