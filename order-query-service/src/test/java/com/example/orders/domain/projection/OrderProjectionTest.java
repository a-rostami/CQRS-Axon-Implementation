package com.example.orders.domain.projection;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.orders.contracts.common.OrderStatus;
import com.example.orders.contracts.event.OrderItemAddedEvent;
import com.example.orders.contracts.event.OrderPlacedEvent;
import com.example.orders.domain.readmodel.OrderItemRepository;
import com.example.orders.domain.readmodel.OrderSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrderProjectionTest {
  @Autowired
  private OrderSummaryRepository summaryRepository;

  @Autowired
  private OrderItemRepository itemRepository;

  private OrderProjection projection;

  @BeforeEach
  void setUp() {
    projection = new OrderProjection(summaryRepository, itemRepository);
  }

  @Test
  void orderPlaced_createsSummary() {
    projection.on(new OrderPlacedEvent("order-1", "cust-1"));

    var summary = summaryRepository.findById("order-1").orElseThrow();
    assertThat(summary.getStatus()).isEqualTo(OrderStatus.PLACED);
    assertThat(summary.getItemCount()).isZero();
    assertThat(summary.getCustomerId()).isEqualTo("cust-1");
  }

  @Test
  void orderItemAdded_incrementsCountAndQty() {
    projection.on(new OrderPlacedEvent("order-1", "cust-1"));
    projection.on(new OrderItemAddedEvent("order-1", "SKU-1", 2));
    projection.on(new OrderItemAddedEvent("order-1", "SKU-1", 3));

    var summary = summaryRepository.findById("order-1").orElseThrow();
    assertThat(summary.getItemCount()).isEqualTo(5);

    var items = itemRepository.findByIdOrderId("order-1");
    assertThat(items).hasSize(1);
    assertThat(items.getFirst().getQty()).isEqualTo(5);
  }
}
