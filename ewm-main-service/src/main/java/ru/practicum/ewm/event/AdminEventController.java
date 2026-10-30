
package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest request) {

        return eventService.updateAdminEvent(eventId, request);
    }

    @GetMapping
    public List<EventFullDto> searchEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeStart,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeEnd,

            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        return eventService.searchAdminEvents(
                users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                from,
                size
        );
    }

}
