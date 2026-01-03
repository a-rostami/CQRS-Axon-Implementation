package com.example.orders.domain.readmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderItemId implements Serializable {
  @Column(name = "order_id", nullable = false, length = 64)
  private String orderId;

  @Column(name = "sku", nullable = false, length = 64)
  private String sku;

  protected OrderItemId() {
  }

  public OrderItemId(String orderId, String sku) {
    this.orderId = orderId;
    this.sku = sku;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getSku() {
    return sku;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrderItemId that = (OrderItemId) o;
    return Objects.equals(orderId, that.orderId) && Objects.equals(sku, that.sku);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderId, sku);
  }
}
