package com.example.orders.contracts.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ConfirmPaymentCommand(@TargetAggregateIdentifier String orderId, String paymentId) {
}
