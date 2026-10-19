
package ru.practicum.stats.client;

import ru.practicum.stats.dto.EndpointHitDto;

import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public class StatsClientCheck {

    public static void main(String[] args) {

        StatsClient client =
                new StatsClient("http://localhost:9090");

        EndpointHitDto hit = new EndpointHitDto(
                "ewm-main-service",
                "/events/25",
                "192.168.1.25",
                LocalDateTime.of(2026, 10, 18, 17, 0)
        );

        client.sendHit(hit);

        System.out.println("Просмотр /events/25 отправлен");

        List<ViewStatsDto> stats = client.getStats(
                LocalDateTime.of(2026, 10, 1, 0, 0),
                LocalDateTime.of(2026, 10, 31, 23, 59, 59),
                List.of("/events/15", "/events/20"),
                false
        );

        for (ViewStatsDto item : stats) {
            System.out.println(
                    item.getUri() + " : " + item.getHits()
            );
        }
    }
}
