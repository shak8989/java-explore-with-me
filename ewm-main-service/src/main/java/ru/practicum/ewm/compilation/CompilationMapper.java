
package ru.practicum.ewm.compilation;

import ru.practicum.ewm.event.EventMapper;

public class CompilationMapper {

    public static CompilationDto toDto(Compilation compilation) {

        CompilationDto dto = new CompilationDto();

        dto.setId(compilation.getId());
        dto.setTitle(compilation.getTitle());
        dto.setPinned(compilation.isPinned());

        dto.setEvents(
                compilation.getEvents()
                        .stream()
                        .map(EventMapper::toShortDto)
                        .toList()
        );

        return dto;
    }
}
