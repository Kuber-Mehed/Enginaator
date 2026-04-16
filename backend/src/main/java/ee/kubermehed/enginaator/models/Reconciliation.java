package ee.kubermehed.enginaator.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
class Reconciliation {

    @Id
    private UUID id;

    private LocalDateTime date;
}