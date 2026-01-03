package com.example.orders.contracts.query;

import com.example.orders.contracts.common.OrderStatus;
import java.util.List;

public record OrderDetails(
    String orderId,
    String customerId,
    OrderStatus status,
    int itemCount,
    List<OrderItemDto> items
) {
}
