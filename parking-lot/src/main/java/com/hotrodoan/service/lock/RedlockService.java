package com.hotrodoan.service.lock;

import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RedlockService {

    private final RedissonClient client1;
    private final RedissonClient client2;
    private final RedissonClient client3;

    public RedlockService(
            @Qualifier("redissonClient1") RedissonClient client1,
            @Qualifier("redissonClient2") RedissonClient client2,
            @Qualifier("redissonClient3") RedissonClient client3) {
        this.client1 = client1;
        this.client2 = client2;
        this.client3 = client3;
    }

    /**
     * Thu thập RLock từ cả 3 node Redis độc lập
     */
    public RedissonRedLock getRedlock(String lockKey) {
        RLock lock1 = client1.getLock(lockKey);
        RLock lock2 = client2.getLock(lockKey);
        RLock lock3 = client3.getLock(lockKey);

        return new RedissonRedLock(lock1, lock2, lock3);
    }
}
