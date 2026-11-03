
package ru.practicum.ewm.request;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.user.User;
import java.util.List;

@Service
public class ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ParticipationRequestService(
            ParticipationRequestRepository requestRepository,
            EventRepository eventRepository,
            UserRepository userRepository) {

        this.requestRepository = requestRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot join your own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Event is not published");
        }

        if (requestRepository.existsByRequester_IdAndEvent_Id(
                userId, eventId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Request already exists");
        }

        int limit = event.getParticipantLimit();

        if (limit > 0 && event.getConfirmedRequests() >= limit) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Participant limit reached");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setEvent(event);
        request.setRequester(user);

        if (limit == 0 || !event.isRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(
                    event.getConfirmedRequests() + 1);
        }

        return ParticipationRequestMapper.toDto(
                requestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found");
        }

        return requestRepository.findByRequester_IdOrderByIdAsc(userId)
                .stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }


    @Transactional
    public ParticipationRequestDto cancelRequest(
            Long userId, Long requestId) {

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Request not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot cancel another user's request");
        }

        if (request.getStatus() == RequestStatus.CANCELED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Request is already canceled");
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            var event = request.getEvent();
            event.setConfirmedRequests(
                    Math.max(0, event.getConfirmedRequests() - 1));
        }

        request.setStatus(RequestStatus.CANCELED);

        return ParticipationRequestMapper.toDto(
                requestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(
            Long userId, Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only event initiator can view requests");
        }

        return requestRepository.findByEvent_IdOrderByIdAsc(eventId)
                .stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatuses(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest updateRequest) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only event initiator can update requests");
        }

        RequestStatus newStatus = updateRequest.getStatus();

        if (newStatus != RequestStatus.CONFIRMED
                && newStatus != RequestStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be CONFIRMED or REJECTED");
        }

        List<Long> requestIds = updateRequest.getRequestIds();

        if (requestIds == null || requestIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Request IDs are required");
        }

        if (requestIds.contains(null)
                || new java.util.HashSet<>(requestIds).size() != requestIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid or duplicate request IDs");
        }

        EventRequestStatusUpdateResult result =
                new EventRequestStatusUpdateResult();

        for (Long requestId : requestIds) {

            ParticipationRequest request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Request not found"));

            if (!request.getEvent().getId().equals(eventId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Request belongs to another event");
            }

            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Only pending requests can be updated");
            }

            if (newStatus == RequestStatus.CONFIRMED) {

                int limit = event.getParticipantLimit();

                if (limit > 0
                        && event.getConfirmedRequests() >= limit) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "No available places");
                }

                request.setStatus(RequestStatus.CONFIRMED);
                event.setConfirmedRequests(
                        event.getConfirmedRequests() + 1);

                result.getConfirmedRequests().add(
                        ParticipationRequestMapper.toDto(
                                requestRepository.save(request)));

            } else {

                request.setStatus(RequestStatus.REJECTED);

                result.getRejectedRequests().add(
                        ParticipationRequestMapper.toDto(
                                requestRepository.save(request)));
            }
        }

        if (newStatus == RequestStatus.CONFIRMED
                && event.getParticipantLimit() > 0
                && event.getConfirmedRequests() >= event.getParticipantLimit()) {

            List<ParticipationRequest> pendingRequests =
                    requestRepository.findByEvent_IdAndStatus(
                            eventId, RequestStatus.PENDING);

            for (ParticipationRequest pending : pendingRequests) {
                pending.setStatus(RequestStatus.REJECTED);

                result.getRejectedRequests().add(
                        ParticipationRequestMapper.toDto(
                                requestRepository.save(pending)));
            }
        }

        return result;
    }

}
