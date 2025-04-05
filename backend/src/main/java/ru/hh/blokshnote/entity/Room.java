package ru.hh.blokshnote.entity;

import jakarta.persistence.*;

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
  private String adminToken;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "expired_at")
  private Instant expiredAt;

  public Room() {
  }

  public Room(UUID roomUuid, String adminToken, Instant createdAt, Instant expiredAt) {
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

  public String getAdminToken() {
    return adminToken;
  }

  public void setAdminToken(String adminToken) {
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
