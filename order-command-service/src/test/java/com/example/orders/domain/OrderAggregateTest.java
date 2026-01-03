package com.example.orders.domain;

import com.example.orders.contracts.command.AddOrderItemCommand;
import com.example.orders.contracts.command.CancelOrderCommand;
import com.example.orders.contracts.command.PlaceOrderCommand;
import com.example.orders.contracts.command.ShipOrderCommand;
import com.example.orders.contracts.event.OrderCancelledEvent;
import com.example.orders.contracts.event.OrderItemAddedEvent;
import com.example.orders.contracts.event.OrderPlacedEvent;
import com.example.orders.contracts.event.OrderShippedEvent;
import com.example.orders.contracts.event.PaymentConfirmedEvent;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderAggregateTest {
  private FixtureConfiguration<OrderAggregate> fixture;

  @BeforeEach
  void setUp() {
    fixture = new AggregateTestFixture<>(OrderAggregate.class);
  }

  @Test
  void placeOrder_emitsPlacedEvent() {
    String orderId = "order-1";
    String customerId = "cust-1";

    fixture.givenNoPriorActivity()
        .when(new PlaceOrderCommand(orderId, customerId))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new OrderPlacedEvent(orderId, customerId));
  }

  @Test
  void addItem_afterPlaced_emitsItemAddedEvent() {
    String orderId = "order-1";
    String customerId = "cust-1";

    fixture.given(new OrderPlacedEvent(orderId, customerId))
        .when(new AddOrderItemCommand(orderId, "SKU-1", 2))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new OrderItemAddedEvent(orderId, "SKU-1", 2));
  }

  @Test
  void addItem_beforePlaced_isRejected() {
    fixture.givenNoPriorActivity()
        .when(new AddOrderItemCommand("order-1", "SKU-1", 1))
        .expectException(AggregateNotFoundException.class);
  }

  @Test
  void addItem_requiresPositiveQty() {
    String orderId = "order-1";
    String customerId = "cust-1";

    fixture.given(new OrderPlacedEvent(orderId, customerId))
        .when(new AddOrderItemCommand(orderId, "SKU-1", 0))
        .expectException(IllegalArgumentException.class)
        .expectExceptionMessage("qty must be > 0");
  }

  @Test
  void ship_beforePayment_isRejected() {
    String orderId = "order-1";
    String customerId = "cust-1";

    fixture.given(new OrderPlacedEvent(orderId, customerId))
        .when(new ShipOrderCommand(orderId, "ship-1"))
        .expectException(IllegalStateException.class)
        .expectExceptionMessage("cannot ship before PaymentConfirmed");
  }

  @Test
  void cancel_afterShipped_isRejected() {
    String orderId = "order-1";
    String customerId = "cust-1";

    fixture.given(
            new OrderPlacedEvent(orderId, customerId),
            new PaymentConfirmedEvent(orderId, "pay-1"),
            new OrderShippedEvent(orderId, "ship-1"))
        .when(new CancelOrderCommand(orderId, "changed mind"))
        .expectException(IllegalStateException.class)
        .expectExceptionMessage("cannot cancel after shipped");
  }

  @Test
  void cancel_beforeShipped_emitsCancelledEvent() {
    String orderId = "order-1";
    String customerId = "cust-1";

    fixture.given(new OrderPlacedEvent(orderId, customerId))
        .when(new CancelOrderCommand(orderId, "changed mind"))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new OrderCancelledEvent(orderId, "changed mind"));
  }
}
