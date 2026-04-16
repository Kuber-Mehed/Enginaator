package ee.kubermehed.enginaator.models;

import ee.kubermehed.enginaator.enums.TransactionType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    private UUID itemId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private int quantityChange;

    private int resultingStock;

    private LocalDateTime timestamp;

    private String reference; // requestId, reconciliationId, etc.
}