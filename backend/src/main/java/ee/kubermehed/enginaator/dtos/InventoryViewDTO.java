package ee.kubermehed.enginaator.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryViewDTO {
    private String name;
    private int quantityInStock;
    private int quantityReserved;
    private int quantityAvailable;
    private int lowStockThreshold;
}