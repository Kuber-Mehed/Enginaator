package ee.kubermehed.enginaator.controllers;

@RestController
@RequestMapping("/api/staff/requests")
public class StaffRequestController {

    private final RequestService requestService;

    public StaffRequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public List<RequestDTO> getAllRequests() {
        return requestService.getAllRequests();
    }

    @PatchMapping("/{requestId}/status")
    public RequestDTO updateStatus(
            @PathVariable Long requestId,
            @RequestBody UpdateStatusDTO dto
    ) {
        return requestService.updateStatus(requestId, dto.getStatus());
    }

    @PostMapping("/{requestId}/reject")
    public RequestDTO rejectRequest(
            @PathVariable Long requestId,
            @RequestBody RejectRequestDTO dto
    ) {
        return requestService.rejectRequest(requestId, dto.getReason());
    }
}