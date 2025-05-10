package com.puntoventa.dao;

import com.puntoventa.model.User;

public interface UserDAO {
    User authenticate(String username, String password) throws Exception;
    void insert(User user) throws Exception;
    void update(User user) throws Exception;
    void delete(int id) throws Exception;
    User findById(int id) throws Exception;
    User findByUsername(String username) throws Exception;
}