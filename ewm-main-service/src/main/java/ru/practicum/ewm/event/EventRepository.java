
package ru.practicum.ewm.event;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface EventRepository extends
        JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {


    List<Event> findByInitiator_IdOrderByIdAsc(Long userId);

    List<Event> findByStateAndCategory_IdInOrderByIdAsc(
            EventState state,
            List<Long> categoryIds);

    List<Event> findByStateAndCategory_IdOrderByIdAsc(EventState state, Long categoryId);


    List<Event> findByStateOrderByIdAsc(EventState state);

    @Query("""
        SELECT e FROM Event e
        WHERE e.state = :state
        AND (
            LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
            OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))
        )
        ORDER BY e.id ASC
        """)
    List<Event> searchByText(
            @Param("state") EventState state,
            @Param("text") String text);


}
