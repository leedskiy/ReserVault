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

    /**
     * Validates the uploaded file's size and format.
     * Only PNG and JPG files under 1.5MB are allowed.
     * @param file The file to validate (MultipartFile or File).
     * @throws IOException If the file cannot be read or is invalid.
     * @throws IllegalArgumentException If the file exceeds size limit or has an unsupported format.
     */
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

    /**
     * Uploads an image from a MultipartFile to a specific Cloudinary folder.
     * @param file The image file to upload.
     * @param folder The destination folder in Cloudinary.
     * @return The secure URL of the uploaded image.
     * @throws IOException If upload or validation fails.
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        logger.info("Uploading image from MultipartFile to folder: {}", folder);
        validateFile(file);
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", folder));
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Uploads an image from a File to a specific Cloudinary folder.
     * @param file The image file to upload.
     * @param folder The destination folder in Cloudinary.
     * @return The secure URL of the uploaded image.
     * @throws IOException If upload or validation fails.
     */
    public String uploadImage(File file, String folder) throws IOException {
        logger.info("Uploading image from File to folder: {}", folder);
        validateFile(file);
        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", folder));
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Deletes an image from Cloudinary based on its URL and folder.
     * @param imageUrl The full URL of the image.
     * @param folder The folder in which the image was uploaded.
     * @throws RuntimeException If deletion fails.
     */
    public void deleteImage(String imageUrl, String folder) {
        logger.info("Deleting image from Cloudinary with URL: {} in folder: {}", imageUrl, folder);
        try {
            String publicId = extractPublicId(imageUrl, folder);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image: " + e.getMessage());
        }
    }

    /**
     * Extracts the public ID of the image from its URL for deletion purposes.
     * @param imageUrl The full Cloudinary URL of the image.
     * @param folder The folder where the image is stored.
     * @return The public ID used by Cloudinary to identify the image.
     * @throws RuntimeException If the URL format is invalid.
     */
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
