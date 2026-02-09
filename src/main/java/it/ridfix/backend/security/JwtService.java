package it.ridfix.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Clock clock;

    @Value("${ridfix.jwt.secret}")
    private String secret;

    @Value("${ridfix.jwt.issuer}")
    private String issuer;

    @Value("${ridfix.jwt.expiration-minutes}")
    private long expirationMinutes;

    private SecretKey key;

    public JwtService(Clock clock) {
        this.clock = clock;
    }

    @PostConstruct
    void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "RIDFIX_JWT_SECRET is missing or too short (min 32 chars)."
            );
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String subject, Map<String, Object> extraClaims) {
        Instant now = Instant.now(clock);
        Instant exp = now.plusSeconds(expirationMinutes * 60);

        var builder = Jwts.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp));

        if (extraClaims != null && !extraClaims.isEmpty()) {
            builder.addClaims(extraClaims);
        }

        return builder
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .requireIssuer(issuer)
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims c = parseClaims(token);
            Date exp = c.getExpiration();
            return exp != null && exp.after(Date.from(Instant.now(clock)));
        } catch (Exception ex) {
            return false;
        }
    }
}
