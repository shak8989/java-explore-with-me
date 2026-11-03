
package ru.practicum.ewm.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewCommentDto {

    @NotBlank
    @Size(max = 2000)
    private String text;
}
