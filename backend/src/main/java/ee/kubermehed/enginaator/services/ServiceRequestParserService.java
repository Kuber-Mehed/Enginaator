package ee.kubermehed.enginaator.services;

import ee.kubermehed.enginaator.dtos.ParsedItemDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ServiceRequestParserService {

    public List<ParsedItemDTO> parseServiceRequest(MultipartFile file) {
        // TODO:
        // 1. Convert incoming voice file into transcript text.
        // 2. Parse transcript into normalized inventory requests.
        // 3. Return item names that match inventory naming conventions.
        // 4. Quantity must be positive and validated before returning.
        // 5. Do NOT trust AI output directly; final validation happens in service layer.
        return List.of();
    }

    public List<ParsedItemDTO> parseServiceRequestText(String requestText) {
        // TODO:
        // Text-only placeholder for manual guest request flow.
        // This should reuse the same parsing pipeline as voice requests
        // after speech-to-text is complete.
        return List.of();
    }
}