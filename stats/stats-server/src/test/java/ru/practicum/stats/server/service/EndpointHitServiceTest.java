
package ru.practicum.stats.server.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.stats.server.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class EndpointHitServiceTest {

    @Test
    void shouldRejectInvalidDateRange() {

        EndpointHitRepository repository =
                mock(EndpointHitRepository.class);

        EndpointHitService service =
                new EndpointHitService(repository);

        LocalDateTime start =
                LocalDateTime.of(2026, 10, 31, 0, 0);

        LocalDateTime end =
                LocalDateTime.of(2026, 10, 1, 0, 0);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.getStats(
                                start, end, List.of(), false)
                );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void shouldUseUniqueStatisticsWhenRequested() {
        EndpointHitRepository repository =
                mock(EndpointHitRepository.class);

        EndpointHitService service =
                new EndpointHitService(repository);

        LocalDateTime start =
                LocalDateTime.of(2026, 10, 1, 0, 0);

        LocalDateTime end =
                LocalDateTime.of(2026, 10, 31, 0, 0);

        service.getStats(start, end, List.of(), true);

        org.mockito.Mockito.verify(repository)
                .findUniqueStats(start, end);
    }


    @Test
    void shouldFilterStatsByUris() {
        EndpointHitRepository repository =
                mock(EndpointHitRepository.class);

        EndpointHitService service =
                new EndpointHitService(repository);

        LocalDateTime start =
                LocalDateTime.of(2026, 10, 1, 0, 0);

        LocalDateTime end =
                LocalDateTime.of(2026, 10, 31, 0, 0);

        List<String> uris = List.of("/events/15");

        service.getStats(start, end, uris, false);

        org.mockito.Mockito.verify(repository)
                .findStatsByUris(start, end, uris);
    }

    @Test
    void shouldFilterUniqueStatsByUris() {
        EndpointHitRepository repository =
                mock(EndpointHitRepository.class);

        EndpointHitService service =
                new EndpointHitService(repository);

        LocalDateTime start =
                LocalDateTime.of(2026, 10, 1, 0, 0);

        LocalDateTime end =
                LocalDateTime.of(2026, 10, 31, 0, 0);

        List<String> uris = List.of("/events/15");

        service.getStats(start, end, uris, true);

        org.mockito.Mockito.verify(repository)
                .findUniqueStatsByUris(start, end, uris);
    }

    @Test
    void shouldGetAllStatsWithoutFilters() {
        EndpointHitRepository repository =
                mock(EndpointHitRepository.class);

        EndpointHitService service =
                new EndpointHitService(repository);

        LocalDateTime start =
                LocalDateTime.of(2026, 10, 1, 0, 0);

        LocalDateTime end =
                LocalDateTime.of(2026, 10, 31, 0, 0);

        service.getStats(start, end, List.of(), false);

        org.mockito.Mockito.verify(repository)
                .findStats(start, end);
    }


}
