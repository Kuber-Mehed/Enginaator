package ee.kubermehed.enginaator.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "quantity_requested", nullable = false)
    private int quantityRequested;

    @Column(name = "quantity_fulfilled", nullable = false)
    private int quantityFulfilled = 0;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_request_id", nullable = false)
    private GuestRequest guestRequest;
}