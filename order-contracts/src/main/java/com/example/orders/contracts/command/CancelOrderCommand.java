package com.example.orders.contracts.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CancelOrderCommand(@TargetAggregateIdentifier String orderId, String reason) {
}
