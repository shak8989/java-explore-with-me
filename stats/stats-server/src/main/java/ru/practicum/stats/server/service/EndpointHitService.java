
package ru.practicum.stats.server.service;

import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.server.model.EndpointHit;
import ru.practicum.stats.server.repository.EndpointHitRepository;
import java.time.LocalDateTime;
import java.util.List;
import ru.practicum.stats.dto.ViewStatsDto;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


@Service
public class EndpointHitService {

    private final EndpointHitRepository repository;

    public EndpointHitService(EndpointHitRepository repository) {
        this.repository = repository;
    }

    public void saveHit(EndpointHitDto dto) {
        EndpointHit hit = new EndpointHit();

        hit.setApp(dto.getApp());
        hit.setUri(dto.getUri());
        hit.setIp(dto.getIp());
        hit.setTimestamp(dto.getTimestamp());

        repository.save(hit);
    }

    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end) {

        return repository.findStats(start, end);
    }

    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            boolean unique) {

        if (unique) {
            return repository.findUniqueStats(start, end);
        }

        return repository.findStats(start, end);
    }


    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique) {

        if (start.isAfter(end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date must not be after end date"
            );
        }

        if (uris != null && !uris.isEmpty()) {
            if (unique) {
                return repository.findUniqueStatsByUris(start, end, uris);
            }

            return repository.findStatsByUris(start, end, uris);
        }

        if (unique) {
            return repository.findUniqueStats(start, end);
        }

        return repository.findStats(start, end);
    }

}
