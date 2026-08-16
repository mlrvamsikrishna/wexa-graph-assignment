package com.wexa.graph.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jDriverConfig {

    @Bean(destroyMethod = "close")
    public Driver neo4jDriver() {
        String uri = requiredEnv("COGNODB_URI");
        String user = envOrDefault("COGNODB_USER", "cognodb");
        String password = requiredEnv("COGNODB_PASSWORD");

        return GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    private static String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

