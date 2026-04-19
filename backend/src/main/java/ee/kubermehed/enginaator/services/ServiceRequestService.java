package ee.kubermehed.enginaator.services;

import ee.kubermehed.enginaator.dtos.InventoryViewDTO;
import ee.kubermehed.enginaator.dtos.ParsedItemDTO;
import ee.kubermehed.enginaator.dtos.RequestViewDTO;
import ee.kubermehed.enginaator.enums.RequestStatus;
import ee.kubermehed.enginaator.models.InventoryItem;
import ee.kubermehed.enginaator.models.RequestItem;
import ee.kubermehed.enginaator.models.GuestRequest;
import ee.kubermehed.enginaator.repositories.InventoryItemRepository;
import ee.kubermehed.enginaator.repositories.ServiceRequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final NotificationService notificationService;

    public List<RequestViewDTO> getServiceRequests() {
        return serviceRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(RequestViewDTO::fromEntity)
                .toList();
    }

    @Transactional
    public RequestViewDTO createServiceRequest(String roomNumber, MultipartFile file) {
        List<ParsedItemDTO> parsedItems = parserService.parseFromAudio(file);
        return createRequest(roomNumber, null, parsedItems);
    }

    @Transactional
    public RequestViewDTO createServiceRequestText(String roomNumber, String requestText) {
        if (requestText == null || requestText.isBlank()) {
            throw new RuntimeException("Request text is required");
        }

        List<ParsedItemDTO> parsedItems = parserService.parseFromText(requestText);
        return createRequest(roomNumber, requestText.trim(), parsedItems);
    }


    @Transactional
    public void updateServiceRequest(UUID requestId, boolean isApproved) {
//  public RequestViewDTO updateServiceRequestStatus(UUID requestId, RequestStatus status)
        GuestRequest guestRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Service request not found"));
        if (isApproved) {
            guestRequest.setStatus(RequestStatus.IN_PROGRESS);
            guestRequest.getRequestItems().forEach(requestItem -> {
                InventoryItem inventoryItem = requestItem.getInventoryItem();

                int leftReserved = inventoryItem.getQuantityReserved() - requestItem.getQuantityRequested();
                inventoryItem.setQuantityReserved(leftReserved);
                int leftInStock = inventoryItem.getQuantityInStock() - requestItem.getQuantityRequested();
                inventoryItem.setQuantityInStock(leftInStock);

                inventoryItemRepository.save(inventoryItem);
            });
            guestRequest.setStatus(RequestStatus.DELIVERED);

        } else {
            guestRequest.setStatus(RequestStatus.REJECTED);
            guestRequest.getRequestItems().forEach(requestItem -> {
                InventoryItem inventoryItem = requestItem.getInventoryItem();

                int leftReserved = inventoryItem.getQuantityReserved() - requestItem.getQuantityRequested();
                inventoryItem.setQuantityReserved(leftReserved);

                inventoryItemRepository.save(inventoryItem);
            });
        }

        serviceRequestRepository.save(guestRequest);
        // TODO:
        // Emit REQUEST_UPDATED event:
        // - broadcast to all staff dashboards
        // - route only to the correct guest room connection

        // TODO: send WS event about service request update to guests
    }

    private RequestViewDTO createRequest(
            String roomNumber,
            String requestText,
            List<ParsedItemDTO> parsedItems
    ) {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new RuntimeException("Room number is required");
        }

        if (parsedItems == null || parsedItems.isEmpty()) {
            throw new RuntimeException("No valid items found in request");
        }

        List<String> itemNames = parsedItems.stream()
                .map(item -> item.getItemName().toLowerCase())
                .toList();

        Map<String, InventoryItem> inventoryItems = inventoryItemRepository
                .findAllByNameIn(itemNames)
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getName().trim().toLowerCase(),
                        item -> item
                ));

        GuestRequest guestRequest = new GuestRequest();
        guestRequest.setRoomNumber(roomNumber.trim());
        guestRequest.setRequestText(requestText);
        guestRequest.setStatus(RequestStatus.RECEIVED);

        for (ParsedItemDTO parsedItem : parsedItems) {
            String key = parsedItem.getItemName().trim().toLowerCase();
            InventoryItem inventoryItem = inventoryItems.get(key);

            if (inventoryItem == null) {
                throw new RuntimeException("Item not found: " + parsedItem.getItemName());
            }

            if (parsedItem.getQuantity() <= 0) {
                throw new RuntimeException("Invalid quantity for item: " + parsedItem.getItemName());
            }

            int available = inventoryItem.getQuantityInStock() - inventoryItem.getQuantityReserved();

            if (available < parsedItem.getQuantity()) {
                throw new RuntimeException("Not enough stock: " + parsedItem.getItemName());
            }

            inventoryItem.setQuantityReserved(
                    inventoryItem.getQuantityReserved() + parsedItem.getQuantity()
            );

            RequestItem requestItem = new RequestItem();
            requestItem.setInventoryItem(inventoryItem);
            requestItem.setQuantityRequested(parsedItem.getQuantity());
            requestItem.setQuantityFulfilled(0);

            guestRequest.addRequestItem(requestItem);
        }

        GuestRequest savedRequest = serviceRequestRepository.save(guestRequest);
        inventoryItemRepository.saveAll(inventoryItems.values());

        RequestViewDTO dto = RequestViewDTO.fromEntity(savedRequest);

        inventoryItems.values().forEach(item ->
                notificationService.sendToInventory(InventoryViewDTO.fromEntity(item))
        );
        notificationService.sendToStaff(dto);
        notificationService.sendToRoom(savedRequest.getRoomNumber(), dto);

        return dto;
    }
}
