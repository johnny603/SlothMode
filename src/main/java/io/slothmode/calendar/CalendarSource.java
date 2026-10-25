package io.slothmode.calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE FACTORY: reads and writes a minimal .ics file.
 *
 * M4 subset of RFC 5545: one VEVENT per break, DTSTART/DTEND as local
 * date-time, SUMMARY = "SlothMode break". Real ical4j lands in M7.
 */
public final class CalendarSource implements BreakSource {

    private static final Logger log = LoggerFactory.getLogger(CalendarSource.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private final Path icsFile;

    public CalendarSource(Path icsFile) {
        this.icsFile = icsFile;
    }

    @Override
    public List<Break> listBreaks() {
        if (!Files.exists(icsFile)) {
            return List.of();
        }
        try {
            List<Break> breaks = new ArrayList<>();
            Break current = null;
            for (String line : Files.readAllLines(icsFile)) {
                if (line.startsWith("DTSTART:")) {
                    LocalDateTime start = LocalDateTime.parse(line.substring("DTSTART:".length()), FMT);
                    current = new Break(start, start.plusMinutes(10), Break.Source.CALENDAR);
                } else if (line.startsWith("DTEND:") && current != null) {
                    LocalDateTime end = LocalDateTime.parse(line.substring("DTEND:".length()), FMT);
                    current = new Break(current.start(), end, Break.Source.CALENDAR);
                } else if (line.equals("END:VEVENT") && current != null) {
                    breaks.add(current);
                    current = null;
                }
            }
            return breaks;
        } catch (IOException | RuntimeException e) {
            log.error("Failed to read {}", icsFile, e);
            return List.of();
        }
    }

    @Override
    public void addBreak(Break b) {
        List<Break> all = new ArrayList<>(listBreaks());
        all.add(b);
        rewrite(all);
    }

    @Override
    public void removeBreak(Break b) {
        List<Break> all = new ArrayList<>(listBreaks());
        all.remove(b);
        rewrite(all);
    }

    private void rewrite(List<Break> breaks) {
        try {
            Files.createDirectories(icsFile.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//SlothMode//EN\n");
            for (Break b : breaks) {
                sb.append("BEGIN:VEVENT\n");
                sb.append("SUMMARY:SlothMode break\n");
                sb.append("DTSTART:").append(FMT.format(b.start())).append("\n");
                sb.append("DTEND:").append(FMT.format(b.end())).append("\n");
                sb.append("END:VEVENT\n");
            }
            sb.append("END:VCALENDAR\n");
            Files.writeString(icsFile, sb.toString());
            log.info("Wrote {} break(s) to {}", breaks.size(), icsFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write " + icsFile, e);
        }
    }

    @Override
    public String name() {
        return "CalendarSource(" + icsFile.getFileName() + ")";
    }

    public Path file() {
        return icsFile;
    }
}
