package ru.hh.blokshnote.mapper;

import org.mapstruct.Mapper;
import ru.hh.blokshnote.dto.room.response.RoomStateDto;
import ru.hh.blokshnote.entity.Room;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface RoomMapper {

  RoomStateDto toRoomStateDto(Room room, Collection<String> users);
}
