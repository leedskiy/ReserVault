package io.leedsk1y.reservault_backend.services;

import org.apache.tika.Tika;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);
    private Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    private void validateFile(Object file) throws IOException {
        logger.debug("Validating file size and format");
        long maxSize = 1_500_000; // 1.5mb

        long fileSize = (file instanceof MultipartFile) ? ((MultipartFile) file).getSize() : ((File) file).length();
        if (fileSize > maxSize) {
            throw new IllegalArgumentException("File size exceeds the 1.5 MB limit.");
        }

        Tika tika = new Tika();
        String mimeType = (file instanceof MultipartFile)
                ? tika.detect(((MultipartFile) file).getInputStream())
                : tika.detect((File) file);

        if (!"image/png".equals(mimeType) && !"image/jpeg".equals(mimeType)) {
            throw new IllegalArgumentException("Invalid file format. Only PNG and JPG are allowed.");
        }
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        logger.info("Uploading image from MultipartFile to folder: {}", folder);
        validateFile(file);
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", folder));
        return uploadResult.get("secure_url").toString();
    }

    public String uploadImage(File file, String folder) throws IOException {
        logger.info("Uploading image from File to folder: {}", folder);
        validateFile(file);
        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", folder));
        return uploadResult.get("secure_url").toString();
    }

    public void deleteImage(String imageUrl, String folder) {
        logger.info("Deleting image from Cloudinary with URL: {} in folder: {}", imageUrl, folder);
        try {
            String publicId = extractPublicId(imageUrl, folder);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image: " + e.getMessage());
        }
    }

    private String extractPublicId(String imageUrl, String folder) {
        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String publicId = fileName.split("\\.")[0];
            return folder + "/" + publicId;
        } catch (Exception e) {
            throw new RuntimeException("Invalid image URL format: " + imageUrl);
        }
    }
}
