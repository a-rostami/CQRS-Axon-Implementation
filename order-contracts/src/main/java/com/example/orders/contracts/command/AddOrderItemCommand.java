package com.example.orders.contracts.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record AddOrderItemCommand(@TargetAggregateIdentifier String orderId, String sku, int qty) {
}
