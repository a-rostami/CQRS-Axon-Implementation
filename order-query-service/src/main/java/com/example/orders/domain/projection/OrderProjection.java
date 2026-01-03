package com.example.orders.domain.projection;

import com.example.orders.contracts.common.OrderStatus;
import com.example.orders.contracts.event.OrderCancelledEvent;
import com.example.orders.contracts.event.OrderItemAddedEvent;
import com.example.orders.contracts.event.OrderPlacedEvent;
import com.example.orders.contracts.event.OrderShippedEvent;
import com.example.orders.contracts.event.PaymentConfirmedEvent;
import com.example.orders.domain.readmodel.OrderItemEntity;
import com.example.orders.domain.readmodel.OrderItemId;
import com.example.orders.domain.readmodel.OrderItemRepository;
import com.example.orders.domain.readmodel.OrderSummaryEntity;
import com.example.orders.domain.readmodel.OrderSummaryRepository;
import java.time.Instant;
import java.util.Optional;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ProcessingGroup("order-projection")
public class OrderProjection {
  private final OrderSummaryRepository summaryRepository;
  private final OrderItemRepository itemRepository;

  public OrderProjection(OrderSummaryRepository summaryRepository, OrderItemRepository itemRepository) {
    this.summaryRepository = summaryRepository;
    this.itemRepository = itemRepository;
  }

  @EventHandler
  @Transactional
  public void on(OrderPlacedEvent event) {
    OrderSummaryEntity summary = new OrderSummaryEntity(
        event.orderId(),
        event.customerId(),
        OrderStatus.PLACED,
        0,
        Instant.now()
    );
    summaryRepository.save(summary);
  }

  @EventHandler
  @Transactional
  public void on(OrderItemAddedEvent event) {
    Optional<OrderSummaryEntity> summaryOptional = summaryRepository.findById(event.orderId());
    if (summaryOptional.isEmpty()) {
      return;
    }
    OrderSummaryEntity summary = summaryOptional.get();

    OrderItemId itemId = new OrderItemId(event.orderId(), event.sku());
    OrderItemEntity item = itemRepository.findById(itemId)
        .map(existing -> {
          existing.setQty(existing.getQty() + event.qty());
          return existing;
        })
        .orElseGet(() -> new OrderItemEntity(itemId, event.qty()));
    itemRepository.save(item);

    summary.setItemCount(summary.getItemCount() + event.qty());
    summary.setLastUpdated(Instant.now());
    summaryRepository.save(summary);
  }

  @EventHandler
  @Transactional
  public void on(PaymentConfirmedEvent event) {
    updateStatus(event.orderId(), OrderStatus.PAID);
  }

  @EventHandler
  @Transactional
  public void on(OrderCancelledEvent event) {
    updateStatus(event.orderId(), OrderStatus.CANCELLED);
  }

  @EventHandler
  @Transactional
  public void on(OrderShippedEvent event) {
    updateStatus(event.orderId(), OrderStatus.SHIPPED);
  }

  @ResetHandler
  @Transactional
  public void onReset() {
    itemRepository.deleteAll();
    summaryRepository.deleteAll();
  }

  private void updateStatus(String orderId, OrderStatus status) {
    summaryRepository.findById(orderId).ifPresent(summary -> {
      summary.setStatus(status);
      summary.setLastUpdated(Instant.now());
      summaryRepository.save(summary);
    });
  }
}
