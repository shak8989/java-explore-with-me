
package ru.practicum.ewm.compilation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompilationRepository
        extends JpaRepository<Compilation, Long> {

    List<Compilation> findByPinnedOrderByIdAsc(boolean pinned);

    // Получение всех подборок с пагинацией
    @Query(
            value = "SELECT * FROM compilations " +
                    "ORDER BY id ASC LIMIT :size OFFSET :from",
            nativeQuery = true
    )
    List<Compilation> findAllPaged(
            @Param("from") int from,
            @Param("size") int size
    );

    // Получение подборок с фильтром и пагинацией
    @Query(
            value = "SELECT * FROM compilations " +
                    "WHERE pinned = :pinned " +
                    "ORDER BY id ASC LIMIT :size OFFSET :from",
            nativeQuery = true
    )
    List<Compilation> findByPinnedPaged(
            @Param("pinned") boolean pinned,
            @Param("from") int from,
            @Param("size") int size
    );
}
