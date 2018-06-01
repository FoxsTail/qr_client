package com.lis.qr_client.interfaces;

import com.lis.qr_client.pojo.User;

import java.util.List;

public interface IUserDatabaseHandler {
    User getByUsername(String username);
    List<User> getAllUsers();
    void deleteAllUsers();
    void deleteByUsername();
}
