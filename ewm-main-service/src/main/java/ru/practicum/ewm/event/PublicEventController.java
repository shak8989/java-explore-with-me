
package ru.practicum.ewm.event;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;

    public PublicEventController(EventService eventService) {
        this.eventService = eventService;
    }



    @GetMapping("/{eventId}")
    public EventFullDto getEventById(
            @PathVariable Long eventId,
            HttpServletRequest request) {

        // Получаем опубликованное мероприятие
        EventFullDto event =
                eventService.getPublishedEventById(eventId);

        // Регистрируем просмотр
        eventService.registerEventView(
                eventId,
                request.getRemoteAddr()
        );

        // Получаем реальное количество просмотров
        long views = eventService.getEventViews(eventId);

        // Записываем просмотры в ответ
        event.setViews(views);

        return event;
    }












    @GetMapping
    public List<EventShortDto> getPublishedEvents(
            @RequestParam(required = false) String text,
            @RequestParam(name = "categories", required = false)
            List<Long> categoryIds,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeEnd,
            @RequestParam(required = false) EventSort sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request)  {

        if (rangeStart != null && rangeEnd != null
                && rangeStart.isAfter(rangeEnd)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "rangeStart must not be after rangeEnd"
            );
        }

        eventService.registerEventSearch(request.getRemoteAddr());

        return eventService.getPublishedEventsShort(
                text, categoryIds, paid, onlyAvailable,
                rangeStart, rangeEnd, sort, from, size);
    }


}
