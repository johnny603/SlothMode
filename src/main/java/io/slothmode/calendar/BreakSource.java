package io.slothmode.calendar;

import java.util.List;

/**
 * ABSTRACT FACTORY: a source of breaks.
 *
 * Implementations can back this with a local .ics file, an in-memory
 * list, or both. The rest of the app never knows which.
 */
public interface BreakSource {

    /** All breaks currently visible from this source. */
    List<Break> listBreaks();

    /** Add a break to this source. */
    void addBreak(Break b);

    /** Remove a break from this source. */
    void removeBreak(Break b);

    /** Short label for logs. */
    String name();
}
