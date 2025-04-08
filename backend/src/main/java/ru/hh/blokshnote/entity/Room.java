package ru.hh.blokshnote.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_uuid", nullable = false, unique = true)
    private UUID roomUuid;

    @Column(name = "admin_token", nullable = false)
    private UUID adminToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expired_at")
    private Instant expiredAt;

    public Room() {
    }

    public Room(UUID roomUuid, UUID adminToken, Instant createdAt, Instant expiredAt) {
        this.roomUuid = roomUuid;
        this.adminToken = adminToken;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getRoomUuid() {
        return roomUuid;
    }

    public void setRoomUuid(UUID roomUuid) {
        this.roomUuid = roomUuid;
    }

    public UUID getAdminToken() {
        return adminToken;
    }

    public void setAdminToken(UUID adminToken) {
        this.adminToken = adminToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }
}
