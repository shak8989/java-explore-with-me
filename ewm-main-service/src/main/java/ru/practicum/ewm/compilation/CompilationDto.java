
package ru.practicum.ewm.compilation;

import ru.practicum.ewm.event.EventShortDto;

import java.util.ArrayList;
import java.util.List;

public class CompilationDto {

    private Long id;
    private String title;
    private boolean pinned;

    private List<EventShortDto> events = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public List<EventShortDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventShortDto> events) {
        this.events = events;
    }
}
