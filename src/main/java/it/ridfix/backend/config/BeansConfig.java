package it.ridfix.backend.config;

import it.ridfix.backend.external.mailgun.MailgunProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties({MailgunProperties.class})
public class BeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Mailgun RestClient (Basic Auth). If Mailgun is disabled or not configured, callers should avoid using it.
     */
    @Bean
    public RestClient mailgunRestClient(MailgunProperties props) {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(("api:" + nullToEmpty(props.apiKey())).getBytes());
        return RestClient.builder()
                .baseUrl("https://api.mailgun.net")
                .defaultHeader(HttpHeaders.AUTHORIZATION, authHeader)
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
    }

    private static String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }
}
