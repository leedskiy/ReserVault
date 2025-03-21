package io.leedsk1y.reservault_backend.controllers;

import io.leedsk1y.reservault_backend.services.ImageProxyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/proxy")
public class ImageProxyController {
    private final ImageProxyService imageService;

    public ImageProxyController(ImageProxyService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/image")
    public ResponseEntity<byte[]> fetchGoogleImage(@RequestParam String url) {
        return imageService.fetchImage(url);
    }
}