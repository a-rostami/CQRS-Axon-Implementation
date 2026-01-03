package com.example.orders.contracts.event;

public record PaymentConfirmedEvent(String orderId, String paymentId) {
}
