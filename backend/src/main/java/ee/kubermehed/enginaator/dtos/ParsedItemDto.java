package ee.kubermehed.enginaator.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item needed for the AI parsing result.
 */
@Getter
@Setter
@NoArgsConstructor
public class ParsedItemDto {
    private String itemName;
    private int quantity;
}