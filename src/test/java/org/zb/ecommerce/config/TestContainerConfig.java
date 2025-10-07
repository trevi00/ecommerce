package org.zb.ecommerce.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

public abstract class TestContainerConfig {

    private static final MySQLContainer<?> mysqlContainer;
    private static final GenericContainer<?> redisContainer;

    static {
        mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withInitScript("test-schema.sql")
                .withReuse(true);

        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379)
                .withReuse(true);

        // 컨테이너들을 병렬로 시작하여 시작 시간 단축
        Startables.deepStart(mysqlContainer, redisContainer).join();
    }

    public static MySQLContainer<?> getMysqlContainer() {
        return mysqlContainer;
    }

    public static GenericContainer<?> getRedisContainer() {
        return redisContainer;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL 설정
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        
        // Redis 설정 (TestContainer 사용)
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379).toString());
        
        // Schema 초기화 비활성화 (TestContainer가 초기화함)
        registry.add("spring.sql.init.mode", () -> "never");
    }
}