package com.example.orders.domain.readmodel;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_item")
public class OrderItemEntity {
  @EmbeddedId
  private OrderItemId id;

  @Column(name = "qty", nullable = false)
  private int qty;

  protected OrderItemEntity() {
  }

  public OrderItemEntity(OrderItemId id, int qty) {
    this.id = id;
    this.qty = qty;
  }

  public OrderItemId getId() {
    return id;
  }

  public int getQty() {
    return qty;
  }

  public void setQty(int qty) {
    this.qty = qty;
  }
}
