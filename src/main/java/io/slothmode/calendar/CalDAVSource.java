package io.slothmode.calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Base64;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;

public final class CalDAVSource implements BreakSource {

    private static final Logger log = LoggerFactory.getLogger(CalDAVSource.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private final String url;
    private final Path credentialsPath;
    private final HttpClient client;

    public CalDAVSource(String url, Path credentialsPath) {
        this.url = url;
        this.credentialsPath = credentialsPath;
        this.client = HttpClient.newBuilder().build();
    }

    private String getAuthHeader() throws IOException {
        String creds = Files.readString(credentialsPath).trim();
        int split = creds.indexOf(':');
        if (split == -1) throw new IOException("Invalid credentials format in " + credentialsPath);
        String auth = creds.substring(0, split) + ":" + creds.substring(split + 1);
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public List<Break> listBreaks() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", getAuthHeader())
                    .header("Depth", "1")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("CalDAV list failed with status {}", response.statusCode());
                return List.of();
            }

            String body = response.body();
            Calendar cal = new CalendarBuilder().build(new StringReader(body));
            List<Break> breaks = new ArrayList<>();
            for (Component comp : cal.getComponents()) {
                if (comp instanceof VEvent event) {
                    var startOpt = event.getStartDate();
                    var endOpt = event.getEndDate();
                    if (startOpt.isEmpty() || endOpt.isEmpty()) {
                        continue;
                    }
                    LocalDateTime start = toLocalDateTime(startOpt.get().getDate());
                    LocalDateTime end = toLocalDateTime(endOpt.get().getDate());
                    if (start == null || end == null) {
                        log.warn("Skipping event with unsupported date type");
                        continue;
                    }
                    breaks.add(new Break(start, end, Break.Source.CALENDAR));
                }
            }
            return breaks;
        } catch (Exception e) {
            log.error("CalDAV list failed", e);
            return List.of();
        }
    }

    private static LocalDateTime toLocalDateTime(Object temporal) {
        if (temporal instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (temporal instanceof ZonedDateTime zdt) {
            return zdt.toLocalDateTime();
        }
        if (temporal instanceof java.time.LocalDate ld) {
            return ld.atStartOfDay();
        }
        return null;
    }

    @Override
    public void addBreak(Break b) {
        try {
            String ics = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//SlothMode//EN\n" +
                          "BEGIN:VEVENT\n" +
                          "SUMMARY:SlothMode break\n" +
                          "DTSTART:" + FMT.format(b.start()) + "\n" +
                          "DTEND:" + FMT.format(b.end()) + "\n" +
                          "END:VEVENT\n" +
                          "END:VCALENDAR\n";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/event_" + System.currentTimeMillis()))
                    .header("Authorization", getAuthHeader())
                    .header("Content-Type", "text/calendar; charset=utf-8")
                    .PUT(HttpRequest.BodyPublishers.ofString(ics))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 201 && response.statusCode() != 204) {
                log.error("CalDAV add failed with status {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("CalDAV add failed", e);
        }
    }

    @Override
    public void removeBreak(Break b) {
        log.warn("CalDAV removeBreak not fully implemented: requires event URI");
    }

    @Override
    public String name() {
        return "CalDAVSource(" + url + ")";
    }
}
