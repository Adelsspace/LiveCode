package ru.hh.blokshnote.dto.room.response;

import java.util.UUID;

public class RoomDto {
    private UUID roomUuid;
    private String status;

    public RoomDto(UUID roomUuid, String status) {
        this.roomUuid = roomUuid;
        this.status = status;
    }

    public UUID getRoomUuid() {
        return roomUuid;
    }

    public void setRoomUuid(UUID roomUuid) {
        this.roomUuid = roomUuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
