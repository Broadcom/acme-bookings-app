package com.acme.boot.bookings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The notification endpoint is a throw-away local HTTP server that records what it receives,
 * so the real EmailValidator and Notification classes are exercised without mocks.
 */
class BookingConfirmationServiceTest {

    private static final LocalDate CHECK_IN = LocalDate.of(2026, 12, 1);

    private HttpServer server;
    private final List<String> receivedBodies = new CopyOnWriteArrayList<>();
    private BookingConfirmationService service;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/notifications", exchange -> {
            receivedBodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        service = new BookingConfirmationService(
                "http://127.0.0.1:" + server.getAddress().getPort() + "/notifications");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsConfirmationContainingTheValidatedEmail() throws IOException {
        String message = service.confirm("guest@acme.com", "Ocean View", CHECK_IN);

        assertEquals("Booking confirmed for guest@acme.com: Ocean View, check-in 2026-12-01", message);
        assertEquals(List.of(message), receivedBodies);
    }

    @Test
    void normalizesTheEmailBeforeSendingIt() throws IOException {
        service.confirm("  Guest@ACME.com ", "Garden Suite", CHECK_IN);

        assertEquals(1, receivedBodies.size());
        assertTrue(receivedBodies.get(0).contains("guest@acme.com"));
    }

    @Test
    void rejectsInvalidEmailAndSendsNothing() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.confirm("not-an-email", "Ocean View", CHECK_IN));

        assertTrue(e.getMessage().contains("not-an-email"));
        assertTrue(receivedBodies.isEmpty());
    }

    @Test
    void rejectsNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> service.confirm(null, "Ocean View", CHECK_IN));
        assertTrue(receivedBodies.isEmpty());
    }

    @Test
    void propagatesIOExceptionWhenEndpointIsDown() {
        server.stop(0);

        assertThrows(IOException.class, () -> service.confirm("guest@acme.com", "Ocean View", CHECK_IN));
    }
}
