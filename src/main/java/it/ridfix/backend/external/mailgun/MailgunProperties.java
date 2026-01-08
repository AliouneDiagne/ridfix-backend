package it.ridfix.backend.external.mailgun;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ridfix.mailgun")
public record MailgunProperties(
        boolean enabled,
        String apiKey,
        String domain,
        String from
) {}
