
package ru.practicum.ewm.compilation;

import jakarta.validation.constraints.Size;
import java.util.Set;

public class UpdateCompilationRequest {

    @Size(min = 1, max = 50)
    private String title;

    private Boolean pinned;

    private Set<Long> events;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public Set<Long> getEvents() {
        return events;
    }

    public void setEvents(Set<Long> events) {
        this.events = events;
    }
}
