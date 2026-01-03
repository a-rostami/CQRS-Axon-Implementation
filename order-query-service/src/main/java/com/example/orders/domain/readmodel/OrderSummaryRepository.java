package com.example.orders.domain.readmodel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSummaryRepository extends JpaRepository<OrderSummaryEntity, String> {
  List<OrderSummaryEntity> findByCustomerIdOrderByLastUpdatedDesc(String customerId);
}
