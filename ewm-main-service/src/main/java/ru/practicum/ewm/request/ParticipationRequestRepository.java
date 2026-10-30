
package ru.practicum.ewm.request;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipationRequestRepository
        extends JpaRepository<ParticipationRequest, Long> {


    boolean existsByRequester_IdAndEvent_Id(Long requesterId, Long eventId);


    List<ParticipationRequest> findByRequester_IdOrderByIdAsc(Long userId);

    List<ParticipationRequest> findByEvent_IdOrderByIdAsc(Long eventId);


    List<ParticipationRequest> findByEvent_IdAndStatus(
            Long eventId,
            RequestStatus status);

}
