package ee.kubermehed.enginaator.controllers;

import ee.kubermehed.enginaator.dtos.RequestViewDTO;
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
    public ResponseEntity<Void> createServiceRequest(@RequestBody MultipartFile file,
                                                     @PathVariable String roomNumber) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        serviceRequestService.createServiceRequest(roomNumber, file);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/service-request/{requestId}")
    public ResponseEntity<Void> updateServiceRequest(@PathVariable UUID requestId, @RequestBody boolean isApproved) {
        serviceRequestService.updateServiceRequest(requestId, isApproved);
        return ResponseEntity.ok().build();
    }
}