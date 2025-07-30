package org.zb.ecommerce.domain.user.entity;

/**
 * 사용자 등급 Enum
 */
public enum UserRole {
    GENERAL("일반회원"),
    VIP("VIP회원");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
