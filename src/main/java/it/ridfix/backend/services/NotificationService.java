package it.ridfix.backend.services;

import it.ridfix.backend.entities.Order;
import it.ridfix.backend.entities.User;
import it.ridfix.backend.external.mailgun.MailgunService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final ObjectProvider<MailgunService> mailgunProvider;

    public NotificationService(ObjectProvider<MailgunService> mailgunProvider) {
        this.mailgunProvider = mailgunProvider;
    }

    public void orderCreated(User user, Order order) {
        MailgunService mailgun = mailgunProvider.getIfAvailable();
        if (mailgun == null) return;

        mailgun.send(
                user.getEmail(),
                "Order created",
                "Your order " + order.getId() + " has been created."
        );
    }

    public void orderStatusChanged(User user, Order order) {
        MailgunService mailgun = mailgunProvider.getIfAvailable();
        if (mailgun == null) return;

        mailgun.send(
                user.getEmail(),
                "Order status updated",
                "Your order " + order.getId() + " is now " + order.getStatus()
        );
    }
}
