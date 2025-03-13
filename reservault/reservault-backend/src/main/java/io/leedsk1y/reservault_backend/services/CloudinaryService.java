package io.leedsk1y.reservault_backend.services;

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

    public String uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
            ObjectUtils.asMap("folder", "hotel_images"));

        return uploadResult.get("secure_url").toString();
    }

    public String uploadImage(File file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file,
                ObjectUtils.asMap("folder", "hotel_images"));
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
