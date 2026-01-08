package it.ridfix.backend.external.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${ridfix.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${ridfix.cloudinary.api-key:}")
    private String apiKey;

    @Value("${ridfix.cloudinary.api-secret:}")
    private String apiSecret;

    @PostConstruct
    void validate() {
        // Cloudinary is optional at runtime (app can still start). We validate in service call instead.
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
}
