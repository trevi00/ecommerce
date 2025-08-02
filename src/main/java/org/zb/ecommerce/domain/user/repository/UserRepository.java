package org.zb.ecommerce.domain.user.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zb.ecommerce.domain.user.entity.User;

import java.util.Optional;

/**
 * User Repository Interface
 * Spring Data JDBC를 사용한 데이터 접근 계층
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    
    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 중복 확인
     */
    boolean existsByEmail(String email);
    
    /**
     * 이메일과 비밀번호로 사용자 조회
     */
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    Optional<User> findByEmailAndPassword(@Param("email") String email, @Param("password") String password);
    
    /**
     * 사용자 삭제 (논리적 삭제를 위해 추후 수정 가능)
     */
    @Query("DELETE FROM users WHERE id = :id")
    void deleteById(@Param("id") Long id);
}
