package io.slothmode.lockout;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class LockoutAbstractionTest {

    @Test
    void enforce_delegatesToImplementor() {
        LockoutImplementor implementor = mock(LockoutImplementor.class);
        LockoutAbstraction abstraction = new LockoutAbstraction(implementor);

        abstraction.enforce(10);

        verify(implementor).present(10);
        verifyNoMoreInteractions(implementor);
    }

    @Test
    void release_delegatesToImplementor() {
        LockoutImplementor implementor = mock(LockoutImplementor.class);
        LockoutAbstraction abstraction = new LockoutAbstraction(implementor);

        abstraction.release();

        verify(implementor).dismiss();
        verifyNoMoreInteractions(implementor);
    }

    @Test
    void enforce_rejectsNonPositiveDuration() {
        LockoutImplementor implementor = mock(LockoutImplementor.class);
        LockoutAbstraction abstraction = new LockoutAbstraction(implementor);

        assertThatThrownBy(() -> abstraction.enforce(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> abstraction.enforce(-1))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(implementor);
    }

    @Test
    void constructor_requiresNonNullImplementor() {
        assertThatThrownBy(() -> new LockoutAbstraction(null))
                .isInstanceOf(NullPointerException.class);
    }
}
