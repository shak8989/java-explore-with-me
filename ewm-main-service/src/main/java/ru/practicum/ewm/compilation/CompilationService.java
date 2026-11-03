
package ru.practicum.ewm.compilation;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    public CompilationService(
            CompilationRepository compilationRepository,
            EventRepository eventRepository) {

        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    // 1. Создание подборки
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto request) {

        Compilation compilation = new Compilation();

        compilation.setTitle(request.getTitle());
        compilation.setPinned(request.isPinned());

        Set<Event> events = request.getEvents() == null
                ? new LinkedHashSet<>()
                : loadEvents(request.getEvents());

        compilation.setEvents(events);

        Compilation saved = compilationRepository.save(compilation);

        return CompilationMapper.toDto(saved);
    }

    // 2. Получение подборки по ID
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compilationId) {

        Compilation compilation = compilationRepository
                .findById(compilationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Compilation not found"));

        return CompilationMapper.toDto(compilation);
    }

    // 3. Получение списка подборок
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned) {

        List<Compilation> compilations;

        if (pinned == null) {
            compilations = compilationRepository.findAll(
                    Sort.by("id").ascending()
            );
        } else {
            compilations = compilationRepository
                    .findByPinnedOrderByIdAsc(pinned);
        }

        return compilations.stream()
                .map(CompilationMapper::toDto)
                .toList();
    }

    // 4. Удаление подборки
    @Transactional
    public void deleteCompilation(Long compilationId) {

        Compilation compilation = compilationRepository
                .findById(compilationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Compilation not found"));

        compilationRepository.delete(compilation);
    }

    // 5. Редактирование подборки
    @Transactional
    public CompilationDto updateCompilation(
            Long compilationId,
            UpdateCompilationRequest request) {

        Compilation compilation = compilationRepository
                .findById(compilationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Compilation not found"));

        // Изменяем название
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        // Изменяем закрепление
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        // Изменяем список мероприятий
        if (request.getEvents() != null) {

            Set<Event> events = loadEvents(request.getEvents());

            compilation.setEvents(events);
        }

        Compilation saved = compilationRepository.save(compilation);

        return CompilationMapper.toDto(saved);
    }

    // Загружаем все события одним вызовом репозитория и сохраняем порядок ID.
    private Set<Event> loadEvents(java.util.Collection<Long> requestedIds) {
        Set<Long> eventIds = new LinkedHashSet<>(requestedIds);
        if (eventIds.contains(null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Event ID cannot be null");
        }

        if (eventIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<Event> foundEvents = eventRepository.findAllById(eventIds);
        if (foundEvents.size() != eventIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "One or more events not found");
        }

        Map<Long, Event> eventsById = foundEvents.stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        Set<Event> orderedEvents = new LinkedHashSet<>();
        for (Long eventId : eventIds) {
            Event event = eventsById.get(eventId);
            if (event == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found: " + eventId);
            }
            orderedEvents.add(event);
        }
        return orderedEvents;
    }

    // 6. Постраничная выдача через PostgreSQL
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilationsPaged(
            Boolean pinned, int from, int size) {

        List<Compilation> compilations;

        if (pinned == null) {

            // Все подборки с пагинацией
            compilations = compilationRepository
                    .findAllPaged(from, size);

        } else {

            // Подборки с фильтром и пагинацией
            compilations = compilationRepository
                    .findByPinnedPaged(pinned, from, size);
        }

        return compilations.stream()
                .map(CompilationMapper::toDto)
                .toList();
    }
}
