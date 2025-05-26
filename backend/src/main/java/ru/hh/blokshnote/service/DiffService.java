package ru.hh.blokshnote.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.blokshnote.entity.Diff;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.repository.DiffRepository;

@Service
public class DiffService {
  private final DiffRepository diffRepository;
  private final DiffMatchPatch diffMatchPatch;

  public DiffService(DiffRepository diffRepository) {
    this.diffRepository = diffRepository;
    this.diffMatchPatch = new DiffMatchPatch();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void makeDiff(Room oldRoom, String newState) {
    String oldState = oldRoom.getEditorText();
    List<DiffMatchPatch.Patch> patches = diffMatchPatch.patchMake(oldState, newState);
    String content = diffMatchPatch.patchToText(patches);
    String decodedContent = URLDecoder.decode(content, StandardCharsets.UTF_8);
    diffRepository.save(new Diff(decodedContent, oldRoom));
  }
}
