package ru.hh.blokshnote.entity;

import jakarta.persistence.*;

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

  public User() {
  }

  public User(String name, boolean isAdmin, Room room) {
    this.name = name;
    this.isAdmin = isAdmin;
    this.room = room;
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
}