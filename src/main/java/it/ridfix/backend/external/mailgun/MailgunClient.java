package it.ridfix.backend.external.mailgun;

import it.ridfix.backend.exceptions.ApiExceptions;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class MailgunClient {

    private final RestClient restClient;

    public MailgunClient(RestClient mailgunRestClient) {
        this.restClient = mailgunRestClient;
    }

    public void sendMessage(String domain, String from, String to, String subject, String text) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("from", from);
        form.add("to", to);
        form.add("subject", subject);
        form.add("text", text);

        try {
            restClient.post()
                    .uri("/v3/{domain}/messages", domain)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            throw new ApiExceptions.ExternalService("Mailgun request failed: " + ex.getMessage());
        }
    }
}
