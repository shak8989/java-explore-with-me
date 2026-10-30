
package ru.practicum.ewm.request;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
public class PrivateEventRequestController {

    private final ParticipationRequestService requestService;

    public PrivateEventRequestController(
            ParticipationRequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public List<ParticipationRequestDto> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult updateRequestStatuses(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest request) {

        return requestService.updateEventRequestStatuses(
                userId, eventId, request);
    }
}
