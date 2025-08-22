package org.zb.ecommerce.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalCoupons;
    private long activeCoupons;
    private long totalOrders;
    private long pendingOrders;
}