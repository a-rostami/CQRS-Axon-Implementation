package com.example.orders.contracts.query;

import com.example.orders.contracts.common.OrderStatus;
import java.time.Instant;

public record OrderSummary(
    String orderId,
    String customerId,
    OrderStatus status,
    int itemCount,
    Instant lastUpdated
) {
}
