package ee.kubermehed.enginaator.services;

import ee.kubermehed.enginaator.dtos.ParsedItemDTO;
import ee.kubermehed.enginaator.dtos.RequestViewDTO;
import ee.kubermehed.enginaator.enums.RequestStatus;
import ee.kubermehed.enginaator.models.InventoryItem;
import ee.kubermehed.enginaator.models.RequestItem;
import ee.kubermehed.enginaator.models.ServiceRequest;
import ee.kubermehed.enginaator.repositories.InventoryItemRepository;
import ee.kubermehed.enginaator.repositories.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestParserService parserService;
    private final ServiceRequestRepository serviceRequestRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public List<RequestViewDTO> getServiceRequests() {
         return serviceRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(RequestViewDTO::fromEntity)
                .toList();
    }

    public void createServiceRequest(String roomNumber, MultipartFile file) {
        List<ParsedItemDTO> items = parserService.parseServiceRequest(file);
        List<String> itemNames = items.stream().map(ParsedItemDTO::getItemName).toList();
        Map<String, InventoryItem> inventoryItems = inventoryItemRepository.findAllByNameIn(itemNames)
                .stream().collect(Collectors.toMap(InventoryItem::getName, item -> item));

        List<RequestItem> requestItems = items.stream().map(dto -> {
            InventoryItem inventoryItem = inventoryItems.get(dto.getItemName());
            if (inventoryItem == null) {
                throw new RuntimeException("Item not found in inventory: " + dto.getItemName());
            }
            RequestItem requestItem = new RequestItem();
            requestItem.setItem(inventoryItem);
            requestItem.setQuantity(dto.getQuantity());
            return requestItem;
        }).toList();

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setRoomNumber(roomNumber);
        serviceRequest.setRequestItems(requestItems);
        serviceRequest.setStatus(RequestStatus.IN_PROGRESS);
        serviceRequest.setCreatedAt(LocalDateTime.now());

        serviceRequestRepository.save(serviceRequest);
    }

    public void updateServiceRequest(UUID requestId, boolean isApproved) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Service request not found"));
        if (isApproved) {
            serviceRequest.setStatus(RequestStatus.IN_PROGRESS);
        } // TODO: continue
    }
}
