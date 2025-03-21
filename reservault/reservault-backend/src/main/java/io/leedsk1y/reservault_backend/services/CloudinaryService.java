package io.leedsk1y.reservault_backend.services;
import org.apache.tika.Tika;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    private Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    private void validateFile(Object file) throws IOException {
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

    public String uploadImage(MultipartFile file) throws IOException {
        validateFile(file);

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "hotel_images"));
        return uploadResult.get("secure_url").toString();
    }


    public String uploadImage(File file) throws IOException {
        validateFile(file);

        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", "hotel_images"));
        return uploadResult.get("secure_url").toString();
    }


    public void deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image: " + e.getMessage());
        }
    }

    private String extractPublicId(String imageUrl) {
        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String publicId = fileName.split("\\.")[0];

            return "hotel_images/" + publicId;
        } catch (Exception e) {
            throw new RuntimeException("Invalid image URL format: " + imageUrl);
        }
    }

}
