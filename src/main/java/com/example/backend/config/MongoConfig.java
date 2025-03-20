package com.example.backend.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb") // ✅ application.yml에서 설정값 읽기
public class MongoConfig {

    private String uri;
    private String database;

    @Bean
    public MongoClient mongoClient() {
        if (uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("❌ MongoDB URI가 설정되지 않았습니다. application.yml을 확인하세요.");
        }
        return MongoClients.create(new ConnectionString(uri));
    }

    @Bean
    public SimpleMongoClientDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoClient(), database);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDatabaseFactory());
    }
}
