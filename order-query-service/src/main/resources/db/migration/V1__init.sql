CREATE TABLE order_summary (
  order_id VARCHAR(64) PRIMARY KEY,
  customer_id VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  item_count INT NOT NULL,
  last_updated TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_order_summary_customer_id ON order_summary(customer_id);

CREATE TABLE order_item (
  order_id VARCHAR(64) NOT NULL,
  sku VARCHAR(64) NOT NULL,
  qty INT NOT NULL,
  PRIMARY KEY (order_id, sku),
  CONSTRAINT fk_order_item_summary FOREIGN KEY (order_id) REFERENCES order_summary(order_id)
);

CREATE TABLE token_entry (
  processor_name VARCHAR(255) NOT NULL,
  segment INT NOT NULL,
  token BYTEA NULL,
  token_type VARCHAR(255) NULL,
  timestamp VARCHAR(255) NULL,
  owner VARCHAR(255) NULL,
  PRIMARY KEY (processor_name, segment)
);
