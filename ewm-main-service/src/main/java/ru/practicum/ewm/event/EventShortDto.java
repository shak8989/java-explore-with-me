
package ru.practicum.ewm.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.ewm.category.CategoryDto;
import ru.practicum.ewm.user.UserShortDto;

import java.time.LocalDateTime;

public class EventShortDto {

    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private UserShortDto initiator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private boolean paid;
    private int confirmedRequests;
    private long views;

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

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public CategoryDto getCategory() {
        return category;
    }

    public void setCategory(CategoryDto category) {
        this.category = category;
    }

    public UserShortDto getInitiator() {
        return initiator;
    }

    public void setInitiator(UserShortDto initiator) {
        this.initiator = initiator;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public int getConfirmedRequests() {
        return confirmedRequests;
    }

    public void setConfirmedRequests(int confirmedRequests) {
        this.confirmedRequests = confirmedRequests;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }
}
