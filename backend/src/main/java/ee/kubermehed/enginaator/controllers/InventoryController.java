package ee.kubermehed.enginaator.controllers;

import ee.kubermehed.enginaator.dtos.InventoryItemDto;
import ee.kubermehed.enginaator.dtos.InventoryViewDto;
import ee.kubermehed.enginaator.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryViewDto>> getInventory() {
        return ResponseEntity.ok(inventoryService.getInventory());
    }

    @PostMapping("/item")
    public ResponseEntity<Void> addItem(@RequestBody InventoryItemDto dto) {
        inventoryService.addItem(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/item/{itemId}")
    public ResponseEntity<Void> restock(@PathVariable UUID itemId,
                                        @RequestBody InventoryItemDto dto) {
        inventoryService.update(itemId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID itemId) {
        inventoryService.deleteItem(itemId);
        return ResponseEntity.ok().build();
    }
}