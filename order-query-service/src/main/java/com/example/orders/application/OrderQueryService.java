package com.example.orders.application;

import com.example.orders.contracts.query.OrderDetails;
import com.example.orders.contracts.query.OrderItemDto;
import com.example.orders.contracts.query.OrderSummary;
import com.example.orders.domain.readmodel.OrderItemRepository;
import com.example.orders.domain.readmodel.OrderSummaryEntity;
import com.example.orders.domain.readmodel.OrderSummaryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderQueryService {
  private final OrderSummaryRepository summaryRepository;
  private final OrderItemRepository itemRepository;

  public OrderQueryService(OrderSummaryRepository summaryRepository, OrderItemRepository itemRepository) {
    this.summaryRepository = summaryRepository;
    this.itemRepository = itemRepository;
  }

  public OrderDetails getOrder(String orderId) {
    OrderSummaryEntity summary = summaryRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    List<OrderItemDto> items = itemRepository.findByIdOrderId(orderId).stream()
        .map(item -> new OrderItemDto(item.getId().getSku(), item.getQty()))
        .toList();

    return new OrderDetails(
        summary.getOrderId(),
        summary.getCustomerId(),
        summary.getStatus(),
        summary.getItemCount(),
        items
    );
  }

  public List<OrderSummary> getOrdersForCustomer(String customerId) {
    return summaryRepository.findByCustomerIdOrderByLastUpdatedDesc(customerId).stream()
        .map(summary -> new OrderSummary(
            summary.getOrderId(),
            summary.getCustomerId(),
            summary.getStatus(),
            summary.getItemCount(),
            summary.getLastUpdated()
        ))
        .toList();
  }
}
