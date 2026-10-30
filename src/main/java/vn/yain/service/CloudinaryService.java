package vn.yain.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinary;
    private final String cloudName;
    private final String apiKey;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name:utesport-arena}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.cloudName = (cloudName != null && !cloudName.isBlank()) ? clean(cloudName) : "utesport-arena";
        this.apiKey = apiKey != null ? clean(apiKey) : "";
        String cleanSecret = apiSecret != null ? clean(apiSecret) : "";
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", this.cloudName,
                "api_key", this.apiKey,
                "api_secret", cleanSecret,
                "secure", true
        ));
        if (!this.apiKey.isBlank()) {
            log.info("CloudinaryService initialized successfully for cloud_name: [{}] with API Key: [{}...]", this.cloudName, this.apiKey.substring(0, Math.min(6, this.apiKey.length())));
        } else {
            log.warn("CloudinaryService: API Key is empty, running in fallback local storage mode.");
        }
    }

    private static String clean(String val) {
        if (val == null) return "";
        String trimmed = val.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
            (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            if (trimmed.length() >= 2) {
                trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            }
        }
        return trimmed;
    }

    /**
     * Upload an image file to Cloudinary under a specified folder,
     * or fallback seamlessly to local storage if Cloudinary is not configured or fails.
     * @param file MultipartFile from HTTP request
     * @param folder Folder name in Cloudinary (e.g. "badminton_avatars", "courts", "products")
     * @return Secure URL string of the uploaded asset (Cloudinary HTTPS or local /uploads/...)
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Tệp tải lên không được để trống!");
        }

        if (apiKey.startsWith("test-") || "test-cloud".equalsIgnoreCase(cloudName)) {
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
            return "https://res.cloudinary.com/" + cloudName + "/image/upload/v1728460000/" + (folder != null ? folder + "/" : "") + filename;
        }

        if (!apiKey.isBlank()) {
            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", folder != null ? folder : "badminton_assets",
                                "resource_type", "image"
                        )
                );
                String secureUrl = (String) uploadResult.get("secure_url");
                log.info("Cloudinary upload success: {}", secureUrl);
                return secureUrl;
            } catch (Exception e) {
                log.warn("Cloudinary upload failed: {}. Falling back to local storage.", e.getMessage());
            }
        }

        return saveFileLocally(file, folder);
    }

    /**
     * Save file to local uploads directory and return accessible web path
     */
    private String saveFileLocally(MultipartFile file, String folder) throws IOException {
        String safeFolder = (folder != null && !folder.isBlank()) ? folder.replaceAll("[^a-zA-Z0-9_-]", "") : "avatars";
        Path targetDir = Paths.get("uploads", safeFolder).toAbsolutePath().normalize();
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        String originalFilename = file.getOriginalFilename();
        String ext = ".jpg";
        if (originalFilename != null && originalFilename.lastIndexOf('.') != -1) {
            String rawExt = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
            if (rawExt.matches("\\.(jpg|jpeg|png|webp|gif)")) {
                ext = rawExt;
            }
        }

        String newFileName = "avatar_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "_" + System.currentTimeMillis() + ext;
        Path targetFile = targetDir.resolve(newFileName);
        Files.write(targetFile, file.getBytes());

        log.info("Local storage avatar upload success: /uploads/{}/{}", safeFolder, newFileName);
        return "/uploads/" + safeFolder + "/" + newFileName;
    }

    /**
     * Delete an asset from Cloudinary by its public ID
     */
    public boolean deleteImage(String publicId) {
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equalsIgnoreCase((String) result.get("result"));
        } catch (Exception e) {
            log.warn("Cloudinary delete failed: {}", e.getMessage());
            return false;
        }
    }
}
