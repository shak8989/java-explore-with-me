
package ru.practicum.ewm.comment;

public class CommentMapper {

    private CommentMapper() {
    }

    public static CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();

        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setEventId(comment.getEvent().getId());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setCreated(comment.getCreated());

        return dto;
    }
}
