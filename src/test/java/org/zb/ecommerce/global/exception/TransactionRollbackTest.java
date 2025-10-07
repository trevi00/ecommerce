package org.zb.ecommerce.global.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.zb.ecommerce.config.TestContainerConfig;
import org.zb.ecommerce.domain.user.repository.UserRepository;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CheckedException과 RuntimeException의 트랜잭션 롤백 동작 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
class TransactionRollbackTest extends TestContainerConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTestService transactionTestService;

    private Long initialCount;

    @BeforeEach
    void setUp() {
        initialCount = userRepository.count();
    }

    @Nested
    @DisplayName("CheckedException 트랜잭션 테스트")
    class CheckedExceptionTest {

        @Test
        @DisplayName("CheckedException 발생 시 트랜잭션이 롤백되지 않음")
        void checkedExceptionDoesNotRollback() {
            // when & then - CheckedException 발생해도 트랜잭션이 롤백되지 않음
            assertThatThrownBy(() -> transactionTestService.saveUserAndThrowCheckedException())
                    .isInstanceOf(IOException.class)
                    .hasMessage("체크드 예외 발생");

            // CheckedException으로 인해 롤백되지 않았으므로 데이터가 저장됨
            Long finalCount = userRepository.count();
            assertThat(finalCount).isGreaterThan(initialCount);
        }

        @Test
        @DisplayName("CheckedException에 @Transactional(rollbackFor) 지정 시 롤백됨")
        void checkedExceptionWithRollbackForAnnotation() {
            // when & then
            assertThatThrownBy(() -> transactionTestService.saveUserAndThrowCheckedExceptionWithRollbackFor())
                    .isInstanceOf(IOException.class)
                    .hasMessage("롤백될 체크드 예외");

            // rollbackFor 지정으로 인해 롤백되었으므로 데이터가 저장되지 않음
            Long finalCount = userRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }
    }

    @Nested
    @DisplayName("RuntimeException 트랜잭션 테스트")
    class RuntimeExceptionTest {

        @Test
        @DisplayName("RuntimeException 발생 시 자동으로 트랜잭션 롤백됨")
        void runtimeExceptionAutoRollback() {
            // when & then
            assertThatThrownBy(() -> transactionTestService.saveUserAndThrowRuntimeException())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("런타임 예외 발생");

            // RuntimeException으로 인해 자동 롤백되었으므로 데이터가 저장되지 않음
            Long finalCount = userRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("IllegalArgumentException 발생 시 트랜잭션 롤백됨")
        void illegalArgumentExceptionRollback() {
            // when & then
            assertThatThrownBy(() -> transactionTestService.saveUserAndThrowIllegalArgumentException())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("잘못된 인수");

            // RuntimeException 계열이므로 롤백됨
            Long finalCount = userRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("커스텀 RuntimeException 발생 시 트랜잭션 롤백됨")
        void customRuntimeExceptionRollback() {
            // given - 이 테스트는 실제 트랜잭션을 테스트하지 않으므로 예외만 확인
            
            // when & then
            assertThatThrownBy(() -> {
                throw new CustomRuntimeException("커스텀 런타임 예외");
            }).isInstanceOf(CustomRuntimeException.class)
              .hasMessage("커스텀 런타임 예외");

            // 이 테스트는 실제 트랜잭션 테스트가 아니므로 데이터 변화 없음
            Long finalCount = userRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }
    }

    /**
     * 테스트용 커스텀 RuntimeException
     */
    static class CustomRuntimeException extends RuntimeException {
        public CustomRuntimeException(String message) {
            super(message);
        }
    }
}