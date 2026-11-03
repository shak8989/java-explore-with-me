
package ru.practicum.ewm.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentDto request) {

        return commentService.createComment(userId, eventId, request);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getComments(
            @PathVariable Long eventId) {

        return commentService.getCommentsByEvent(eventId);
    }

    @PatchMapping("/users/{userId}/comments/{commentId}")
    public CommentDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody NewCommentDto request) {

        return commentService.updateComment(
                userId, commentId, request);
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId) {

        commentService.deleteComment(userId, commentId);
    }
}
