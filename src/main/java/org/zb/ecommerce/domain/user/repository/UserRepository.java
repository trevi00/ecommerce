package org.zb.ecommerce.domain.user.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * User Repository Interface
 * Spring Data JDBC를 사용한 데이터 접근 계층
 * 가능한 한 쿼리 메서드를 사용하고, 복잡한 쿼리만 @Query 사용
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    
    /**
     * 이메일로 사용자 조회
     * 쿼리 메서드 사용 - 컴파일 타임에 검증됨
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 중복 확인
     * 쿼리 메서드 사용
     */
    boolean existsByEmail(String email);
    
    /**
     * 이메일과 비밀번호로 사용자 조회
     * 쿼리 메서드 사용으로 변경 - 타입 안전성 보장
     */
    Optional<User> findByEmailAndPassword(String email, String password);
    
    /**
     * 이름으로 사용자 검색
     * 쿼리 메서드 사용
     */
    List<User> findByNameContaining(String name);
    
    /**
     * 이메일 또는 이름으로 사용자 검색
     * 복잡한 OR 조건이므로 @Query 사용하되 동적 조건 처리
     */
    @Query("SELECT * FROM users WHERE " +
           "(:email IS NULL OR email LIKE CONCAT('%', :email, '%')) AND " +
           "(:name IS NULL OR name LIKE CONCAT('%', :name, '%'))")
    List<User> findByEmailContainingOrNameContaining(@Param("email") String email, @Param("name") String name);
    
    /**
     * 활성 사용자 수 카운트
     * 쿼리 메서드 사용
     */
    long countByRole(org.zb.ecommerce.domain.user.entity.UserRole role);
}