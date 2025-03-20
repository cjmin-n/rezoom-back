package com.example.backend.config;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfig {

    private final Dotenv dotenv = Dotenv.load(); // ✅ `.env` 파일 로드


    @Bean
    public MongoClient mongoClient() {
        String uri = dotenv.get("MONGO_DB_URI"); // ✅ `.env`에서 MongoDB URI 가져오기

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build();

        return MongoClients.create(settings); // ✅ 최신 방식
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoClient(), dotenv.get("MONGO_DB_NAME"));
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDatabaseFactory()); // ✅ 최신 방식 적용
    }
}
