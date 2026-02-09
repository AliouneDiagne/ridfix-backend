package it.ridfix.backend.external.mailgun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Optional email provider based on Mailgun.
 * This service is enabled only when ridfix.mailgun.enabled=true.
 * Any failure or misconfiguration will NOT block the application flow.
 */
@Service
@ConditionalOnProperty(prefix = "ridfix.mailgun", name = "enabled", havingValue = "true")
public class MailgunService {

    private static final Logger log = LoggerFactory.getLogger(MailgunService.class);

    private final MailgunClient client;
    private final MailgunProperties props;

    public MailgunService(MailgunClient client, MailgunProperties props) {
        this.client = client;
        this.props = props;
    }

    public void send(String to, String subject, String text) {

        if (!isConfigured()) {
            log.warn(
                    "Mailgun enabled but not fully configured (api-key/domain/from missing). Email skipped."
            );
            return;
        }

        try {
            client.sendMessage(
                    props.domain(),
                    props.from(),
                    to,
                    subject,
                    text
            );
        } catch (Exception ex) {
            log.warn(
                    "Mailgun send failed. Email skipped. Cause: {}",
                    ex.getMessage()
            );
        }
    }

    private boolean isConfigured() {
        return !isBlank(props.domain())
                && !isBlank(props.from())
                && !isBlank(props.apiKey());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
