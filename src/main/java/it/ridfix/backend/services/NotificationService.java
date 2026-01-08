package it.ridfix.backend.services;

import it.ridfix.backend.entities.Order;
import it.ridfix.backend.entities.User;
import it.ridfix.backend.external.mailgun.MailgunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final MailgunService mailgun;

    public NotificationService(MailgunService mailgun) {
        this.mailgun = mailgun;
    }

    public void orderCreated(User user, Order order) {
        String subject = "Ridfix - Order created " + order.getId();
        String text = "Hi " + user.getName() + ",\n\nYour order has been created. Status: " + order.getStatus() + "\nOrder ID: " + order.getId() + "\n\nRidfix Team";
        failSoftSend(user.getEmail(), subject, text);
    }

    public void orderStatusChanged(User user, Order order) {
        String subject = "Ridfix - Order status updated " + order.getId();
        String text = "Hi " + user.getName() + ",\n\nYour order status is now: " + order.getStatus() + "\nOrder ID: " + order.getId() + "\n\nRidfix Team";
        failSoftSend(user.getEmail(), subject, text);
    }

    private void failSoftSend(String to, String subject, String text) {
        try {
            mailgun.send(to, subject, text);
        } catch (Exception ex) {
            log.warn("Mailgun send failed (fail-soft): {}", ex.getMessage());
        }
    }
}
