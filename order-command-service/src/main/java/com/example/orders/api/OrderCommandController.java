package com.example.orders.api;

import com.example.orders.application.OrderCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@Validated
public class OrderCommandController {
  private final OrderCommandService orderCommandService;

  public OrderCommandController(OrderCommandService orderCommandService) {
    this.orderCommandService = orderCommandService;
  }

  @PostMapping
  public ResponseEntity<OrderIdResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
    orderCommandService.placeOrder(request.orderId(), request.customerId());
    return ResponseEntity.status(HttpStatus.CREATED).body(new OrderIdResponse(request.orderId()));
  }

  @PostMapping("/{id}/items")
  public ResponseEntity<Void> addItem(@PathVariable("id") String orderId,
                                      @Valid @RequestBody AddOrderItemRequest request) {
    orderCommandService.addItem(orderId, request.sku(), request.qty());
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/{id}/payment/confirm")
  public ResponseEntity<Void> confirmPayment(@PathVariable("id") String orderId,
                                             @Valid @RequestBody ConfirmPaymentRequest request) {
    orderCommandService.confirmPayment(orderId, request.paymentId());
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<Void> cancelOrder(@PathVariable("id") String orderId,
                                          @Valid @RequestBody CancelOrderRequest request) {
    orderCommandService.cancelOrder(orderId, request.reason());
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/{id}/ship")
  public ResponseEntity<Void> shipOrder(@PathVariable("id") String orderId,
                                        @Valid @RequestBody ShipOrderRequest request) {
    orderCommandService.shipOrder(orderId, request.shipmentId());
    return ResponseEntity.accepted().build();
  }

  public record PlaceOrderRequest(@NotBlank String orderId, @NotBlank String customerId) {
  }

  public record AddOrderItemRequest(@NotBlank String sku, @Positive int qty) {
  }

  public record ConfirmPaymentRequest(@NotBlank String paymentId) {
  }

  public record CancelOrderRequest(@NotBlank String reason) {
  }

  public record ShipOrderRequest(@NotBlank String shipmentId) {
  }

  public record OrderIdResponse(String orderId) {
  }
}
