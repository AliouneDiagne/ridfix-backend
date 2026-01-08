package it.ridfix.backend.external.mailgun;

import it.ridfix.backend.exceptions.ApiExceptions;
import org.springframework.stereotype.Service;

@Service
public class MailgunService {

    private final MailgunClient client;
    private final MailgunProperties props;

    public MailgunService(MailgunClient client, MailgunProperties props) {
        this.client = client;
        this.props = props;
    }

    /**
     * Sends an email. If Mailgun is disabled or misconfigured, throws a controlled exception
     * (caller may choose to swallow it to make the system fail-soft).
     */
    public void send(String to, String subject, String text) {
        if (!props.enabled()) {
            throw new ApiExceptions.ExternalService("Mailgun is disabled (set MAILGUN_ENABLED=true)");
        }
        if (isBlank(props.apiKey()) || isBlank(props.domain()) || isBlank(props.from())) {
            throw new ApiExceptions.ExternalService("Mailgun is not configured (set MAILGUN_API_KEY/DOMAIN/FROM)");
        }
        client.sendMessage(props.domain(), props.from(), to, subject, text);
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
