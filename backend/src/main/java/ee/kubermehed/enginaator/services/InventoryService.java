package ee.kubermehed.enginaator.services;

import ee.kubermehed.enginaator.dtos.InventoryItemDTO;
import ee.kubermehed.enginaator.dtos.InventoryViewDTO;
import ee.kubermehed.enginaator.models.InventoryItem;
import ee.kubermehed.enginaator.repositories.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    public List<InventoryViewDTO> getInventory() {
        return List.of();
    }

    public void addItem(InventoryItemDTO item) {
        InventoryItem newItem = InventoryItemDTO.toEntity(item);
        inventoryItemRepository.save(newItem);
    }

    public void update(UUID itemId, InventoryItemDTO item) {
        InventoryItem updatedItem = InventoryItemDTO.toEntity(item);
        InventoryItem itemToUpdate = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        itemToUpdate.setName(updatedItem.getName());
        itemToUpdate.setCategory(updatedItem.getCategory());
        itemToUpdate.setQuantityInStock(updatedItem.getQuantityInStock());
        itemToUpdate.setUnit(updatedItem.getUnit());
        itemToUpdate.setLowStockThreshold(updatedItem.getLowStockThreshold());

        inventoryItemRepository.save(itemToUpdate);
    }

    public void deleteItem(UUID itemId) {
        inventoryItemRepository.deleteById(itemId);
    }
}
