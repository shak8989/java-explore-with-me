
package ru.practicum.stats.client;

import org.springframework.web.client.RestTemplate;

import ru.practicum.stats.dto.ViewStatsDto;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import ru.practicum.stats.dto.EndpointHitDto;

public class StatsClient {

    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(String serverUrl) {
        this.restTemplate = new RestTemplate();
        this.serverUrl = serverUrl;
    }

    public void sendHit(EndpointHitDto hit) {
        restTemplate.postForEntity(
                serverUrl + "/hit",
                hit,
                Void.class
        );
    }


    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(serverUrl)
                        .path("/stats")
                        .queryParam("start", start.format(formatter))
                        .queryParam("end", end.format(formatter))
                        .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", String.join(",", uris));
        }

        URI uri = builder.build().encode().toUri();

        ViewStatsDto[] response =
                restTemplate.getForObject(uri, ViewStatsDto[].class);

        return response == null ? List.of() : Arrays.asList(response);
    }


}


