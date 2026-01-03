package com.example.orders.contracts.event;

public record OrderItemAddedEvent(String orderId, String sku, int qty) {
}
