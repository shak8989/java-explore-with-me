
package ru.practicum.ewm.event;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.user.User;

@Entity
@Table(name = "events")
public class Event {

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    public User getInitiator() {
        return initiator;
    }

    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }

    @Column(name = "participant_limit", nullable = false)
    private int participantLimit = 0;

    public int getParticipantLimit() {
        return participantLimit;
    }

    public void setParticipantLimit(int participantLimit) {
        this.participantLimit = participantLimit;
    }

    @Column(nullable = false)
    private boolean paid = false;

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    @Column(name = "request_moderation", nullable = false)
    private boolean requestModeration = true;

    public boolean isRequestModeration() {
        return requestModeration;
    }

    public void setRequestModeration(boolean requestModeration) {
        this.requestModeration = requestModeration;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventState state = EventState.PENDING;

    public EventState getState() {
        return state;
    }

    public void setState(EventState state) {
        this.state = state;
    }

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    public LocalDateTime getPublishedOn() {
        return publishedOn;
    }

    public void setPublishedOn(LocalDateTime publishedOn) {
        this.publishedOn = publishedOn;
    }

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Column(nullable = false, length = 2000)
    private String annotation;

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    @Embedded
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Column(name = "confirmed_requests", nullable = false)
    private int confirmedRequests = 0;

    public int getConfirmedRequests() {
        return confirmedRequests;
    }

    public void setConfirmedRequests(int confirmedRequests) {
        this.confirmedRequests = confirmedRequests;
    }

}
