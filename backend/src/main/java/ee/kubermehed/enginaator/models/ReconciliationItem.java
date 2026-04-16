package ee.kubermehed.enginaator.models;

import ee.kubermehed.enginaator.enums.DiscrepancyReason;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
class ReconciliationItem {
    @Id
    private UUID id;

    @ManyToOne
    private Reconciliation reconciliation;

    @ManyToOne
    private InventoryItem item;

    private int expectedCount;
    private int physicalCount;
    private int discrepancy;

    @Enumerated(EnumType.STRING)
    private DiscrepancyReason reason;
}