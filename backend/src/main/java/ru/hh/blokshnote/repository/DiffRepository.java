package ru.hh.blokshnote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hh.blokshnote.entity.Diff;

public interface DiffRepository extends JpaRepository<Diff, Long> {
}
