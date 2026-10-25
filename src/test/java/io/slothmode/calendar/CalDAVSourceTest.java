package io.slothmode.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

class CalDAVSourceTest {

    @TempDir
    Path tempDir;

    @Test
    void listBreaks_parsesCannedResponse() throws IOException {
        Path creds = tempDir.resolve("creds");
        Files.writeString(creds, "user:pass");

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            String response = "BEGIN:VCALENDAR\r\n" +
                              "VERSION:2.0\r\n" +
                              "BEGIN:VEVENT\r\n" +
                              "DTSTART:20261024T100000\r\n" +
                              "DTEND:20261024T101000\r\n" +
                              "END:VEVENT\r\n" +
                              "END:VCALENDAR\r\n";
            byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        try {
            CalDAVSource source = new CalDAVSource(
                    "http://localhost:" + server.getAddress().getPort(), creds);
            List<Break> breaks = source.listBreaks();

            assertThat(breaks).hasSize(1);
            assertThat(breaks.get(0).start().toString()).contains("2026-10-24T10:00");
        } finally {
            server.stop(0);
        }
    }
}
