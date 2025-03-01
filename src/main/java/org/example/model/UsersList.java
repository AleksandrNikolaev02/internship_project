package org.example.model;

import java.util.List;

public class UsersList {
    private List<User> userList;

    public UsersList(List<User> userList) {
        this.userList = userList;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }
}
