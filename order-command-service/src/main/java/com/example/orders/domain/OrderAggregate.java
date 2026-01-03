package com.example.orders.domain;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import com.example.orders.contracts.command.AddOrderItemCommand;
import com.example.orders.contracts.command.CancelOrderCommand;
import com.example.orders.contracts.command.ConfirmPaymentCommand;
import com.example.orders.contracts.command.PlaceOrderCommand;
import com.example.orders.contracts.command.ShipOrderCommand;
import com.example.orders.contracts.common.OrderStatus;
import com.example.orders.contracts.event.OrderCancelledEvent;
import com.example.orders.contracts.event.OrderItemAddedEvent;
import com.example.orders.contracts.event.OrderPlacedEvent;
import com.example.orders.contracts.event.OrderShippedEvent;
import com.example.orders.contracts.event.PaymentConfirmedEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class OrderAggregate {
  @AggregateIdentifier
  private String orderId;
  private String customerId;
  private OrderStatus status;
  private Map<String, Integer> items = new HashMap<>();

  protected OrderAggregate() {
  }

  @CommandHandler
  public OrderAggregate(PlaceOrderCommand command) {
    requireNonBlank(command.orderId(), "orderId is required");
    requireNonBlank(command.customerId(), "customerId is required");
    apply(new OrderPlacedEvent(command.orderId(), command.customerId()));
  }

  @CommandHandler
  public void handle(AddOrderItemCommand command) {
    requireNonBlank(command.sku(), "sku is required");
    if (command.qty() <= 0) {
      throw new IllegalArgumentException("qty must be > 0");
    }
    requireStatusIn("cannot add items before OrderPlaced", OrderStatus.PLACED, OrderStatus.PAID);
    apply(new OrderItemAddedEvent(orderId, command.sku(), command.qty()));
  }

  @CommandHandler
  public void handle(ConfirmPaymentCommand command) {
    requireNonBlank(command.paymentId(), "paymentId is required");
    requireStatusIn("cannot confirm payment before OrderPlaced", OrderStatus.PLACED);
    apply(new PaymentConfirmedEvent(orderId, command.paymentId()));
  }

  @CommandHandler
  public void handle(CancelOrderCommand command) {
    requireNonBlank(command.reason(), "reason is required");
    if (status == OrderStatus.SHIPPED) {
      throw new IllegalStateException("cannot cancel after shipped");
    }
    if (status == OrderStatus.CANCELLED) {
      throw new IllegalStateException("order already cancelled");
    }
    apply(new OrderCancelledEvent(orderId, command.reason()));
  }

  @CommandHandler
  public void handle(ShipOrderCommand command) {
    requireNonBlank(command.shipmentId(), "shipmentId is required");
    requireStatusIn("cannot ship before PaymentConfirmed", OrderStatus.PAID);
    apply(new OrderShippedEvent(orderId, command.shipmentId()));
  }

  @EventSourcingHandler
  public void on(OrderPlacedEvent event) {
    orderId = event.orderId();
    customerId = event.customerId();
    status = OrderStatus.PLACED;
  }

  @EventSourcingHandler
  public void on(OrderItemAddedEvent event) {
    items.merge(event.sku(), event.qty(), Integer::sum);
  }

  @EventSourcingHandler
  public void on(PaymentConfirmedEvent event) {
    status = OrderStatus.PAID;
  }

  @EventSourcingHandler
  public void on(OrderCancelledEvent event) {
    status = OrderStatus.CANCELLED;
  }

  @EventSourcingHandler
  public void on(OrderShippedEvent event) {
    status = OrderStatus.SHIPPED;
  }

  private void requireStatusIn(String message, OrderStatus... allowed) {
    if (status == null || Arrays.stream(allowed).noneMatch(status::equals)) {
      throw new IllegalStateException(message);
    }
  }

  private void requireNonBlank(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
  }
}
