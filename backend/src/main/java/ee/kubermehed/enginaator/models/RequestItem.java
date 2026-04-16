package ee.kubermehed.enginaator.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
class RequestItem {

    @Id
    private UUID id;

    @ManyToOne
    private ServiceRequest serviceRequest;

    @ManyToOne
    private InventoryItem item;

    private int quantity;
}