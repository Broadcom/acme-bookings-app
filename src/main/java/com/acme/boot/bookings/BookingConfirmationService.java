package com.acme.boot.bookings;

import com.acme.boot.notification.Notification;
import com.acme.boot.util.EmailValidator;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Confirms a booking by notifying the guest. The guest email is validated first, so nothing is
 * sent for an invalid address.
 */
@Service
public class BookingConfirmationService {

    private final String notificationEndpoint;

    public BookingConfirmationService(@Value("${bookings.notification.endpoint}") String notificationEndpoint) {
        this.notificationEndpoint = notificationEndpoint;
    }

    /**
     * @return the message that was sent
     * @throws IllegalArgumentException if the guest email is not valid
     * @throws IOException if the notification cannot be delivered
     */
    public String confirm(String guestEmail, String roomName, LocalDate checkIn) throws IOException {
        if (!EmailValidator.isValid(guestEmail)) {
            throw new IllegalArgumentException("Invalid guest email: " + guestEmail);
        }
        String message = String.format("Booking confirmed for %s: %s, check-in %s",
                EmailValidator.normalize(guestEmail), roomName, checkIn);
        new Notification(notificationEndpoint, message).sendNotification();
        return message;
    }
}
