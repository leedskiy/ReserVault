package io.leedsk1y.reservault_backend.config.cleanup;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import jakarta.annotation.PreDestroy;

@Configuration
public class DatabaseCleanupConfig {
    private MongoTemplate mongoTemplate;
    private final CloudinaryCleanupConfig cloudinaryCleanupConfig;

    public DatabaseCleanupConfig(MongoTemplate mongoTemplate, CloudinaryCleanupConfig cloudinaryCleanupConfig) {
        this.mongoTemplate = mongoTemplate;
        this.cloudinaryCleanupConfig = cloudinaryCleanupConfig;
    }

    @PreDestroy
    public void clearDatabase() {
        cloudinaryCleanupConfig.cleanupCloudinaryImages();

        for (String collectionName : mongoTemplate.getCollectionNames()) {
            mongoTemplate.getCollection(collectionName).drop();
        }
    }
}
