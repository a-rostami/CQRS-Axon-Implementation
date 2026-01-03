package com.example.orders.contracts.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ShipOrderCommand(@TargetAggregateIdentifier String orderId, String shipmentId) {
}
