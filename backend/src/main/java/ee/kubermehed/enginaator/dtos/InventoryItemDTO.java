package ee.kubermehed.enginaator.dtos;

import ee.kubermehed.enginaator.enums.InventoryItemCategory;
import ee.kubermehed.enginaator.models.InventoryItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class InventoryItemDTO {
    private UUID id;
    private String name;
    private InventoryItemCategory category;
    private int quantityInStock;
    private String unit;
    private int lowStockThreshold;

    public static InventoryItem toEntity(InventoryItemDTO dto) {
        InventoryItem item = new InventoryItem();
        item.setName(dto.getName());
        item.setCategory(dto.getCategory().name());
        item.setQuantityInStock(dto.getQuantityInStock());
        item.setUnit(dto.getUnit());
        item.setLowStockThreshold(dto.getLowStockThreshold());
        return item;
    }
}
