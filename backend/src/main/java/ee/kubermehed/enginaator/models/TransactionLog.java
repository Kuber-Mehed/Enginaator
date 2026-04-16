package ee.kubermehed.enginaator.models;

import ee.kubermehed.enginaator.enums.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
class TransactionLog {

    @Id
    private UUID id;

    private UUID itemId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private int quantityChange;

    private int resultingStock;

    private LocalDateTime timestamp;

    private String reference; // requestId, reconciliationId, etc.
}