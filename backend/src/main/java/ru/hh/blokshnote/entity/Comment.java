package ru.hh.blokshnote.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "content", nullable = false)
  private String content;

  @ManyToOne(optional = false)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "is_llm", nullable = false)
  private boolean isLlm;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public Comment() {
  }

  public Long getId() {
    return id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Room getRoom() {
    return room;
  }

  public void setRoom(Room room) {
    this.room = room;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public boolean isLlm() {
    return isLlm;
  }

  public void setLlm(boolean llm) {
    isLlm = llm;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public static class Builder {
    private final Comment comment;

    public Builder() {
      this.comment = new Comment();
    }

    public Builder content(String content) {
      comment.setContent(content);
      return this;
    }

    public Builder room(Room room) {
      comment.setRoom(room);
      return this;
    }

    public Builder user(User user) {
      comment.setUser(user);
      return this;
    }

    public Builder isLlm(boolean isLlm) {
      comment.setLlm(isLlm);
      return this;
    }

    public Builder createdAt(Instant createdAt) {
      comment.setCreatedAt(createdAt);
      return this;
    }

    public Comment build() {
      return comment;
    }
  }
}
