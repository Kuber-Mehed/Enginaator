package ee.kubermehed.enginaator.config;

import ee.kubermehed.enginaator.models.InventoryItem;
import ee.kubermehed.enginaator.repositories.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final InventoryItemRepository inventoryItemRepository;

    @Override
    public void run(String... args) {
        seedInventory();
    }

    private void seedInventory() {
        if (inventoryItemRepository.count() > 0) {
            return;
        }

        saveInventoryItem("Bath Towel", "housekeeping", "piece", 50, 10);
        saveInventoryItem("Hand Towel", "housekeeping", "piece", 40, 8);
        saveInventoryItem("Bathrobe", "housekeeping", "piece", 15, 3);
        saveInventoryItem("Pillow", "housekeeping", "piece", 20, 5);
        saveInventoryItem("Blanket", "housekeeping", "piece", 15, 3);
        saveInventoryItem("Iron", "housekeeping", "piece", 5, 2);
        saveInventoryItem("Ironing Board", "housekeeping", "piece", 5, 2);
        saveInventoryItem("Bottled Water", "room_service", "bottle", 100, 20);
        saveInventoryItem("Coffee Pod", "room_service", "piece", 80, 15);
        saveInventoryItem("Tea Bag", "room_service", "piece", 60, 10);
        saveInventoryItem("Club Sandwich", "room_service", "piece", 10, 3);
        saveInventoryItem("Shampoo", "housekeeping", "bottle", 30, 5);
        saveInventoryItem("Toothbrush Kit", "housekeeping", "piece", 25, 5);
        saveInventoryItem("Light Bulb", "maintenance", "piece", 20, 5);
        saveInventoryItem("Power Adapter", "maintenance", "piece", 8, 2);
    }

    private void saveInventoryItem(
            String name,
            String category,
            String unit,
            int quantityInStock,
            int lowStockThreshold
    ) {
        InventoryItem item = new InventoryItem();
        item.setName(name.toLowerCase());
        item.setCategory(category);
        item.setUnit(unit);
        item.setQuantityInStock(quantityInStock);
        item.setQuantityReserved(0);
        item.setLowStockThreshold(lowStockThreshold);
        inventoryItemRepository.save(item);
    }
}