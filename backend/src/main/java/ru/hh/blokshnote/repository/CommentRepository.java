package ru.hh.blokshnote.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.hh.blokshnote.entity.Comment;
import ru.hh.blokshnote.entity.Room;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  @EntityGraph(attributePaths = {"user"})
  List<Comment> findAllByRoomOrderByCreatedAtAsc(Room room);

  void deleteByRoomIdIn(List<Long> roomIds);
}
