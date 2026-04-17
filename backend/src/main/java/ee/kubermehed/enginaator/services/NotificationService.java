package ee.kubermehed.enginaator.services;

import ee.kubermehed.enginaator.dtos.InventoryViewDTO;
import ee.kubermehed.enginaator.dtos.RequestViewDTO;
import ee.kubermehed.enginaator.models.ServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate template;

    public void sendToStaff(RequestViewDTO newRequest) {
        template.convertAndSend("/topic/staff", newRequest);
    }

    public void sendToInventory(InventoryViewDTO updatedInventoryItem) {
        template.convertAndSend("/topic/inventory", updatedInventoryItem);
    }

    public void sendToRoom(String roomNumber, Object payload) {
        template.convertAndSend("/topic/room/" + roomNumber, payload);
    }
}