package ee.kubermehed.enginaator.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestItemViewDTO {
    private String itemName;
    private int quantity;
}
