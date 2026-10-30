
package ru.practicum.ewm.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryRepository;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private StatsClient statsClient;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldRegisterEventSearch() {

        eventService.registerEventSearch("127.0.0.1");

        ArgumentCaptor<EndpointHitDto> captor =
                ArgumentCaptor.forClass(EndpointHitDto.class);

        verify(statsClient).sendHit(captor.capture());

        EndpointHitDto hit = captor.getValue();

        assertEquals("ewm-main-service", hit.getApp());
        assertEquals("/events", hit.getUri());
        assertEquals("127.0.0.1", hit.getIp());
        assertNotNull(hit.getTimestamp());
    }

    @Test
    void shouldRegisterEventView() {

        eventService.registerEventView(3L, "127.0.0.1");

        ArgumentCaptor<EndpointHitDto> captor =
                ArgumentCaptor.forClass(EndpointHitDto.class);

        verify(statsClient).sendHit(captor.capture());

        EndpointHitDto hit = captor.getValue();

        assertEquals("ewm-main-service", hit.getApp());
        assertEquals("/events/3", hit.getUri());
        assertEquals("127.0.0.1", hit.getIp());
        assertNotNull(hit.getTimestamp());
    }

    @Test
    void shouldReturnEventViews() {

        ViewStatsDto statistics = new ViewStatsDto(
                "ewm-main-service",
                "/events/3",
                5L
        );

        when(statsClient.getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(List.of("/events/3")),
                eq(true)
        )).thenReturn(List.of(statistics));

        long views = eventService.getEventViews(3L);

        assertEquals(5L, views);

        verify(statsClient).getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(List.of("/events/3")),
                eq(true)
        );
    }

    @Test
    void shouldReturnZeroWhenStatsServerUnavailable() {

        when(statsClient.getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(List.of("/events/3")),
                eq(true)
        )).thenThrow(
                new RestClientException("Stats server unavailable")
        );

        long views = eventService.getEventViews(3L);

        assertEquals(0L, views);
    }

    @Test
    void shouldReturnViewsForMultipleEvents() {

        List<Long> eventIds = List.of(1L, 3L);

        ViewStatsDto statistics = new ViewStatsDto(
                "ewm-main-service",
                "/events/3",
                5L
        );

        when(statsClient.getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(List.of("/events/1", "/events/3")),
                eq(true)
        )).thenReturn(List.of(statistics));

        var views = eventService.getEventViewsForEvents(eventIds);

        assertEquals(Long.valueOf(0L), views.get(1L));
        assertEquals(Long.valueOf(5L), views.get(3L));
    }

    @Test
    void shouldReturnZeroViewsForMultipleEventsWhenStatsUnavailable() {

        List<Long> eventIds = List.of(1L, 2L, 3L);

        when(statsClient.getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(List.of("/events/1", "/events/2", "/events/3")),
                eq(true)
        )).thenThrow(
                new RestClientException("Stats server unavailable")
        );

        var views = eventService.getEventViewsForEvents(eventIds);

        assertEquals(3, views.size());
        assertEquals(0L, views.get(1L));
        assertEquals(0L, views.get(2L));
        assertEquals(0L, views.get(3L));
    }

    @Test
    void shouldReturnEmptyViewsForEmptyEventList() {

        var views = eventService.getEventViewsForEvents(
                List.of()
        );

        assertTrue(views.isEmpty());

        verifyNoInteractions(statsClient);
    }

    @Test
    void shouldNotFailWhenRegisteringViewWithoutStatsServer() {

        doThrow(new RestClientException("Stats server unavailable"))
                .when(statsClient)
                .sendHit(any(EndpointHitDto.class));

        assertDoesNotThrow(() ->
                eventService.registerEventView(3L, "127.0.0.1")
        );

        verify(statsClient).sendHit(
                any(EndpointHitDto.class)
        );
    }

    @Test
    void shouldNotFailWhenRegisteringSearchWithoutStatsServer() {

        doThrow(new RestClientException("Stats server unavailable"))
                .when(statsClient)
                .sendHit(any(EndpointHitDto.class));

        assertDoesNotThrow(() ->
                eventService.registerEventSearch("127.0.0.1")
        );

        verify(statsClient).sendHit(
                any(EndpointHitDto.class)
        );
    }

    @Test
    void shouldReturn404WhenEventNotFound() {

        when(eventRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventService.getPublishedEventById(999L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(eventRepository).findById(999L);
    }

    @Test
    void shouldReturn404WhenEventNotPublished() {

        Event event = mock(Event.class);

        when(eventRepository.findById(4L))
                .thenReturn(Optional.of(event));

        when(event.getState())
                .thenReturn(EventState.PENDING);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventService.getPublishedEventById(4L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void shouldRejectPublishingEventLessThanOneHourBeforeStart() {

        Event event = mock(Event.class);

        UpdateEventAdminRequest request =
                new UpdateEventAdminRequest();

        when(eventRepository.findById(4L))
                .thenReturn(Optional.of(event));

        request.setStateAction(AdminStateAction.PUBLISH_EVENT);

        when(event.getState())
                .thenReturn(EventState.PENDING);

        when(event.getEventDate())
                .thenReturn(LocalDateTime.now().plusMinutes(30));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventService.updateAdminEvent(4L, request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldPublishEventWhenStartIsMoreThanOneHourAway() {

        Event event = new Event();
        event.setId(4L);
        event.setState(EventState.PENDING);
        event.setEventDate(LocalDateTime.now().plusDays(2));
        event.setCategory(new Category());
        event.setInitiator(new User());

        UpdateEventAdminRequest request =
                new UpdateEventAdminRequest();

        when(eventRepository.findById(4L))
                .thenReturn(Optional.of(event));

        request.setStateAction(AdminStateAction.PUBLISH_EVENT);

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() ->
                eventService.updateAdminEvent(4L, request)
        );

        assertEquals(EventState.PUBLISHED, event.getState());

        assertNotNull(event.getPublishedOn());

        verify(eventRepository).save(event);
    }

    @Test
    void shouldRejectPublishingAlreadyPublishedEvent() {

        Event event = new Event();
        event.setId(4L);
        event.setState(EventState.PUBLISHED);

        UpdateEventAdminRequest request =
                new UpdateEventAdminRequest();

        when(eventRepository.findById(4L))
                .thenReturn(Optional.of(event));

        request.setStateAction(AdminStateAction.PUBLISH_EVENT);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventService.updateAdminEvent(4L, request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldRejectPendingEvent() {

        Event event = new Event();
        event.setId(5L);
        event.setState(EventState.PENDING);
        event.setCategory(new Category());
        event.setInitiator(new User());

        UpdateEventAdminRequest request =
                new UpdateEventAdminRequest();

        when(eventRepository.findById(5L))
                .thenReturn(Optional.of(event));

        request.setStateAction(AdminStateAction.REJECT_EVENT);

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() ->
                eventService.updateAdminEvent(5L, request)
        );

        assertEquals(
                EventState.CANCELED,
                event.getState()
        );

        verify(eventRepository).save(event);
    }

    @Test
    void shouldRejectCancelingPublishedEvent() {

        Event event = new Event();
        event.setId(5L);
        event.setState(EventState.PUBLISHED);

        UpdateEventAdminRequest request =
                new UpdateEventAdminRequest();

        when(eventRepository.findById(5L))
                .thenReturn(Optional.of(event));

        request.setStateAction(AdminStateAction.REJECT_EVENT);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventService.updateAdminEvent(5L, request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        assertEquals(
                EventState.PUBLISHED,
                event.getState()
        );

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldReturn400WhenAdminStateActionMissing() {

        Event event = new Event();
        event.setId(6L);
        event.setState(EventState.PENDING);

        UpdateEventAdminRequest request =
                new UpdateEventAdminRequest();

        when(eventRepository.findById(6L))
                .thenReturn(Optional.of(event));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventService.updateAdminEvent(6L, request)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldReturnPublishedEventById() {

        Event event = new Event();
        event.setId(7L);
        event.setTitle("Java Conference");
        event.setState(EventState.PUBLISHED);
        event.setCategory(new Category());
        event.setInitiator(new User());

        when(eventRepository.findById(7L))
                .thenReturn(Optional.of(event));

        var result = eventService.getPublishedEventById(7L);

        assertNotNull(result);
        assertEquals(7L, result.getId());
        assertEquals("Java Conference", result.getTitle());
        assertEquals(EventState.PUBLISHED, result.getState());

        verify(eventRepository).findById(7L);
    }

    @Test
    void shouldSearchPublishedEventsByText() {

        Event event = new Event();
        event.setId(8L);
        event.setTitle("Java Conference");
        event.setState(EventState.PUBLISHED);
        event.setCategory(new Category());
        event.setInitiator(new User());

        when(eventRepository.searchByText(
                EventState.PUBLISHED, "Java"))
                .thenReturn(List.of(event));

        var result = eventService.getPublishedEventsShort(
                "  Java  ", 0, 10
        );

        assertEquals(1, result.size());
        assertEquals(8L, result.get(0).getId());
        assertEquals("Java Conference", result.get(0).getTitle());

        verify(eventRepository).searchByText(
                EventState.PUBLISHED, "Java"
        );
    }

    @Test
    void shouldFilterPublishedEventsByCategory() {

        Category musicCategory = mock(Category.class);
        when(musicCategory.getId()).thenReturn(1L);

        Category sportCategory = mock(Category.class);
        when(sportCategory.getId()).thenReturn(2L);

        Event musicEvent = new Event();
        musicEvent.setId(10L);
        musicEvent.setTitle("Music Night");
        musicEvent.setState(EventState.PUBLISHED);
        musicEvent.setCategory(musicCategory);
        musicEvent.setInitiator(new User());

        Event sportEvent = new Event();
        sportEvent.setId(11L);
        sportEvent.setTitle("Football Match");
        sportEvent.setState(EventState.PUBLISHED);
        sportEvent.setCategory(sportCategory);
        sportEvent.setInitiator(new User());

        when(eventRepository.findByStateAndCategory_IdOrderByIdAsc(
                EventState.PUBLISHED, 1L
        )).thenReturn(List.of(musicEvent, sportEvent));

        var result = eventService.getPublishedEventsShort(
                null, 1L, 0, 10
        );

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals("Music Night", result.get(0).getTitle());

        verify(eventRepository)
                .findByStateAndCategory_IdOrderByIdAsc(
                        EventState.PUBLISHED, 1L
                );
    }

    @Test
    void shouldFilterPaidEvents() {

        Category category = new Category();
        category.setId(1L);

        User initiator = new User();

        Event paidEvent = new Event();
        paidEvent.setId(20L);
        paidEvent.setTitle("Paid Concert");
        paidEvent.setPaid(true);
        paidEvent.setCategory(category);
        paidEvent.setInitiator(initiator);

        Event freeEvent = new Event();
        freeEvent.setId(21L);
        freeEvent.setTitle("Free Concert");
        freeEvent.setPaid(false);
        freeEvent.setCategory(category);
        freeEvent.setInitiator(initiator);

        when(eventRepository.findByStateOrderByIdAsc(
                EventState.PUBLISHED
        )).thenReturn(List.of(paidEvent, freeEvent));

        when(statsClient.getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList(),
                eq(true)
        )).thenReturn(List.of());

        var result = eventService.getPublishedEventsShort(
                null, null, true, null,
                null, null, null, 0, 10
        );

        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getId());
        assertEquals("Paid Concert", result.get(0).getTitle());
    }

    @Test
    void shouldFilterOnlyAvailableEvents() {

        Category category = new Category();
        category.setId(1L);

        User initiator = new User();

        Event fullEvent = new Event();
        fullEvent.setId(30L);
        fullEvent.setTitle("Full Workshop");
        fullEvent.setCategory(category);
        fullEvent.setInitiator(initiator);
        fullEvent.setParticipantLimit(1);
        fullEvent.setConfirmedRequests(1);

        Event availableEvent = new Event();
        availableEvent.setId(31L);
        availableEvent.setTitle("Available Workshop");
        availableEvent.setCategory(category);
        availableEvent.setInitiator(initiator);
        availableEvent.setParticipantLimit(10);
        availableEvent.setConfirmedRequests(3);

        when(eventRepository.findByStateOrderByIdAsc(
                EventState.PUBLISHED
        )).thenReturn(List.of(fullEvent, availableEvent));

        when(statsClient.getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList(),
                eq(true)
        )).thenReturn(List.of());

        var result = eventService.getPublishedEventsShort(
                null, null, null, true,
                null, null, null, 0, 10
        );

        assertEquals(1, result.size());
        assertEquals(31L, result.get(0).getId());
        assertEquals("Available Workshop", result.get(0).getTitle());
    }

    @Test
    void shouldReturnCorrectPageOfEvents() {

        Category category = new Category();
        category.setId(1L);

        User initiator = new User();

        Event first = new Event();
        first.setId(1L);
        first.setCategory(category);
        first.setInitiator(initiator);

        Event second = new Event();
        second.setId(2L);
        second.setCategory(category);
        second.setInitiator(initiator);

        Event third = new Event();
        third.setId(3L);
        third.setCategory(category);
        third.setInitiator(initiator);

        when(eventRepository.findByStateOrderByIdAsc(
                EventState.PUBLISHED
        )).thenReturn(List.of(first, second, third));

        var result = eventService.getPublishedEventsShort(1, 1);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void shouldSortPublishedEventsByEventDate() {

        Category category = new Category();
        category.setId(1L);

        User initiator = new User();

        Event laterEvent = new Event();
        laterEvent.setId(40L);
        laterEvent.setTitle("December Concert");
        laterEvent.setEventDate(
                LocalDateTime.of(2027, 12, 20, 19, 0)
        );
        laterEvent.setCategory(category);
        laterEvent.setInitiator(initiator);

        Event earlierEvent = new Event();
        earlierEvent.setId(41L);
        earlierEvent.setTitle("November Concert");
        earlierEvent.setEventDate(
                LocalDateTime.of(2027, 11, 20, 19, 0)
        );
        earlierEvent.setCategory(category);
        earlierEvent.setInitiator(initiator);

        when(eventRepository.findByStateOrderByIdAsc(
                EventState.PUBLISHED
        )).thenReturn(List.of(laterEvent, earlierEvent));

        when(statsClient.getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList(),
                eq(true)
        )).thenReturn(List.of());

        var result = eventService.getPublishedEventsShort(
                null, null, null, null,
                null, null, EventSort.EVENT_DATE, 0, 10
        );

        assertEquals(2, result.size());
        assertEquals(41L, result.get(0).getId());
        assertEquals(40L, result.get(1).getId());
    }

    @Test
    void shouldSortPublishedEventsByViews() {

        Category category = new Category();
        category.setId(1L);

        User initiator = new User();

        Event lessPopular = new Event();
        lessPopular.setId(50L);
        lessPopular.setTitle("Small Concert");
        lessPopular.setCategory(category);
        lessPopular.setInitiator(initiator);

        Event morePopular = new Event();
        morePopular.setId(51L);
        morePopular.setTitle("Big Concert");
        morePopular.setCategory(category);
        morePopular.setInitiator(initiator);

        when(eventRepository.findByStateOrderByIdAsc(
                EventState.PUBLISHED
        )).thenReturn(List.of(lessPopular, morePopular));

        when(statsClient.getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList(),
                eq(true)
        )).thenReturn(List.of(
                new ViewStatsDto(
                        "ewm-main-service", "/events/50", 2L
                ),
                new ViewStatsDto(
                        "ewm-main-service", "/events/51", 10L
                )
        ));

        var result = eventService.getPublishedEventsShort(
                null, null, null, null,
                null, null, EventSort.VIEWS, 0, 10
        );

        assertEquals(2, result.size());
        assertEquals(51L, result.get(0).getId());
        assertEquals(50L, result.get(1).getId());
    }
}
