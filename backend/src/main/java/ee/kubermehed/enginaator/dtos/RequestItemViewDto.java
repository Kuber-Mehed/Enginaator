package ee.kubermehed.enginaator.dtos;

import ee.kubermehed.enginaator.models.RequestItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item and quantity, that guest requests.
 */
@Getter
@Setter
@NoArgsConstructor
public class RequestItemViewDto {
    private String itemName;
    private int quantityRequested;
    private int quantityFulfilled;

    public static RequestItemViewDto fromEntity(RequestItem requestItem) {
        RequestItemViewDto dto = new RequestItemViewDto();
        dto.setItemName(requestItem.getInventoryItem().getName());
        dto.setQuantityRequested(requestItem.getQuantityRequested());
        dto.setQuantityFulfilled(requestItem.getQuantityFulfilled());
        return dto;
    }
}
