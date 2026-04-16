package ee.kubermehed.enginaator.controllers;

import ee.kubermehed.enginaator.dtos.CreateRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guest/requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @PostMapping
    public ResponseEntity<CreateRequestResponse> createRequest(
            @RequestBody CreateRequestDTO dto) {
        return ResponseEntity.ok(
                requestService.createGuestRequest(dto)
        );
    }

    @GetMapping("/{roomNumber}")
    public List<RequestDTO> getRoomRequests(@PathVariable String roomNumber) {
        return requestService.getRequestsForRoom(roomNumber);
    }
}