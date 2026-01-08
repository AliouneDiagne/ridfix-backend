package it.ridfix.backend.external.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import it.ridfix.backend.exceptions.ApiExceptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${ridfix.cloudinary.folder:ridfix}")
    private String folder;

    @Value("${ridfix.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${ridfix.cloudinary.api-key:}")
    private String apiKey;

    @Value("${ridfix.cloudinary.api-secret:}")
    private String apiSecret;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) {
        validateConfigured();
        validateImage(file);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"
                    ));
            Object url = result.get("secure_url");
            if (url == null) url = result.get("url");
            if (url == null) throw new ApiExceptions.ExternalService("Cloudinary did not return a URL");
            return url.toString();
        } catch (Exception ex) {
            throw new ApiExceptions.ExternalService("Cloudinary upload failed: " + ex.getMessage());
        }
    }

    private void validateConfigured() {
        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            throw new ApiExceptions.BadRequest("Cloudinary is not configured (set CLOUDINARY_* env vars)");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiExceptions.BadRequest("File is required");
        }
        if (file.getSize() > 5_000_000) {
            throw new ApiExceptions.BadRequest("File too large (max 5MB)");
        }
        String ct = file.getContentType();
        if (ct == null || !(ct.equalsIgnoreCase("image/jpeg") || ct.equalsIgnoreCase("image/png") || ct.equalsIgnoreCase("image/webp"))) {
            throw new ApiExceptions.BadRequest("Only JPG/PNG/WEBP images are allowed");
        }
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
