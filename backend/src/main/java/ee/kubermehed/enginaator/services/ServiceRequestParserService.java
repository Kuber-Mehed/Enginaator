package ee.kubermehed.enginaator.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.kubermehed.enginaator.dtos.ParsedItemDTO;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class ServiceRequestParserService {

    private static final String AI_PARSING_SERVICE_URL = "http://localhost:8000/";
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServiceRequestParserService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl(AI_PARSING_SERVICE_URL).build();
    }

    public List<ParsedItemDTO> parseFromAudio(MultipartFile file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());

        String rawResponse = postForString(
                "/process-audio/",
                MediaType.MULTIPART_FORM_DATA,
                builder.build()
        );

        return parseItemsJson(normalizeItemsJson(rawResponse));
    }

    public List<ParsedItemDTO> parseFromText(String requestText) {
        String rawResponse = postForString(
                "/extract",
                MediaType.APPLICATION_JSON,
                Map.of("text", requestText)
        );

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode resultNode = root.get("result");

            if (resultNode == null || resultNode.isNull()) {
                throw new RuntimeException("Missing 'result' field in API response");
            }

            return parseItemsJson(normalizeItemsJson(resultNode.asText()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse text request", e);
        }
    }

    private String postForString(String uri, MediaType contentType, Object body) {
        try {
            String rawResponse = webClient.post()
                    .uri(uri)
                    .contentType(contentType)
                    .bodyValue(body)
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

            return rawResponse;
        } catch (WebClientResponseException e) {
            throw new RuntimeException(
                    "API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to call external API", e);
        }
    }

    private List<ParsedItemDTO> parseItemsJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<ParsedItemDTO>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse items JSON: " + json, e);
        }
    }

    private String normalizeItemsJson(String rawJson) {
        String json = rawJson.replace("```json", "")
                .replace("```", "")
                .trim();

        if (json.startsWith("\"") && json.endsWith("\"")) {
            json = json.substring(1, json.length() - 1);
        }

        json = json.replace("\\\"", "\"")
                .replace("\\n", "")
                .trim();

        int start = json.indexOf('[');
        int end = json.lastIndexOf(']');

        if (start != -1 && end != -1) {
            json = json.substring(start, end + 1);
        }

        return json;
    }

    private String extractTextResultJson(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode resultNode = root.get("result");

            if (resultNode == null || resultNode.isNull()) {
                throw new RuntimeException("Missing 'result' field in API response");
            }

            return resultNode.asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text parsing result", e);
        }
    }
}