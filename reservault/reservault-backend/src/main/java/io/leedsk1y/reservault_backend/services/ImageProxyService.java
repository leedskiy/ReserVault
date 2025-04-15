package io.leedsk1y.reservault_backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class ImageProxyService {
    private static final Logger logger = LoggerFactory.getLogger(ImageProxyService.class);

    /**
     * Fetches an image from a given external URL and returns it as a byte array.
     * Sets a user-agent header to mimic a browser request.
     * @param url The URL of the external image.
     * @return ResponseEntity containing the image data or an error response.
     */
    public ResponseEntity<byte[]> fetchImage(String url) {
        logger.info("Fetching image from external URL: {}", url);
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    new URI(url), HttpMethod.GET, entity, byte[].class);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}