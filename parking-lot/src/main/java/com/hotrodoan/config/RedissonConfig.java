package com.hotrodoan.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${SPRING_DATA_REDIS_HOST:redis}")
    private String redisHost;

    @Value("${SPRING_DATA_REDIS_PORT:6379}")
    private String redisPort;

    @Value("${SPRING_DATA_REDIS_PASSWORD:}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + redisHost + ":" + redisPort;
        
        var singleServerConfig = config.useSingleServer()
                .setAddress(address)
                .setConnectionMinimumIdleSize(10)
                .setConnectionPoolSize(64);

        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            singleServerConfig.setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}
