package com.puntoventa.controller;

import com.puntoventa.dao.UserDAO;
import com.puntoventa.dao.UserDAOImpl;
import com.puntoventa.model.User;

public class UserController {
    private UserDAO userDAO;
    private static User currentUser;

    public UserController() {
        this.userDAO = new UserDAOImpl();
    }

    public User login(String username, String password) throws Exception {
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            currentUser = user;
        }
        return user;
    }

    public void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isUserLoggedIn() {
        return currentUser != null;
    }

    public static boolean isCurrentUserAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}