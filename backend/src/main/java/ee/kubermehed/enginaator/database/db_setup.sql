CREATE TYPE request_status AS ENUM (
    'RECEIVED',
    'IN_PROGRESS',
    'DELIVERED',
    'REJECTED',
    'CANCELLED'
    );

CREATE TYPE transaction_type AS ENUM (
    'RESERVATION',
    'RELEASE_RESERVATION',
    'FULFILLMENT',
    'RESTOCK',
    'ADJUSTMENT',
    'RECONCILIATION'
    );

CREATE TYPE discrepancy_reason AS ENUM (
    'DAMAGED',
    'THEFT',
    'MISCOUNTED',
    'SUPPLIER_ERROR'
    );

-- =========================
-- INVENTORY
-- =========================

CREATE TABLE inventory_item
(
    id                  UUID PRIMARY KEY,

    name                VARCHAR(255) NOT NULL UNIQUE,
    category            VARCHAR(100),
    unit                VARCHAR(50),

    quantity_in_stock   INTEGER      NOT NULL CHECK (quantity_in_stock >= 0),
    quantity_reserved   INTEGER      NOT NULL DEFAULT 0 CHECK (quantity_reserved >= 0),

    low_stock_threshold INTEGER      NOT NULL DEFAULT 0 CHECK (low_stock_threshold >= 0)
);

CREATE INDEX idx_inventory_name ON inventory_item (name);

-- =========================
-- service_request
-- =========================

CREATE TABLE service_request
(
    id            UUID PRIMARY KEY,

    room_number   VARCHAR(20)    NOT NULL,

    status        request_status NOT NULL DEFAULT 'RECEIVED',

    created_at    TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_service_request_room ON service_request (room_number);
CREATE INDEX idx_service_request_status ON service_request (status);
CREATE INDEX idx_service_request_created_at ON service_request (created_at DESC);

-- =========================
-- REQUEST ITEMS (LINE ITEMS)
-- =========================

CREATE TABLE request_item
(
    id                 UUID PRIMARY KEY,

    service_request_id UUID    NOT NULL,
    inventory_item_id  UUID    NOT NULL,

    quantity           INTEGER NOT NULL CHECK (quantity > 0),

    CONSTRAINT fk_service_request_item_request
        FOREIGN KEY (service_request_id)
            REFERENCES service_request (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_request_item_inventory
        FOREIGN KEY (inventory_item_id)
            REFERENCES inventory_item (id)
            ON DELETE RESTRICT
);

CREATE INDEX idx_request_item_request ON request_item (service_request_id);

-- =========================
-- TRANSACTION LOG
-- =========================

CREATE TABLE transaction_log
(
    id                UUID PRIMARY KEY,

    inventory_item_id UUID             NOT NULL,

    type              transaction_type NOT NULL,

    quantity_change   INTEGER          NOT NULL,
    resulting_stock   INTEGER          NOT NULL,

    timestamp         TIMESTAMP        NOT NULL DEFAULT NOW(),

    reference         VARCHAR(255),

    CONSTRAINT fk_transaction_inventory
        FOREIGN KEY (inventory_item_id)
            REFERENCES inventory_item (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_transaction_item ON transaction_log (inventory_item_id);
CREATE INDEX idx_transaction_time ON transaction_log (timestamp DESC);

-- =========================
-- RECONCILIATION
-- =========================

CREATE TABLE reconciliation
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================
-- RECONCILIATION ITEMS
-- =========================
CREATE TABLE reconciliation_item
(
    id                UUID PRIMARY KEY,

    reconciliation_id UUID               NOT NULL,
    inventory_item_id UUID               NOT NULL,

    expected_count    INTEGER            NOT NULL CHECK (expected_count >= 0),
    physical_count    INTEGER            NOT NULL CHECK (physical_count >= 0),

    discrepancy       INTEGER            NOT NULL,

    reason            discrepancy_reason NOT NULL,

    CONSTRAINT fk_recon_item_reconciliation
        FOREIGN KEY (reconciliation_id)
            REFERENCES reconciliation (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_recon_item_inventory
        FOREIGN KEY (inventory_item_id)
            REFERENCES inventory_item (id)
            ON DELETE RESTRICT
);

CREATE INDEX idx_recon_item_recon ON reconciliation_item (reconciliation_id);
