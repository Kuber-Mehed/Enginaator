package ee.kubermehed.enginaator.services;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate template;

    public void sendToStaff(Object payload) {
        template.convertAndSend("/topic/staff", payload);
    }

    public void sendToRoom(String roomNumber, Object payload) {
        template.convertAndSend("/topic/room/" + roomNumber, payload);
    }
}