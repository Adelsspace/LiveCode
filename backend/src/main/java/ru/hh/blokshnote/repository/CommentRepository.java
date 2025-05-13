package ru.hh.blokshnote.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.hh.blokshnote.entity.Comment;
import ru.hh.blokshnote.entity.Room;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  @EntityGraph(attributePaths = {"user"})
  List<Comment> findAllByRoomOrderByCreatedAtAsc(Room room);
}
