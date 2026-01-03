package com.example.orders.contracts.event;

public record OrderCancelledEvent(String orderId, String reason) {
}
