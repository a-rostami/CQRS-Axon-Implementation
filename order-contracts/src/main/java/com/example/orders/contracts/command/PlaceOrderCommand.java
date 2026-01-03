package com.example.orders.contracts.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record PlaceOrderCommand(@TargetAggregateIdentifier String orderId, String customerId) {
}
