package ee.kubermehed.enginaator.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
class InventoryItem {

    @Id
    private UUID id;

    private String name;

    private String category;

    private String unit;

    private int quantityInStock;

    private int quantityReserved;

    private int lowStockThreshold;
}