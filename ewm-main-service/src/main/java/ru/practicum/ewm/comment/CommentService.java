
package ru.practicum.ewm.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentDto createComment(Long userId, Long eventId,
                                    NewCommentDto request) {

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        return CommentMapper.toDto(saved);
    }

    public List<CommentDto> getCommentsByEvent(Long eventId) {

        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Event not found");
        }

        return commentRepository
                .findByEvent_IdOrderByCreatedDesc(eventId)
                .stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Transactional
    public CommentDto updateComment(
            Long userId,
            Long commentId,
            NewCommentDto request) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the author can edit this comment");
        }

        comment.setText(request.getText());

        Comment saved = commentRepository.save(comment);

        return CommentMapper.toDto(saved);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the author can delete this comment");
        }

        commentRepository.delete(comment);
    }

}
