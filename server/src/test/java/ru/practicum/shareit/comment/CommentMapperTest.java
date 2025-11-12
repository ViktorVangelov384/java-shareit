package ru.practicum.shareit.comment;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void toCommentDto_shouldMapCorrectly() {
        User author = new User(1L, "Author", "author@email.com");
        Item item = new Item(1L, "Дрель", "Описание", true, author, null);
        Comment comment = new Comment(1L, "Отличная вещь!", item, author,
                LocalDateTime.of(2023, 12, 1, 10, 0));

        CommentDto result = CommentMapper.toCommentDto(comment);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Отличная вещь!");
        assertThat(result.getAuthorName()).isEqualTo("Author");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2023, 12, 1, 10, 0));
    }
}
