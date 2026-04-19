package ee.kubermehed.enginaator.dtos;

import ee.kubermehed.enginaator.enums.RequestStatus;
import ee.kubermehed.enginaator.models.GuestRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Full guest's request, with all data.
 */
@Getter
@Setter
@NoArgsConstructor
public class RequestViewDto {
    private UUID id;
    private String roomNumber;
    private String text;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RequestItemViewDto> requestItems;

    public static RequestViewDto fromEntity(GuestRequest guestRequest) {
        RequestViewDto viewDTO = new RequestViewDto();
        viewDTO.setId(guestRequest.getId());
        viewDTO.setRoomNumber(guestRequest.getRoomNumber());
        viewDTO.setText(guestRequest.getRequestText());
        viewDTO.setStatus(guestRequest.getStatus());
        viewDTO.setCreatedAt(guestRequest.getCreatedAt());
        viewDTO.setUpdatedAt(guestRequest.getUpdatedAt());

        viewDTO.setRequestItems(
                guestRequest.getRequestItems().stream()
                        .map(RequestItemViewDto::fromEntity)
                        .toList()
        );

        return viewDTO;
    }
}