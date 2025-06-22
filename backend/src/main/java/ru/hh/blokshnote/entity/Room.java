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

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "editor_text", nullable = false)
  private String editorText;

  @Column(name = "editor_language")
  private String editorLanguage;

  @Column(name = "is_closed")
  private boolean isClosed;

  @Column(name = "is_modified_by_code")
  private boolean isModifiedByWritingCode = false;

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

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getEditorText() {
    return editorText;
  }

  public void setEditorText(String editorText) {
    this.editorText = editorText;
  }

  public String getEditorLanguage() {
    return editorLanguage;
  }

  public void setEditorLanguage(String editorLanguage) {
    this.editorLanguage = editorLanguage;
  }

  public boolean isClosed() {
    return isClosed;
  }

  public void setClosed(boolean closed) {
    isClosed = closed;
  }

  public boolean isModifiedByWritingCode() {
    return isModifiedByWritingCode;
  }

  public void setModifiedByWritingCode(boolean modifiedByWritingCode) {
    isModifiedByWritingCode = modifiedByWritingCode;
  }
}
