
package ru.practicum.ewm.event;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.category.CategoryDto;
import ru.practicum.ewm.user.UserShortDto;

public class EventMapper {

    public static EventFullDto toFullDto(Event event) {
        EventFullDto dto = new EventFullDto();

        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setState(event.getState());
        dto.setLocation(event.getLocation());

        dto.setPaid(event.isPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setRequestModeration(event.isRequestModeration());

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(event.getCategory().getId());
        categoryDto.setName(event.getCategory().getName());
        dto.setCategory(categoryDto);

        UserShortDto initiatorDto = new UserShortDto();
        initiatorDto.setId(event.getInitiator().getId());
        initiatorDto.setName(event.getInitiator().getName());
        dto.setInitiator(initiatorDto);

        dto.setViews(0);

        return dto;
    }

    public static Event toEvent(
            NewEventDto request,
            Category category,
            User initiator) {

        Event event = new Event();

        event.setTitle(request.getTitle());
        event.setAnnotation(request.getAnnotation());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setLocation(request.getLocation());

        event.setPaid(request.isPaid());
        event.setParticipantLimit(request.getParticipantLimit());
        event.setRequestModeration(request.isRequestModeration());

        event.setCategory(category);
        event.setInitiator(initiator);

        return event;
    }

    public static EventShortDto toShortDto(Event event) {
        EventShortDto dto = new EventShortDto();

        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setEventDate(event.getEventDate());
        dto.setPaid(event.isPaid());
        dto.setConfirmedRequests(event.getConfirmedRequests());

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(event.getCategory().getId());
        categoryDto.setName(event.getCategory().getName());
        dto.setCategory(categoryDto);

        UserShortDto initiatorDto = new UserShortDto();
        initiatorDto.setId(event.getInitiator().getId());
        initiatorDto.setName(event.getInitiator().getName());
        dto.setInitiator(initiatorDto);

        dto.setViews(0);

        return dto;
    }

}
