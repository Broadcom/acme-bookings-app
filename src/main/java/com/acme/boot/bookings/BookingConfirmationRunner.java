package com.acme.boot.bookings;

import java.io.IOException;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Demo entry point: on startup, confirms a booking for the email given as the first argument
 * (or a sample guest) through {@link BookingConfirmationService}.
 */
@Component
public class BookingConfirmationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BookingConfirmationRunner.class);

    private final BookingConfirmationService confirmationService;

    public BookingConfirmationRunner(BookingConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

    @Override
    public void run(String... args) {
        String guestEmail = args.length > 0 ? args[0] : "Demo.Guest@Acme.com";
        try {
            String message = confirmationService.confirm(guestEmail, "Ocean View", LocalDate.now().plusDays(30));
            log.info("Notification sent: {}", message);
        } catch (IllegalArgumentException e) {
            log.warn("Booking not confirmed: {}", e.getMessage());
        } catch (IOException e) {
            log.warn("Booking confirmed but the notification could not be delivered: {}", e.toString());
        }
    }
}
