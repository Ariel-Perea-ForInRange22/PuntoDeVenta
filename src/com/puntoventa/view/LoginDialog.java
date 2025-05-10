package com.puntoventa.view;

import com.puntoventa.controller.UserController;
import com.puntoventa.model.User;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean loginSuccessful = false;
    private UserController userController;

    public LoginDialog(Frame owner) {
        super(owner, "Iniciar Sesión", true);
        userController = new UserController();
        initComponents();
        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!loginSuccessful) {
                    System.exit(0);
                }
            }
        });
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username field
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        add(usernameField, gbc);

        // Password field
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        add(passwordField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Iniciar Sesión");
        JButton cancelButton = new JButton("Cancelar");

        loginButton.addActionListener(e -> attemptLogin());
        cancelButton.addActionListener(e -> {
            loginSuccessful = false;
            System.exit(0);
        });

        // Add enter key listener to password field
        passwordField.addActionListener(e -> attemptLogin());

        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            User user = userController.login(username, password);
            if (user != null) {
                loginSuccessful = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos",
                    "Error de autenticación",
                    JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al intentar iniciar sesión: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}