package org.zb.ecommerce.domain.user.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.zb.ecommerce.config.TestContainerConfig;
import org.zb.ecommerce.domain.user.entity.User;
import org.zb.ecommerce.domain.user.entity.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepository @DataJdbcTest 테스트
 * TestContainers를 사용한 실제 데이터베이스 연동 테스트
 */
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Testcontainers
@DisplayName("UserRepository 데이터베이스 테스트")
class UserRepositoryDataJdbcTest extends TestContainerConfig {

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("사용자 생성 테스트")
    class CreateUserTest {

        @Test
        @DisplayName("사용자가 정상적으로 저장된다")
        void save_Success() {
            // given
            User user = User.builder()
                    .email("test@example.com")
                    .password("encoded_password")
                    .name("테스트사용자")
                    .phone("010-1234-5678")
                    .role(UserRole.GENERAL)
                    .build();

            // when
            User savedUser = userRepository.save(user);

            // then - 다양한 검증 방법 활용
            assertAll("저장된 사용자 검증",
                    () -> assertNotNull(savedUser, "저장된 사용자는 null이 아니어야 한다"),
                    () -> assertNotNull(savedUser.getId(), "ID가 생성되어야 한다"),
                    () -> assertTrue(savedUser.getId() > 0, "ID는 양수여야 한다"),
                    () -> assertEquals(user.getEmail(), savedUser.getEmail(), "이메일이 일치해야 한다"),
                    () -> assertEquals(user.getName(), savedUser.getName(), "이름이 일치해야 한다"),
                    () -> assertEquals(user.getPhone(), savedUser.getPhone(), "전화번호가 일치해야 한다"),
                    () -> assertEquals(UserRole.GENERAL, savedUser.getRole(), "역할이 일치해야 한다"),
                    () -> assertNotNull(savedUser.getCreatedAt(), "생성일시가 설정되어야 한다"),
                    () -> assertNotNull(savedUser.getUpdatedAt(), "수정일시가 설정되어야 한다")
            );

            // AssertJ 활용
            assertThat(savedUser)
                    .extracting("email", "name", "phone", "role")
                    .containsExactly(user.getEmail(), user.getName(), user.getPhone(), UserRole.GENERAL);

            assertThat(savedUser.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
            assertThat(savedUser.getUpdatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
        }

        @Test
        @DisplayName("여러 사용자를 저장할 수 있다")
        void saveMultipleUsers_Success() {
            // given
            long timestamp = System.currentTimeMillis();
            List<User> users = List.of(
                    User.builder()
                            .email("user1-" + timestamp + "@test.com")
                            .password("password12345678")
                            .name("사용자1")
                            .phone("010-1111-1111")
                            .role(UserRole.GENERAL)
                            .build(),
                    User.builder()
                            .email("user2-" + timestamp + "@test.com")
                            .password("password12345678")
                            .name("사용자2")
                            .phone("010-2222-2222")
                            .role(UserRole.GENERAL)
                            .build(),
                    User.builder()
                            .email("user3-" + timestamp + "@test.com")
                            .password("password12345678")
                            .name("사용자3")
                            .phone("010-3333-3333")
                            .role(UserRole.GENERAL)
                            .build()
            );

            // when
            List<User> savedUsers = users.stream()
                    .map(userRepository::save)
                    .toList();

            // then
            assertEquals(3, savedUsers.size(), "3명의 사용자가 저장되어야 한다");
            assertTrue(savedUsers.stream().allMatch(u -> u.getId() != null), "모든 사용자의 ID가 생성되어야 한다");
            assertThat(savedUsers).hasSize(3);
        }

        @Test
        @DisplayName("VIP 사용자도 정상적으로 저장된다")
        void saveVipUser_Success() {
            // given
            User vipUser = User.builder()
                    .email("vip-" + System.currentTimeMillis() + "@example.com")
                    .password("vip_password123456")
                    .name("VIP사용자")
                    .phone("010-9999-9999")
                    .role(UserRole.VIP)
                    .build();

            // when
            User savedUser = userRepository.save(vipUser);

            // then
            assertEquals(UserRole.VIP, savedUser.getRole(), "VIP 역할이 저장되어야 한다");
            assertThat(savedUser.getRole()).isEqualTo(UserRole.VIP);
        }
    }

    @Nested
    @DisplayName("사용자 조회 테스트")
    class FindUserTest {

        @Test
        @DisplayName("ID로 사용자 조회가 성공한다")
        void findById_Success() {
            // given
            User testUser = userRepository.save(
                    User.builder()
                            .email("find-" + System.currentTimeMillis() + "@example.com")
                            .password("password12345678")
                            .name("조회테스트사용자")
                            .phone("010-9999-9999")
                            .role(UserRole.GENERAL)
                            .build()
            );

            // when
            Optional<User> found = userRepository.findById(testUser.getId());

            // then
            assertAll("ID로 조회 결과 검증",
                    () -> assertTrue(found.isPresent(), "사용자가 조회되어야 한다"),
                    () -> assertEquals(testUser.getId(), found.get().getId(), "ID가 일치해야 한다"),
                    () -> assertEquals(testUser.getEmail(), found.get().getEmail(), "이메일이 일치해야 한다"),
                    () -> assertEquals(testUser.getName(), found.get().getName(), "이름이 일치해야 한다")
            );

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional을 반환한다")
        void findById_NotFound_ReturnsEmpty() {
            // given
            Long nonExistentId = 99999L;

            // when
            Optional<User> found = userRepository.findById(nonExistentId);

            // then
            assertFalse(found.isPresent(), "사용자가 조회되지 않아야 한다");
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("이메일로 사용자 조회가 성공한다")
        void findByEmail_Success() {
            // given
            User testUser = userRepository.save(
                    User.builder()
                            .email("email-test-" + System.currentTimeMillis() + "@example.com")
                            .password("password12345678")
                            .name("이메일테스트사용자")
                            .phone("010-8888-8888")
                            .role(UserRole.GENERAL)
                            .build()
            );

            // when
            Optional<User> found = userRepository.findByEmail(testUser.getEmail());

            // then
            assertTrue(found.isPresent(), "이메일로 사용자가 조회되어야 한다");
            assertEquals(testUser.getEmail(), found.get().getEmail(), "이메일이 일치해야 한다");
            assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회 시 빈 Optional을 반환한다")
        void findByEmail_NotFound_ReturnsEmpty() {
            // when
            Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

            // then
            assertFalse(found.isPresent(), "존재하지 않는 이메일로는 조회되지 않아야 한다");
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("사용자 존재 여부 확인 테스트")
    class ExistsUserTest {

        @Test
        @DisplayName("존재하는 이메일 확인이 정상 작동한다")
        void existsByEmail_ExistingEmail_ReturnsTrue() {
            // given
            User user = userRepository.save(
                    User.builder()
                            .email("exists-" + System.currentTimeMillis() + "@example.com")
                            .password("password12345678")
                            .name("존재테스트사용자")
                            .phone("010-7777-7777")
                            .role(UserRole.GENERAL)
                            .build()
            );

            // when
            boolean exists = userRepository.existsByEmail(user.getEmail());

            // then
            assertTrue(exists, "존재하는 이메일은 true를 반환해야 한다");
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 이메일 확인이 정상 작동한다")
        void existsByEmail_NonExistingEmail_ReturnsFalse() {
            // when
            boolean exists = userRepository.existsByEmail("notexists@example.com");

            // then
            assertFalse(exists, "존재하지 않는 이메일은 false를 반환해야 한다");
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("사용자 수 카운트 테스트")
    class CountUserTest {

        @Test
        @DisplayName("사용자 수 카운트가 정확하다")
        void count_ReturnsCorrectCount() {
            // given
            long initialCount = userRepository.count();
            long timestamp = System.currentTimeMillis();
            
            List<User> users = List.of(
                    User.builder()
                            .email("count1-" + timestamp + "@example.com")
                            .password("password12345678")
                            .name("카운트사용자1")
                            .phone("010-1000-0001")
                            .role(UserRole.GENERAL)
                            .build(),
                    User.builder()
                            .email("count2-" + timestamp + "@example.com")
                            .password("password12345678")
                            .name("카운트사용자2")
                            .phone("010-1000-0002")
                            .role(UserRole.GENERAL)
                            .build()
            );

            // when
            users.forEach(userRepository::save);
            long finalCount = userRepository.count();

            // then
            assertEquals(initialCount + 2, finalCount, "사용자 수가 2명 증가해야 한다");
            assertThat(finalCount).isEqualTo(initialCount + 2);
            assertTrue(finalCount >= 2, "총 사용자 수는 2명 이상이어야 한다");
        }
    }

    @Nested
    @DisplayName("사용자 삭제 테스트")
    class DeleteUserTest {

        @Test
        @DisplayName("사용자 삭제가 정상 작동한다")
        void delete_Success() {
            // given
            User user = userRepository.save(
                    User.builder()
                            .email("delete-" + System.currentTimeMillis() + "@example.com")
                            .password("password12345678")
                            .name("삭제테스트사용자")
                            .phone("010-6666-6666")
                            .role(UserRole.GENERAL)
                            .build()
            );

            Long userId = user.getId();
            assertTrue(userRepository.findById(userId).isPresent(), "삭제 전에 사용자가 존재해야 한다");

            // when
            userRepository.deleteById(userId);

            // then
            Optional<User> deletedUser = userRepository.findById(userId);
            assertAll("삭제 결과 검증",
                    () -> assertFalse(deletedUser.isPresent(), "삭제 후 사용자가 존재하지 않아야 한다"),
                    () -> assertFalse(userRepository.existsByEmail(user.getEmail()), "이메일로도 조회되지 않아야 한다")
            );

            assertThat(deletedUser).isEmpty();
        }
    }

    @Nested
    @DisplayName("데이터베이스 제약조건 테스트")
    class ConstraintTest {

        @Test
        @DisplayName("필수 필드 누락 시 저장이 실패한다")
        void save_MissingRequiredFields_ShouldFail() {
            // given - 이메일이 null이거나 빈 문자열인 경우
            assertThrows(IllegalArgumentException.class, () -> {
                User.builder()
                        .email(null)
                        .password("password12345678")
                        .name("테스트사용자")
                        .phone("010-1234-5678")
                        .role(UserRole.GENERAL)
                        .build();
            }, "이메일이 null일 때 예외가 발생해야 한다");

            assertThrows(IllegalArgumentException.class, () -> {
                User.builder()
                        .email("")
                        .password("password12345678")
                        .name("테스트사용자")
                        .phone("010-1234-5678")
                        .role(UserRole.GENERAL)
                        .build();
            }, "이메일이 빈 문자열일 때 예외가 발생해야 한다");
        }

        @Test
        @DisplayName("이메일 중복 시 저장이 실패한다")
        void save_DuplicateEmail_ShouldFail() {
            // given
            String duplicateEmail = "duplicate-" + System.currentTimeMillis() + "@example.com";
            
            User firstUser = User.builder()
                    .email(duplicateEmail)
                    .password("password12345678")
                    .name("첫번째사용자")
                    .phone("010-5555-5555")
                    .role(UserRole.GENERAL)
                    .build();
            
            User secondUser = User.builder()
                    .email(duplicateEmail)
                    .password("password87654321")
                    .name("두번째사용자")
                    .phone("010-4444-4444")
                    .role(UserRole.GENERAL)
                    .build();

            userRepository.save(firstUser);

            // when & then - DuplicateKeyException이 DataIntegrityViolationException으로 래핑됨
            assertThrows(Exception.class, () -> userRepository.save(secondUser),
                    "중복 이메일로 저장 시 예외가 발생해야 한다");
        }
    }
}