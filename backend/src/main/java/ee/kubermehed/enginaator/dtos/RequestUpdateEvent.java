package ee.kubermehed.enginaator.dtos;

import ee.kubermehed.enginaator.enums.RequestStatus;

public class RequestUpdateEvent {
    private Long requestId;
    private String roomNumber;
    private RequestStatus status;
}