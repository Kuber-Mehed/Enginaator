package ee.kubermehed.enginaator.controllers;

import ee.kubermehed.enginaator.dtos.RequestViewDTO;
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
    public ResponseEntity<List<RequestViewDTO>> getServiceRequests() {
        return ResponseEntity.ok(serviceRequestService.getServiceRequests());
    }

    //    @GetMapping("/{roomNumber}")
//    public List<RequestDTO> getRoomRequests(@PathVariable String roomNumber) {
//        return requestService.getRequestsForRoom(roomNumber);
//    }

    @PostMapping("/room/{roomNumber}")
    public ResponseEntity<Void> createServiceRequest(@RequestParam("file") MultipartFile file,
                                                     @PathVariable String roomNumber) {
        System.out.println("got smth");
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        serviceRequestService.createServiceRequest(roomNumber, file);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/room/{roomNumber}/text")
    public ResponseEntity<?> postTextRequest(
            @PathVariable String roomNumber,
            @RequestBody TextRequestDto body
    ) {
        String requestText = body.getRequestText();

        // TODO:
        // Call serviceRequestService.createTextServiceRequest(roomNumber, requestText)
        // after text request flow is implemented.
        // For now this endpoint exists only to preserve frontend contract.

        return ResponseEntity.ok().build();
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