
package ru.practicum.ewm.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final StatsClient statsClient;

    public EventService(
            EventRepository eventRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            StatsClient statsClient) {

        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.statsClient = statsClient;
    }

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto request) {

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + userId));

        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found"));

        Event event = EventMapper.toEvent(request, category, initiator);

        Event savedEvent = eventRepository.save(event);

        return EventMapper.toFullDto(savedEvent);
    }


    @Transactional(readOnly = true)
    public List<EventFullDto> getUserEvents(
            Long userId, int from, int size) {

        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository
                .findByInitiator_IdOrderByIdAsc(userId, pageable)
                .getContent()
                .stream()
                .map(EventMapper::toFullDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public EventFullDto getUserEventById(Long userId, Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found: " + eventId
                ));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Event not found for user: " + userId
            );
        }

        return EventMapper.toFullDto(event);
    }

    @Transactional
    public EventFullDto updateUserEvent(
            Long userId,
            Long eventId,
            UpdateEventUserRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Event not found for user");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Published event cannot be edited");
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Category not found"));
            event.setCategory(category);
        }

        if (request.getEventDate() != null) {
            if (!request.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Event must start in more than 2 hours");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }

        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        }

        if (request.getStateAction() == UserStateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }

        return EventMapper.toFullDto(eventRepository.save(event));
    }

    @Transactional
    public EventFullDto updateAdminEvent(
            Long eventId,
            UpdateEventAdminRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found: " + eventId));

        AdminStateAction action = request.getStateAction();

        boolean hasFieldUpdates = request.getTitle() != null
                || request.getAnnotation() != null
                || request.getDescription() != null
                || request.getCategory() != null
                || request.getEventDate() != null
                || request.getLocation() != null
                || request.getPaid() != null
                || request.getParticipantLimit() != null
                || request.getRequestModeration() != null;

        if (action == null && !hasFieldUpdates) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No fields to update");
        }

        if (action != null && event.getState() != EventState.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending events can be moderated");
        }

        if (request.getEventDate() != null
                && !request.getEventDate().isAfter(
                        LocalDateTime.now().plusHours(1))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Event must start in more than 1 hour");
        }

        if (action == AdminStateAction.PUBLISH_EVENT) {

            LocalDateTime effectiveDate = request.getEventDate() != null
                    ? request.getEventDate()
                    : event.getEventDate();

            if (effectiveDate == null
                    || !effectiveDate.isAfter(
                            LocalDateTime.now().plusHours(1))) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Event must start in more than 1 hour");
            }
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {

            Category category = categoryRepository.findById(
                    request.getCategory()
            ).orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Category not found: " + request.getCategory()
            ));

            event.setCategory(category);
        }

        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }

        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }

        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (action == AdminStateAction.PUBLISH_EVENT) {

            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());

        } else if (action == AdminStateAction.REJECT_EVENT) {

            event.setState(EventState.CANCELED);
        }

        return EventMapper.toFullDto(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public EventFullDto getPublishedEventById(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found: " + eventId
                ));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Event is not published"
            );
        }

        return EventMapper.toFullDto(event);
    }


    @Transactional(readOnly = true)
    public List<EventFullDto> getPublishedEvents(int from, int size) {

        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository
                .findByStateOrderByIdAsc(EventState.PUBLISHED, pageable)
                .getContent()
                .stream()
                .map(EventMapper::toFullDto)
                .toList();
    }



    @Transactional(readOnly = true)
    public List<EventShortDto> getPublishedEventsShort(int from, int size) {

        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository
                .findByStateOrderByIdAsc(EventState.PUBLISHED, pageable)
                .getContent()
                .stream()
                .map(EventMapper::toShortDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<EventShortDto> getPublishedEventsShort(
            String text, int from, int size) {

        Pageable pageable = PageRequest.of(
                from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        List<Event> events;
        if (text == null || text.isBlank()) {
            events = eventRepository
                    .findByStateOrderByIdAsc(EventState.PUBLISHED, pageable)
                    .getContent();
        } else {
            events = eventRepository
                    .searchByText(EventState.PUBLISHED, text.trim(), pageable)
                    .getContent();
        }

        return events.stream()
                .map(EventMapper::toShortDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getPublishedEventsShort(
            String text, Long categoryId, int from, int size) {

        Specification<Event> specification = publishedEventsSpecification(
                text,
                categoryId == null ? null : List.of(categoryId),
                null, null, null, null);

        Pageable pageable = PageRequest.of(
                from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        return eventRepository.findAll(specification, pageable)
                .getContent()
                .stream()
                .map(EventMapper::toShortDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getPublishedEventsShort(
            String text, List<Long> categoryIds,
            Boolean paid, Boolean onlyAvailable,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            EventSort sort, int from, int size) {

        Specification<Event> specification = publishedEventsSpecification(
                text, categoryIds, paid, onlyAvailable, rangeStart, rangeEnd);

        List<Event> events;
        Map<Long, Long> sortViews = Map.of();

        if (sort == EventSort.VIEWS) {
            // Статистика просмотров хранится в другом сервисе, а не в PostgreSQL.
            // До реализации SQL-совместимого счётчика просмотров сортировать
            // по просмотрам приходится до пагинации в памяти.
            events = eventRepository.findAll(
                    specification, Sort.by(Sort.Direction.ASC, "id"));
            sortViews = getEventViewsForEvents(
                    events.stream().map(Event::getId).toList());
            Map<Long, Long> viewsForSorting = sortViews;
            events = events.stream()
                    .sorted((first, second) -> {
                        int result = Long.compare(
                                viewsForSorting.getOrDefault(second.getId(), 0L),
                                viewsForSorting.getOrDefault(first.getId(), 0L));
                        return result != 0 ? result
                                : Long.compare(first.getId(), second.getId());
                    })
                    .skip(from)
                    .limit(size)
                    .toList();
        } else {
            Sort ordering = sort == EventSort.EVENT_DATE
                    ? Sort.by(Sort.Direction.ASC, "eventDate", "id")
                    : Sort.by(Sort.Direction.ASC, "id");
            Pageable pageable = PageRequest.of(from / size, size, ordering);
            events = eventRepository.findAll(specification, pageable).getContent();
        }

        List<EventShortDto> result = events.stream()
                .map(EventMapper::toShortDto)
                .toList();

        Map<Long, Long> views = sort == EventSort.VIEWS
                ? sortViews
                : getEventViewsForEvents(
                        result.stream().map(EventShortDto::getId).toList());

        result.forEach(dto ->
                dto.setViews(views.getOrDefault(dto.getId(), 0L)));

        return result;
    }

    private Specification<Event> publishedEventsSpecification(
            String text, List<Long> categoryIds,
            Boolean paid, Boolean onlyAvailable,
            LocalDateTime rangeStart, LocalDateTime rangeEnd) {

        Specification<Event> specification =
                (root, query, cb) -> cb.equal(root.get("state"), EventState.PUBLISHED);

        if (text != null && !text.isBlank()) {
            String pattern = "%" + text.trim().toLowerCase(java.util.Locale.ROOT) + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("annotation")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)));
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    root.get("category").get("id").in(categoryIds));
        }
        if (paid != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("paid"), paid));
        }
        if (Boolean.TRUE.equals(onlyAvailable)) {
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("participantLimit"), 0),
                    cb.gt(root.<Integer>get("participantLimit"),
                            root.<Integer>get("confirmedRequests"))));
        }
        if (rangeStart != null) {
            specification = specification.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            specification = specification.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
        return specification;
    }

    public void registerEventView(Long eventId, String ip) {

        EndpointHitDto hit = new EndpointHitDto();

        hit.setApp("ewm-main-service");
        hit.setUri("/events/" + eventId);
        hit.setIp(ip);
        hit.setTimestamp(LocalDateTime.now());

        try {
            statsClient.sendHit(hit);
        } catch (RestClientException exception) {
            LoggerFactory.getLogger(EventService.class)
                    .warn("Failed to register event view: {}",
                            eventId, exception);
        }
    }

    public long getEventViews(Long eventId) {

        String uri = "/events/" + eventId;

        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    LocalDateTime.of(2000, 1, 1, 0, 0),
                    LocalDateTime.now().plusSeconds(1),
                    List.of(uri),
                    true
            );

            return stats.stream()
                    .filter(stat -> "ewm-main-service".equals(stat.getApp()))
                    .filter(stat -> uri.equals(stat.getUri()))
                    .mapToLong(stat -> stat.getHits() == null
                            ? 0L
                            : stat.getHits())
                    .sum();

        } catch (RestClientException exception) {

            LoggerFactory.getLogger(EventService.class)
                    .warn("Failed to get views for event: {}",
                            eventId, exception);

            return 0L;
        }
    }

    public Map<Long, Long> getEventViewsForEvents(List<Long> eventIds) {

        Map<Long, Long> views = new HashMap<>();

        for (Long id : eventIds) {
            views.put(id, 0L);
        }

        if (eventIds.isEmpty()) {
            return views;
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    LocalDateTime.of(2000, 1, 1, 0, 0),
                    LocalDateTime.now().plusSeconds(1),
                    uris,
                    true
            );

            for (ViewStatsDto stat : stats) {
                if (!"ewm-main-service".equals(stat.getApp())) {
                    continue;
                }

                for (Long id : eventIds) {
                    if (("/events/" + id).equals(stat.getUri())) {

                        long hits = stat.getHits() == null
                                ? 0L
                                : stat.getHits();

                        views.merge(id, hits, Long::sum);
                    }
                }
            }

        } catch (RestClientException exception) {
            LoggerFactory.getLogger(EventService.class)
                    .warn("Failed to get event views", exception);
        }

        return views;
    }

    public void registerEventSearch(String ip) {

        EndpointHitDto hit = new EndpointHitDto();

        hit.setApp("ewm-main-service");
        hit.setUri("/events");
        hit.setIp(ip);
        hit.setTimestamp(LocalDateTime.now());

        try {
            statsClient.sendHit(hit);
        } catch (RestClientException exception) {
            LoggerFactory.getLogger(EventService.class)
                    .warn("Failed to register event search", exception);
        }
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> searchAdminEvents(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size) {

        Specification<Event> specification =
                (root, query, cb) -> cb.conjunction();

        if (users != null && !users.isEmpty()) {
            specification = specification.and(
                    (root, query, cb) ->
                            root.get("initiator").get("id").in(users)
            );
        }

        if (states != null && !states.isEmpty()) {
            specification = specification.and(
                    (root, query, cb) ->
                            root.get("state").in(states)
            );
        }

        if (categories != null && !categories.isEmpty()) {
            specification = specification.and(
                    (root, query, cb) ->
                            root.get("category").get("id").in(categories)
            );
        }

        if (rangeStart != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.<LocalDateTime>get("eventDate"),
                                    rangeStart
                            )
            );
        }

        if (rangeEnd != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.lessThanOrEqualTo(
                                    root.<LocalDateTime>get("eventDate"),
                                    rangeEnd
                            )
            );
        }


        Pageable pageable = PageRequest.of(
                from / size,
                size,
                Sort.by(Sort.Direction.ASC, "id")
        );

        return eventRepository.findAll(specification, pageable)
                .getContent()
                .stream()
                .map(EventMapper::toFullDto)
                .toList();
    }

}
