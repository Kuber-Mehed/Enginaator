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
    private RequestStatus status;
    private LocalDateTime createdAt;
    private List<RequestItemViewDTO> requestItems;

    public static RequestViewDTO fromEntity(ServiceRequest serviceRequest) {
        RequestViewDTO viewDTO = new RequestViewDTO();
        viewDTO.setId(serviceRequest.getId());
        viewDTO.setRoomNumber(serviceRequest.getRoomNumber());
        viewDTO.setStatus(serviceRequest.getStatus());
        viewDTO.setCreatedAt(serviceRequest.getCreatedAt());
        viewDTO.setRequestItems(serviceRequest.getRequestItems().stream().map(requestItem -> {
            RequestItemViewDTO itemViewDTO = new RequestItemViewDTO();
            itemViewDTO.setItemName(requestItem.getItem().getName());
            itemViewDTO.setQuantity(requestItem.getQuantity());
            return itemViewDTO;
        }).toList());
        return viewDTO;
    }
}