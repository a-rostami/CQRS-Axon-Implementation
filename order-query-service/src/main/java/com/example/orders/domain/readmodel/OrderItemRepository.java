package com.example.orders.domain.readmodel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, OrderItemId> {
  List<OrderItemEntity> findByIdOrderId(String orderId);
}
