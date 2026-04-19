package ee.kubermehed.enginaator.controllers;

import ee.kubermehed.enginaator.dtos.RequestViewDto;
import ee.kubermehed.enginaator.dtos.TextRequestDto;
import ee.kubermehed.enginaator.services.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guest/service-request")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @GetMapping()
    public ResponseEntity<List<RequestViewDto>> getServiceRequests() {
        return ResponseEntity.ok(serviceRequestService.getServiceRequests());
    }

    @PostMapping("/room/{roomNumber}")
    public ResponseEntity<RequestViewDto> createServiceRequest(@RequestParam("file") MultipartFile file,
                                                               @PathVariable String roomNumber) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        RequestViewDto created = serviceRequestService.createServiceRequest(roomNumber, file);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/room/{roomNumber}/text")
    public ResponseEntity<RequestViewDto> postTextRequest(
            @PathVariable String roomNumber,
            @RequestBody TextRequestDto body) {

        RequestViewDto created = serviceRequestService.createServiceRequestText(
                roomNumber, body.getRequestText()
        );
        return ResponseEntity.ok(created);
    }

    @PostMapping("/service-request/{requestId}")
    public ResponseEntity<Void> updateServiceRequest(@PathVariable UUID requestId, @RequestBody boolean isApproved) {
        serviceRequestService.updateServiceRequest(requestId, isApproved);
        return ResponseEntity.ok().build();
    }

    @RestController
    @RequestMapping("/api/staff/requests")
    public class StaffRequestController {

        // TODO:
        // Inject ServiceRequestService

        // TODO:
        // GET /api/staff/requests
        // Return all requests for dashboard startup load, newest first.

        // TODO:
        // PATCH /api/staff/requests/{requestId}
        // Accept status update payload:
        // { "status": "IN_PROGRESS" }
        // This endpoint should:
        // 1. update DB
        // 2. emit REQUEST_UPDATED to staff dashboards
        // 3. emit room-scoped guest update event to the correct room only
    }
}