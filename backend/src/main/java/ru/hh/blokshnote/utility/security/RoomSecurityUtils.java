package ru.hh.blokshnote.utility.security;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.blokshnote.entity.Room;

public class RoomSecurityUtils {
  public static void verifyAdminToken(Room room, UUID adminToken) {
    if (!room.getAdminToken().equals(adminToken)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin token");
    }
  }
}
