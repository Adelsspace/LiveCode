package ru.hh.blokshnote.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import ru.hh.blokshnote.utility.colors.UserColorUtil;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "is_admin", nullable = false)
  private boolean isAdmin;

  @ManyToOne
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @Column(name = "user_color", nullable = false)
  private String userColor;

  public User() {
  }

  public User(String name, boolean isAdmin, Room room) {
    this.name = name;
    this.isAdmin = isAdmin;
    this.room = room;
    this.userColor = UserColorUtil.generateUserColor(name);
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isAdmin() {
    return isAdmin;
  }

  public void setAdmin(boolean admin) {
    isAdmin = admin;
  }

  public Room getRoom() {
    return room;
  }

  public void setRoom(Room room) {
    this.room = room;
  }

  public String getUserColor() {
    return userColor;
  }

  public void setUserColor(String userColor) {
    this.userColor = userColor;
  }

  @PrePersist
  private void ensureColorIsGenerated() {
    if (this.userColor == null || this.userColor.isEmpty()) {
      this.userColor = UserColorUtil.generateUserColor(this.name);
    }
  }
}