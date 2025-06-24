package ru.hh.blokshnote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hh.blokshnote.entity.Diff;

import java.util.List;

public interface DiffRepository extends JpaRepository<Diff, Long> {

  void deleteByRoomIdIn(List<Long> roomIds);
}
