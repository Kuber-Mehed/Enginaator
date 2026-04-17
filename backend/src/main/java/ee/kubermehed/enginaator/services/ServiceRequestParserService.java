package ee.kubermehed.enginaator.services;

import ee.kubermehed.enginaator.dtos.ParsedItemDTO;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class ServiceRequestParserService {

    private static final String AI_PARSING_SERVICE_URL = "http://localhost:8000/";

    private final WebClient webClient;

    public ServiceRequestParserService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl(AI_PARSING_SERVICE_URL).build();
    }

    public List<ParsedItemDTO> parseServiceRequest(MultipartFile file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());

        try {
            String rawResponse = webClient.post()
                    .uri("/process-audio/")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()

                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            response.bodyToMono(String.class)
                                    .map(errorBody -> new RuntimeException("Client error: " + errorBody))
                    )

                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            response.bodyToMono(String.class)
                                    .map(errorBody -> new RuntimeException("Server error: " + errorBody))
                    )

                    .bodyToMono(String.class)
                    .block();

            if (rawResponse == null) {
                throw new RuntimeException("Empty response from API");
            }

            String json = rawResponse;

            json = json.replace("```json", "")
                    .replace("```", "")
                    .trim();

            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = json.substring(1, json.length() - 1);
            }

            json = json.replace("\\[", "[")
                    .replace("\\]", "]")
                    .replace("\\\"", "\"")
                    .replace("\\n", "")
                    .trim();

            int start = json.indexOf('[');
            int end = json.lastIndexOf(']');

            if (start != -1 && end != -1) {
                json = json.substring(start, end + 1);
            }

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(
                    json,
                    new TypeReference<List<ParsedItemDTO>>() {}
            );

        } catch (WebClientResponseException e) {
            throw new RuntimeException(
                    "API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);

        } catch (Exception e) {
            throw new RuntimeException("Failed to call external API", e);
        }
    }
    public List<ParsedItemDTO> parseServiceRequestText(String requestText) {
        // TODO:
        // Text-only placeholder for manual guest request flow.
        // This should reuse the same parsing pipeline as voice requests
        // after speech-to-text is complete.
        return List.of();
    }
}