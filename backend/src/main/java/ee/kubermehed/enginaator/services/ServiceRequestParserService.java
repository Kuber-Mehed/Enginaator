package ee.kubermehed.enginaator.services;

import ee.kubermehed.enginaator.dtos.ParsedItemDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ServiceRequestParserService {

    public List<ParsedItemDTO> parseServiceRequest(MultipartFile file) {
        return List.of();
    }
}
