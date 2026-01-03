package com.example.orders.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.orders.contracts.common.OrderStatus;
import com.example.orders.contracts.event.OrderItemAddedEvent;
import com.example.orders.contracts.event.OrderPlacedEvent;
import com.example.orders.contracts.event.PaymentConfirmedEvent;
import com.example.orders.contracts.query.OrderDetails;
import com.example.orders.contracts.query.OrderSummary;
import com.example.orders.domain.readmodel.OrderSummaryRepository;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.wait.strategy.Wait;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureRestTestClient
class OrderQueryIntegrationTest {
  @Container
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
      .withDatabaseName("orders")
      .withUsername("orders")
      .withPassword("orders");

  @Container
  static final GenericContainer<?> axonServer = new GenericContainer<>(
      DockerImageName.parse("axoniq/axonserver:latest"))
      .withExposedPorts(8024, 8124)
      .withEnv("JAVA_TOOL_OPTIONS", "-Daxoniq.axonserver.standalone=true")
      .waitingFor(Wait.forHttp("/actuator/health").forPort(8024).forStatusCode(200))
      .withStartupTimeout(Duration.ofMinutes(2));

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    if (!postgres.isRunning()) {
      postgres.start();
    }
    if (!axonServer.isRunning()) {
      axonServer.start();
    }
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> "false");
    registry.add("axon.axonserver.servers",
        () -> axonServer.getHost() + ":" + axonServer.getMappedPort(8124));
  }

  @Autowired
  private EventGateway eventGateway;

  @Autowired
  private RestTestClient restTestClient;

  @Autowired
  private OrderSummaryRepository summaryRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void ensureSchema() {
    jdbcTemplate.execute("""
        CREATE TABLE IF NOT EXISTS order_summary (
          order_id VARCHAR(64) PRIMARY KEY,
          customer_id VARCHAR(64) NOT NULL,
          status VARCHAR(32) NOT NULL,
          item_count INT NOT NULL,
          last_updated TIMESTAMPTZ NOT NULL
        )
        """);
    jdbcTemplate.execute(
        "CREATE INDEX IF NOT EXISTS idx_order_summary_customer_id ON order_summary(customer_id)");
    jdbcTemplate.execute("""
        CREATE TABLE IF NOT EXISTS order_item (
          order_id VARCHAR(64) NOT NULL,
          sku VARCHAR(64) NOT NULL,
          qty INT NOT NULL,
          PRIMARY KEY (order_id, sku),
          CONSTRAINT fk_order_item_summary FOREIGN KEY (order_id) REFERENCES order_summary(order_id)
        )
        """);
    jdbcTemplate.execute("""
        CREATE TABLE IF NOT EXISTS token_entry (
          processor_name VARCHAR(255) NOT NULL,
          segment INT NOT NULL,
          token BYTEA NULL,
          token_type VARCHAR(255) NULL,
          timestamp VARCHAR(255) NULL,
          owner VARCHAR(255) NULL,
          PRIMARY KEY (processor_name, segment)
        )
        """);
    jdbcTemplate.update("DELETE FROM order_item");
    jdbcTemplate.update("DELETE FROM order_summary");
    jdbcTemplate.update("DELETE FROM token_entry");
  }

  @Test
  void publishesEvents_updatesReadModel_andServesQueries() {
    String orderId = UUID.randomUUID().toString();
    String customerId = "cust-1";

    eventGateway.publish(new OrderPlacedEvent(orderId, customerId));
    eventGateway.publish(new OrderItemAddedEvent(orderId, "SKU-1", 2));
    eventGateway.publish(new PaymentConfirmedEvent(orderId, "pay-1"));

    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
      var summary = summaryRepository.findById(orderId).orElseThrow();
      assertThat(summary.getStatus()).isEqualTo(OrderStatus.PAID);
      assertThat(summary.getItemCount()).isEqualTo(2);
    });

    var detailsResponse = restTestClient.get()
        .uri("/orders/{orderId}", orderId)
        .exchange();
    detailsResponse.expectStatus().isOk();
    var details = detailsResponse.expectBody(OrderDetails.class)
        .returnResult()
        .getResponseBody();
    assertThat(details).isNotNull();
    assertThat(details.itemCount()).isEqualTo(2);

    var listResponse = restTestClient.get()
        .uri("/customers/{customerId}/orders", customerId)
        .exchange();
    listResponse.expectStatus().isOk();
    var summaries = listResponse.expectBody(new ParameterizedTypeReference<List<OrderSummary>>() {
    })
        .returnResult()
        .getResponseBody();
    assertThat(summaries).isNotNull();
    assertThat(summaries).hasSize(1);
    assertThat(summaries.getFirst().orderId()).isEqualTo(orderId);
  }
}
