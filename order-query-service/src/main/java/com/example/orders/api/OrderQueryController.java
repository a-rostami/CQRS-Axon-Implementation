package com.example.orders.api;

import com.example.orders.application.OrderQueryService;
import com.example.orders.contracts.query.OrderDetails;
import com.example.orders.contracts.query.OrderSummary;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class OrderQueryController {
  private final OrderQueryService orderQueryService;

  public OrderQueryController(OrderQueryService orderQueryService) {
    this.orderQueryService = orderQueryService;
  }

  @GetMapping("/orders/{id}")
  public ResponseEntity<OrderDetails> getOrder(@PathVariable("id") String orderId) {
    return ResponseEntity.ok(orderQueryService.getOrder(orderId));
  }

  @GetMapping("/customers/{customerId}/orders")
  public ResponseEntity<List<OrderSummary>> getCustomerOrders(@PathVariable String customerId) {
    return ResponseEntity.ok(orderQueryService.getOrdersForCustomer(customerId));
  }
}
