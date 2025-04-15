package io.leedsk1y.reservault_backend.config.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import jakarta.annotation.PreDestroy;

@Configuration
public class DatabaseCleanupConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleanupConfig.class);

    private MongoTemplate mongoTemplate;
    private final CloudinaryCleanupConfig cloudinaryCleanupConfig;

    @Value("${reservault.cleanup.enabled:false}")
    private boolean cleanupEnabled;

    public DatabaseCleanupConfig(MongoTemplate mongoTemplate, CloudinaryCleanupConfig cloudinaryCleanupConfig) {
        this.mongoTemplate = mongoTemplate;
        this.cloudinaryCleanupConfig = cloudinaryCleanupConfig;
    }

    /**
     * Executes cleanup logic before application shutdown if `reservault.cleanup.enabled` is true.
     * - Deletes all hotel and offer images from Cloudinary.
     * - Drops all MongoDB collections in the database.
     *
     * Used for development environment to reset state.
     */
    @PreDestroy
    public void clearDatabase() {
        if (!cleanupEnabled) return;

        cloudinaryCleanupConfig.cleanupCloudinaryImages();

        for (String collectionName : mongoTemplate.getCollectionNames()) {
            mongoTemplate.getCollection(collectionName).drop();
            logger.info("Dropped collection: {}", collectionName);
        }
    }
}
