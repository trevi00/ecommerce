package org.zb.ecommerce.global.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zb.ecommerce.domain.user.entity.User;
import org.zb.ecommerce.domain.user.entity.UserRole;
import org.zb.ecommerce.domain.user.repository.UserRepository;

import java.io.IOException;

/**
 * 트랜잭션 테스트를 위한 서비스 클래스
 */
@Service
public class TransactionTestService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void saveUserAndThrowCheckedException() throws IOException {
        User user = User.builder()
                .email("checked-" + System.currentTimeMillis() + "@example.com")
                .password("password123")
                .name("체크드예외테스트")
                .phone("010-1111-1111")
                .role(UserRole.GENERAL)
                .build();
        
        userRepository.save(user);
        
        // CheckedException 발생
        throw new IOException("체크드 예외 발생");
    }

    @Transactional(rollbackFor = IOException.class)
    public void saveUserAndThrowCheckedExceptionWithRollbackFor() throws IOException {
        User user = User.builder()
                .email("rollback-" + System.currentTimeMillis() + "@example.com")
                .password("password123")
                .name("롤백테스트")
                .phone("010-2222-2222")
                .role(UserRole.GENERAL)
                .build();
        
        userRepository.save(user);
        
        // CheckedException 발생하지만 rollbackFor로 지정했으므로 롤백됨
        throw new IOException("롤백될 체크드 예외");
    }

    @Transactional
    public void saveUserAndThrowRuntimeException() {
        User user = User.builder()
                .email("runtime-" + System.currentTimeMillis() + "@example.com")
                .password("password123")
                .name("런타임예외테스트")
                .phone("010-3333-3333")
                .role(UserRole.GENERAL)
                .build();
        
        userRepository.save(user);
        
        // RuntimeException 발생 시 자동으로 롤백됨
        throw new RuntimeException("런타임 예외 발생");
    }

    @Transactional
    public void saveUserAndThrowIllegalArgumentException() {
        User user1 = User.builder()
                .email("illegal1-" + System.currentTimeMillis() + "@example.com")
                .password("password123")
                .name("사용자1")
                .phone("010-4444-4444")
                .role(UserRole.GENERAL)
                .build();
        
        User user2 = User.builder()
                .email("illegal2-" + System.currentTimeMillis() + "@example.com")
                .password("password456")
                .name("사용자2")
                .phone("010-5555-5555")
                .role(UserRole.VIP)
                .build();
        
        userRepository.save(user1);
        userRepository.save(user2);
        
        // IllegalArgumentException 발생
        throw new IllegalArgumentException("잘못된 인수");
    }
}