package com.hotrodoan.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RedissonConfig {

    @Value("${redis.password:}")
    private String redisPassword;

    @Value("${redis.node1.host:redis-1}")
    private String node1Host;

    @Value("${redis.node2.host:redis-2}")
    private String node2Host;

    @Value("${redis.node3.host:redis-3}")
    private String node3Host;

    private RedissonClient createClient(String host, int port) {
        Config config = new Config();
        String address = String.format("redis://%s:%d", host, port);
        
        config.useSingleServer()
              .setAddress(address)
              .setPassword((redisPassword == null || redisPassword.trim().isEmpty()) ? null : redisPassword)
              .setConnectionMinimumIdleSize(2)
              .setConnectionPoolSize(10)
              .setConnectTimeout(3000)
              .setTimeout(3000);

        return Redisson.create(config);
    }

    @Primary
    @Bean(name = "redissonClient1")
    public RedissonClient redissonClient1() {
        return createClient(node1Host, 6379);
    }

    @Bean(name = "redissonClient2")
    public RedissonClient redissonClient2() {
        return createClient(node2Host, 6379);
    }

    @Bean(name = "redissonClient3")
    public RedissonClient redissonClient3() {
        return createClient(node3Host, 6379);
    }
}
