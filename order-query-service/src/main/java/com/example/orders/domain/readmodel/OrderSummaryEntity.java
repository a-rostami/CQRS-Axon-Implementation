package com.example.orders.domain.readmodel;

import com.example.orders.contracts.common.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "order_summary")
public class OrderSummaryEntity {
  @Id
  @Column(name = "order_id", nullable = false, length = 64)
  private String orderId;

  @Column(name = "customer_id", nullable = false, length = 64)
  private String customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private OrderStatus status;

  @Column(name = "item_count", nullable = false)
  private int itemCount;

  @Column(name = "last_updated", nullable = false)
  private Instant lastUpdated;

  protected OrderSummaryEntity() {
  }

  public OrderSummaryEntity(String orderId, String customerId, OrderStatus status, int itemCount, Instant lastUpdated) {
    this.orderId = orderId;
    this.customerId = customerId;
    this.status = status;
    this.itemCount = itemCount;
    this.lastUpdated = lastUpdated;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
  }

  public int getItemCount() {
    return itemCount;
  }

  public void setItemCount(int itemCount) {
    this.itemCount = itemCount;
  }

  public Instant getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Instant lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
}
