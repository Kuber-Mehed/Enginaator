package ee.kubermehed.enginaator.dtos;

import ee.kubermehed.enginaator.models.InventoryItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Inventory item, that client receives.
 */
@Getter
@Setter
@NoArgsConstructor
public class InventoryViewDto {
    private UUID id;
    private String name;
    private int quantityInStock;
    private int quantityReserved;
    private int quantityAvailable;
    private int lowStockThreshold;

    public static InventoryViewDto fromEntity(InventoryItem item) {
        InventoryViewDto dto = new InventoryViewDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setQuantityInStock(item.getQuantityInStock());
        dto.setQuantityReserved(item.getQuantityReserved());
        dto.setQuantityAvailable(item.getQuantityInStock() - item.getQuantityReserved());
        dto.setLowStockThreshold(item.getLowStockThreshold());
        return dto;
    }
}