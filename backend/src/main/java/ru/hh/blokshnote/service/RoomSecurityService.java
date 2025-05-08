package ru.hh.blokshnote.service;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.blokshnote.entity.Room;

@Service
public class RoomSecurityService {
  @Transactional(readOnly = true)
  public void verifyAdminToken(Room room, UUID adminToken) {
    if (!room.getAdminToken().equals(adminToken)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin token");
    }
  }
}
