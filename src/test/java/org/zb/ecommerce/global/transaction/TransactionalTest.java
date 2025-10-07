package org.zb.ecommerce.global.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zb.ecommerce.config.BaseIntegrationTest;
import org.springframework.transaction.annotation.Propagation;
import org.zb.ecommerce.domain.user.dto.SignUpRequest;
import org.zb.ecommerce.domain.user.entity.User;
import org.zb.ecommerce.domain.user.exception.CustomCheckedException;
import org.zb.ecommerce.domain.user.exception.CustomRuntimeException;
import org.zb.ecommerce.domain.user.repository.UserRepository;
import org.zb.ecommerce.global.annotation.CustomTransactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 트랜잭션 전파 옵션 및 롤백 조건 테스트
 */
@Disabled("MySQL TestContainers와 트랜잭션 전파 테스트 호환성 문제로 비활성화")
class TransactionalTest extends BaseIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionalTestService transactionalTestService;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }
    
    @Nested
    @DisplayName("트랜잭션 전파 옵션 테스트")
    class PropagationTest {
        
        @Test
        @DisplayName("REQUIRED - 기존 트랜잭션 사용")
        void propagation_Required() {
            // given
            String email = "required@example.com";
            
            // when & then
            assertAll(
                "REQUIRED 전파 옵션 검증",
                () -> assertDoesNotThrow(() -> 
                    transactionalTestService.testRequiredPropagation(email)),
                () -> assertTrue(userRepository.existsByEmail(email), 
                    "사용자가 정상적으로 저장되어야 함"),
                () -> assertEquals(1L, userRepository.count(), 
                    "총 1명의 사용자가 저장되어야 함")
            );
        }
        
        @Test
        @DisplayName("REQUIRES_NEW - 새로운 트랜잭션 생성")
        void propagation_RequiresNew() {
            // given
            String email = "requires-new@example.com";
            
            // when & then
            assertAll(
                "REQUIRES_NEW 전파 옵션 검증",
                () -> assertDoesNotThrow(() -> 
                    transactionalTestService.testRequiresNewPropagation(email)),
                () -> assertTrue(userRepository.existsByEmail(email)),
                () -> assertNotNull(userRepository.findByEmail(email).orElse(null))
            );
        }
        
        @Test
        @DisplayName("MANDATORY - 기존 트랜잭션 필수")
        void propagation_Mandatory() {
            // given
            String email = "mandatory@example.com";
            
            // when & then - 트랜잭션 없이 호출 시 예외 발생
            assertThrows(RuntimeException.class, () -> 
                transactionalTestService.testMandatoryPropagation(email),
                "기존 트랜잭션이 없으면 예외가 발생해야 함");
        }
        
        @Test
        @DisplayName("SUPPORTS - 트랜잭션이 있으면 참여, 없으면 비트랜잭션으로 실행")
        void propagation_Supports() {
            // given
            String email = "supports@example.com";
            
            // when & then
            assertAll(
                "SUPPORTS 전파 옵션 검증",
                () -> assertDoesNotThrow(() -> 
                    transactionalTestService.testSupportsPropagation(email)),
                () -> assertTrue(userRepository.existsByEmail(email))
            );
        }
        
        @Test
        @DisplayName("NEVER - 트랜잭션 금지")
        void propagation_Never() {
            // given
            String email = "never@example.com";
            
            // when & then - 트랜잭션 컨텍스트에서 호출하면 예외 발생하는지 확인
            assertDoesNotThrow(() -> 
                transactionalTestService.testNeverPropagation(email),
                "트랜잭션이 없을 때는 정상 실행되어야 함");
        }
    }
    
    @Nested
    @DisplayName("롤백 조건 테스트")
    class RollbackTest {
        
        @Test
        @DisplayName("RuntimeException 발생 시 롤백")
        void rollback_RuntimeException() {
            // given
            String email = "rollback-runtime@example.com";
            
            // when & then
            assertAll(
                "RuntimeException 롤백 검증",
                () -> assertThrows(CustomRuntimeException.class, () -> 
                    transactionalTestService.throwRuntimeException(email)),
                () -> assertFalse(userRepository.existsByEmail(email), 
                    "RuntimeException 발생 시 롤백되어 사용자가 저장되지 않아야 함"),
                () -> assertEquals(0L, userRepository.count())
            );
        }
        
        @Test
        @DisplayName("CheckedException 발생 시 롤백 안됨 (기본)")
        void noRollback_CheckedException_Default() {
            // given
            String email = "no-rollback-checked@example.com";
            
            // when & then
            assertAll(
                "CheckedException 기본 동작 검증",
                () -> assertThrows(CustomCheckedException.class, () -> 
                    transactionalTestService.throwCheckedException(email)),
                () -> assertTrue(userRepository.existsByEmail(email), 
                    "CheckedException 발생 시 기본적으로 롤백되지 않아 사용자가 저장되어야 함")
            );
        }
        
        @Test
        @DisplayName("CheckedException에 대해 명시적 롤백 설정")
        void rollback_CheckedException_Explicit() {
            // given
            String email = "rollback-checked@example.com";
            
            // when & then
            assertAll(
                "CheckedException 명시적 롤백 검증",
                () -> assertThrows(CustomCheckedException.class, () -> 
                    transactionalTestService.throwCheckedExceptionWithRollback(email)),
                () -> assertFalse(userRepository.existsByEmail(email), 
                    "명시적 롤백 설정으로 CheckedException 발생 시에도 롤백되어야 함")
            );
        }
        
        @Test
        @DisplayName("RuntimeException에 대해 명시적 noRollback 설정")
        void noRollback_RuntimeException_Explicit() {
            // given
            String email = "no-rollback-runtime@example.com";
            
            // when & then
            assertAll(
                "RuntimeException noRollback 검증",
                () -> assertThrows(CustomRuntimeException.class, () -> 
                    transactionalTestService.throwRuntimeExceptionWithNoRollback(email)),
                () -> assertTrue(userRepository.existsByEmail(email), 
                    "noRollbackFor 설정으로 RuntimeException 발생 시에도 커밋되어야 함")
            );
        }
    }
    
    @Nested
    @DisplayName("Private 메서드 트랜잭션 테스트")
    class PrivateMethodTest {
        
        @Test
        @DisplayName("Private 메서드에는 트랜잭션이 적용되지 않음")
        void privateMethod_NoTransaction() {
            // given
            String email = "private-method@example.com";
            
            // when & then
            assertAll(
                "Private 메서드 트랜잭션 검증",
                () -> assertDoesNotThrow(() -> 
                    transactionalTestService.callPrivateTransactionalMethod(email)),
                () -> assertTrue(userRepository.existsByEmail(email), 
                    "Private 메서드의 @Transactional은 무시되지만 호출은 성공해야 함")
            );
            
            // Private 메서드에서 예외 발생 시에도 롤백되지 않음을 확인
            String email2 = "private-method-exception@example.com";
            assertThrows(CustomRuntimeException.class, () -> 
                transactionalTestService.callPrivateTransactionalMethodWithException(email2));
            assertTrue(userRepository.existsByEmail(email2), 
                "Private 메서드에서는 트랜잭션이 적용되지 않아 예외 발생 시에도 데이터가 저장됨");
        }
    }
}