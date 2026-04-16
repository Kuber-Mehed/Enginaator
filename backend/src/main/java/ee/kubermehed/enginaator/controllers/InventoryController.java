package ee.kubermehed.enginaator.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryViewDTO> getInventory() {
        return inventoryService.getInventory();
    }

    @PostMapping("/restock")
    public void restock(@RequestBody RestockDTO dto) {
        inventoryService.restock(dto);
    }

    @PostMapping("/item")
    public InventoryItemDTO addItem(@RequestBody CreateItemDTO dto) {
        return inventoryService.addItem(dto);
    }

    @DeleteMapping("/item/{id}")
    public void deleteItem(@PathVariable Long id) {
        inventoryService.deleteItem(id);
    }
}