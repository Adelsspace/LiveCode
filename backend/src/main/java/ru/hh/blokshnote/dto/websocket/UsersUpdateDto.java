package ru.hh.blokshnote.dto.websocket;

import java.util.List;

public class UsersUpdateDto {
    private List<UserState> users;

    public UsersUpdateDto() {
    }

    public UsersUpdateDto(List<UserState> users) {
        this.users = users;
    }

    public List<UserState> getUsers() {
        return users;
    }

    public void setUsers(List<UserState> users) {
        this.users = users;
    }
}