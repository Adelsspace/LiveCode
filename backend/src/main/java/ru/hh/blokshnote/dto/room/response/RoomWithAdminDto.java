package ru.hh.blokshnote.dto.room.response;

import java.util.UUID;

public class RoomWithAdminDto {
    private final UUID adminToken;

    public RoomWithAdminDto(UUID adminToken) {
        this.adminToken = adminToken;
    }

    public UUID getAdminToken() {
        return adminToken;
    }
}
