package org.zb.ecommerce.global.transaction;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.user.entity.User;
import org.zb.ecommerce.domain.user.entity.UserRole;
import org.zb.ecommerce.domain.user.exception.CustomCheckedException;
import org.zb.ecommerce.domain.user.exception.CustomRuntimeException;
import org.zb.ecommerce.domain.user.repository.UserRepository;
import org.zb.ecommerce.global.annotation.CustomTransactional;

/**
 * 트랜잭션 테스트용 서비스
 */
@Service
public class TransactionalTestService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public TransactionalTestService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // REQUIRED: 기존 트랜잭션이 있으면 참여, 없으면 새로 생성 (기본값)
    @CustomTransactional(propagation = Propagation.REQUIRED)
    public void testRequiredPropagation(String email) {
        User user = createTestUser(email);
        userRepository.save(user);
    }
    
    // REQUIRES_NEW: 항상 새로운 트랜잭션 생성
    @CustomTransactional(propagation = Propagation.REQUIRES_NEW)
    public void testRequiresNewPropagation(String email) {
        User user = createTestUser(email);
        userRepository.save(user);
    }
    
    // MANDATORY: 기존 트랜잭션이 반드시 있어야 함, 없으면 예외 발생
    @CustomTransactional(propagation = Propagation.MANDATORY)
    public void testMandatoryPropagation(String email) {
        User user = createTestUser(email);
        userRepository.save(user);
    }
    
    // SUPPORTS: 트랜잭션이 있으면 참여, 없으면 비트랜잭션으로 실행
    @CustomTransactional(propagation = Propagation.SUPPORTS)
    public void testSupportsPropagation(String email) {
        User user = createTestUser(email);
        userRepository.save(user);
    }
    
    // NEVER: 트랜잭션이 있으면 예외 발생
    @CustomTransactional(propagation = Propagation.NEVER)
    public void testNeverPropagation(String email) {
        User user = createTestUser(email);
        userRepository.save(user);
    }
    
    // RuntimeException 발생 - 기본적으로 롤백됨
    @CustomTransactional
    public void throwRuntimeException(String email) {
        User user = createTestUser(email);
        userRepository.save(user);
        throw new CustomRuntimeException("테스트용 RuntimeException");
    }
    
    // CheckedException 발생 - 기본적으로 롤백되지 않음
    @CustomTransactional
    public void throwCheckedException(String email) throws CustomCheckedException {
        User user = createTestUser(email);
        userRepository.save(user);
        throw new CustomCheckedException("테스트용 CheckedException");
    }
    
    // CheckedException에 대해 명시적으로 롤백 설정
    @CustomTransactional(rollbackFor = CustomCheckedException.class)
    public void throwCheckedExceptionWithRollback(String email) throws CustomCheckedException {
        User user = createTestUser(email);
        userRepository.save(user);
        throw new CustomCheckedException("명시적 롤백 설정된 CheckedException");
    }
    
    // RuntimeException에 대해 명시적으로 noRollback 설정
    @CustomTransactional(noRollbackFor = CustomRuntimeException.class)
    public void throwRuntimeExceptionWithNoRollback(String email) {
        User user = createTestUser(email);
        userRepository.save(user);
        throw new CustomRuntimeException("noRollback 설정된 RuntimeException");
    }
    
    // Private 메서드 트랜잭션 테스트
    public void callPrivateTransactionalMethod(String email) {
        privateTransactionalMethod(email);
    }
    
    public void callPrivateTransactionalMethodWithException(String email) {
        privateTransactionalMethodWithException(email);
    }
    
    // Private 메서드에는 @Transactional이 적용되지 않음
    @CustomTransactional
    private void privateTransactionalMethod(String email) {
        User user = createTestUser(email);
        userRepository.save(user);
    }
    
    @CustomTransactional
    private void privateTransactionalMethodWithException(String email) {
        User user = createTestUser(email);
        userRepository.save(user);
        throw new CustomRuntimeException("Private 메서드에서 발생한 예외");
    }
    
    private User createTestUser(String email) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .name("테스트사용자")
                .phone("010-1234-5678")
                .role(UserRole.GENERAL)
                .build();
    }
}