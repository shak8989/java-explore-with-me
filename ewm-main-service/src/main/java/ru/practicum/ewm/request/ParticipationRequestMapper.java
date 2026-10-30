
package ru.practicum.ewm.request;

public class ParticipationRequestMapper {

    public static ParticipationRequestDto toDto(
            ParticipationRequest request) {

        ParticipationRequestDto dto =
                new ParticipationRequestDto();

        dto.setId(request.getId());
        dto.setEvent(request.getEvent().getId());
        dto.setRequester(request.getRequester().getId());
        dto.setStatus(request.getStatus());
        dto.setCreated(request.getCreated());

        return dto;
    }
}
