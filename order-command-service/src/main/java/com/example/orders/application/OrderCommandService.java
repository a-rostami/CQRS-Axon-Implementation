package com.example.orders.application;

import com.example.orders.contracts.command.AddOrderItemCommand;
import com.example.orders.contracts.command.CancelOrderCommand;
import com.example.orders.contracts.command.ConfirmPaymentCommand;
import com.example.orders.contracts.command.PlaceOrderCommand;
import com.example.orders.contracts.command.ShipOrderCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

@Service
public class OrderCommandService {
  private final CommandGateway commandGateway;

  public OrderCommandService(CommandGateway commandGateway) {
    this.commandGateway = commandGateway;
  }

  public void placeOrder(String orderId, String customerId) {
    commandGateway.sendAndWait(new PlaceOrderCommand(orderId, customerId));
  }

  public void addItem(String orderId, String sku, int qty) {
    commandGateway.sendAndWait(new AddOrderItemCommand(orderId, sku, qty));
  }

  public void confirmPayment(String orderId, String paymentId) {
    commandGateway.sendAndWait(new ConfirmPaymentCommand(orderId, paymentId));
  }

  public void cancelOrder(String orderId, String reason) {
    commandGateway.sendAndWait(new CancelOrderCommand(orderId, reason));
  }

  public void shipOrder(String orderId, String shipmentId) {
    commandGateway.sendAndWait(new ShipOrderCommand(orderId, shipmentId));
  }
}
