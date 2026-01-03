package com.example.orders.contracts.event;

public record OrderShippedEvent(String orderId, String shipmentId) {
}
