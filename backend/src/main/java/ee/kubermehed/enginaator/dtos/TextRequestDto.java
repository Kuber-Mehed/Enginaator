package ee.kubermehed.enginaator.dtos;

import lombok.Getter;
import lombok.Setter;

/**
 *  Handle text, received from guest.
 */
@Getter
@Setter
public class TextRequestDto {
    private String requestText;
}