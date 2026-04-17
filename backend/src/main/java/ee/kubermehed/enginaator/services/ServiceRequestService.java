package ee.kubermehed.enginaator.services;

import ee.kubermehed.enginaator.dtos.ParsedItemDTO;
import ee.kubermehed.enginaator.dtos.RequestViewDTO;
import ee.kubermehed.enginaator.enums.RequestStatus;
import ee.kubermehed.enginaator.models.InventoryItem;
import ee.kubermehed.enginaator.models.RequestItem;
import ee.kubermehed.enginaator.models.ServiceRequest;
import ee.kubermehed.enginaator.repositories.InventoryItemRepository;
import ee.kubermehed.enginaator.repositories.ServiceRequestRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void createServiceRequest(String roomNumber, MultipartFile file) {
        List<ParsedItemDTO> parsedItems = parserService.parseServiceRequest(file);
        List<String> itemNames = parsedItems.stream().map(ParsedItemDTO::getItemName).toList();
        Map<String, InventoryItem> inventoryItems = inventoryItemRepository.findAllByNameIn(itemNames)
                .stream().collect(Collectors.toMap(InventoryItem::getName, item -> item));

        List<RequestItem> requestItems = parsedItems.stream().map(parsedItem -> {
            InventoryItem inventoryItem = inventoryItems.get(parsedItem.getItemName());
            if (inventoryItem == null) {
                throw new RuntimeException("Item not found in inventory: " + parsedItem.getItemName());
            }

            int available = inventoryItem.getQuantityInStock() - inventoryItem.getQuantityReserved();

            if (available < parsedItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for item: " + parsedItem.getItemName());
            }

            inventoryItem.setQuantityReserved(inventoryItem.getQuantityReserved() + parsedItem.getQuantity());

            RequestItem requestItem = new RequestItem();
            requestItem.setItem(inventoryItem);
            requestItem.setQuantity(parsedItem.getQuantity());
            return requestItem;
        }).toList();

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setRoomNumber(roomNumber);
        serviceRequest.setRequestItems(requestItems);
        serviceRequest.setStatus(RequestStatus.RECEIVED);
        serviceRequest.setCreatedAt(LocalDateTime.now());

        serviceRequestRepository.save(serviceRequest);
        inventoryItemRepository.saveAll(inventoryItems.values());
        // TODO: send WS event about new service request
    }

    @Transactional
    public void updateServiceRequest(UUID requestId, boolean isApproved) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Service request not found"));
        if (isApproved) {
            serviceRequest.setStatus(RequestStatus.IN_PROGRESS);
            serviceRequest.getRequestItems().forEach(requestItem -> {
                InventoryItem inventoryItem = requestItem.getItem();

                int leftReserved = inventoryItem.getQuantityReserved() - requestItem.getQuantity();
                inventoryItem.setQuantityReserved(leftReserved);
                int leftInStock = inventoryItem.getQuantityInStock() - requestItem.getQuantity();
                inventoryItem.setQuantityInStock(leftInStock);

                inventoryItemRepository.save(inventoryItem);
            });
            serviceRequest.setStatus(RequestStatus.DELIVERED);

        } else {
            serviceRequest.setStatus(RequestStatus.REJECTED);
            serviceRequest.getRequestItems().forEach(requestItem -> {
                InventoryItem inventoryItem = requestItem.getItem();

                int leftReserved = inventoryItem.getQuantityReserved() - requestItem.getQuantity();
                inventoryItem.setQuantityReserved(leftReserved);

                inventoryItemRepository.save(inventoryItem);
            });
        }

        serviceRequestRepository.save(serviceRequest);
        // TODO: send WS event about service request update to guests
    }
}
