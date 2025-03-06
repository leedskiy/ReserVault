package io.leedsk1y.reservault_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import jakarta.annotation.PreDestroy;

@Configuration
public class DatabaseCleanupConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PreDestroy
    public void clearDatabase() {
        for (String collectionName : mongoTemplate.getCollectionNames()) {
            mongoTemplate.getCollection(collectionName).drop();
        }
    }
}
