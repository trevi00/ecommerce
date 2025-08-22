package org.zb.ecommerce.global.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.zb.ecommerce.config.BaseIntegrationTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CacheServiceTest extends BaseIntegrationTest {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void set_및_get_테스트() {
        // given
        String key = "test-key";
        String value = "test-value";

        // when
        cacheService.set(key, value);
        String result = cacheService.get(key, String.class);

        // then
        assertThat(result).isEqualTo(value);
    }

    @Test
    void set_with_timeout_테스트() throws InterruptedException {
        // given
        String key = "test-key-timeout";
        String value = "test-value";
        Duration timeout = Duration.ofSeconds(1);

        // when
        cacheService.set(key, value, timeout);
        String resultBefore = cacheService.get(key, String.class);
        
        Thread.sleep(1100); // 1.1초 대기
        
        String resultAfter = cacheService.get(key, String.class);

        // then
        assertThat(resultBefore).isEqualTo(value);
        assertThat(resultAfter).isNull();
    }

    @Test
    void delete_테스트() {
        // given
        String key = "test-key-delete";
        String value = "test-value";
        cacheService.set(key, value);

        // when
        cacheService.delete(key);
        String result = cacheService.get(key, String.class);

        // then
        assertThat(result).isNull();
    }

    @Test
    void hasKey_테스트() {
        // given
        String key = "test-key-exists";
        String value = "test-value";

        // when & then
        assertThat(cacheService.hasKey(key)).isFalse();
        
        cacheService.set(key, value);
        assertThat(cacheService.hasKey(key)).isTrue();
        
        cacheService.delete(key);
        assertThat(cacheService.hasKey(key)).isFalse();
    }

    @Test
    void increment_테스트() {
        // given
        String key = "test-counter";

        // when
        cacheService.increment(key);
        cacheService.increment(key);

        // then
        Integer result = cacheService.get(key, Integer.class);
        assertThat(result).isEqualTo(2);
    }

    @Test
    void decrement_테스트() {
        // given
        String key = "test-counter-dec";
        cacheService.set(key, 5);

        // when
        cacheService.decrement(key);
        cacheService.decrement(key);

        // then
        Integer result = cacheService.get(key, Integer.class);
        assertThat(result).isEqualTo(3);
    }

    @Test
    void expire_테스트() throws InterruptedException {
        // given
        String key = "test-expire";
        String value = "test-value";
        cacheService.set(key, value);

        // when
        cacheService.expire(key, Duration.ofSeconds(1));
        String resultBefore = cacheService.get(key, String.class);
        
        Thread.sleep(1100); // 1.1초 대기
        
        String resultAfter = cacheService.get(key, String.class);

        // then
        assertThat(resultBefore).isEqualTo(value);
        assertThat(resultAfter).isNull();
    }
}