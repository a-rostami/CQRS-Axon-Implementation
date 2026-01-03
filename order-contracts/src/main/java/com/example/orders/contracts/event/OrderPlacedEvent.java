package com.example.orders.contracts.event;

public record OrderPlacedEvent(String orderId, String customerId) {
}
