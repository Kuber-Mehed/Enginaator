package ee.kubermehed.enginaator.dtos;

import ee.kubermehed.enginaator.enums.RequestStatus;
import ee.kubermehed.enginaator.models.ServiceRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class RequestViewDTO {
    private UUID id;
    private String roomNumber;

    // TODO:
    // Expose the original guest request text here.
    // Staff dashboard must be able to show raw request text even if AI parsing is incomplete.
    private String text;

    private RequestStatus status;
    private LocalDateTime createdAt;

    // TODO:
    // Add updatedAt when entity supports it.
    private LocalDateTime updatedAt;

    private List<RequestItemViewDTO> requestItems;

    public static RequestViewDTO fromEntity(ServiceRequest serviceRequest) {
        RequestViewDTO viewDTO = new RequestViewDTO();
        viewDTO.setId(serviceRequest.getId());
        viewDTO.setRoomNumber(serviceRequest.getRoomNumber());

        // TODO:
        // Replace null with serviceRequest.getText() after entity field is added.
        viewDTO.setText(null);

        viewDTO.setStatus(serviceRequest.getStatus());
        viewDTO.setCreatedAt(serviceRequest.getCreatedAt());

        // TODO:
        // Replace null with real updatedAt field after entity supports it.
        viewDTO.setUpdatedAt(null);

        viewDTO.setRequestItems(serviceRequest.getRequestItems().stream().map(requestItem -> {
            RequestItemViewDTO itemViewDTO = new RequestItemViewDTO();
            itemViewDTO.setItemName(requestItem.getItem().getName());
            itemViewDTO.setQuantity(requestItem.getQuantity());
            return itemViewDTO;
        }).toList());
        return viewDTO;
    }
}