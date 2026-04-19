package ee.kubermehed.enginaator.services;

import ee.kubermehed.enginaator.dtos.InventoryItemDto;
import ee.kubermehed.enginaator.dtos.InventoryViewDto;
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

    public List<InventoryViewDto> getInventory() {
        return inventoryItemRepository.findAllByOrderByName().stream().map(InventoryViewDto::fromEntity).toList();
    }

    public void addItem(InventoryItemDto item) {
        InventoryItem newItem = InventoryItemDto.toEntity(item);
        inventoryItemRepository.save(newItem);
    }

    public void update(UUID itemId, InventoryItemDto item) {
        InventoryItem updatedItem = InventoryItemDto.toEntity(item);
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
