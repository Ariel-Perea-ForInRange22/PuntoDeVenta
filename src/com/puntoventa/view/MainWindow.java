package com.puntoventa.view;

import com.puntoventa.controller.ProductController;
import com.puntoventa.controller.SaleController;
import com.puntoventa.controller.UserController;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class MainWindow extends JFrame {
    private final UserController userController;
    private final ProductController productController;
    private final SaleController saleController;
    private JMenuBar menuBar;
    private ProductPanel productPanel;
    private SalesPanel salesPanel;
    private ReportsPanel reportsPanel;
    
    public MainWindow() {
        // Initialize controllers first
        userController = new UserController();
        productController = new ProductController();
        saleController = new SaleController();
        
        // Configure basic window properties
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        
        // Show login dialog before initializing any components
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setVisible(true);
        
        if (!loginDialog.isLoginSuccessful()) {
            dispose(); // Properly dispose of the window
            System.exit(0);
            return;
        }
        
        // Initialize UI in EDT
        SwingUtilities.invokeLater(() -> {
            try {
                setTitle("Sistema Punto de Venta - Usuario: " + UserController.getCurrentUser().getUsername());
                initComponents();
                createLayout();
                validate();
                repaint();
            } catch (Exception e) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, "Error initializing components", e);
                JOptionPane.showMessageDialog(this,
                    "Error al inicializar la aplicación: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                dispose();
                System.exit(1);
            }
        });
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int confirm = JOptionPane.showConfirmDialog(
                    MainWindow.this,
                    "¿Está seguro que desea salir?",
                    "Confirmar Salida",
                    JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }
    
    private void initComponents() {
        // Initialize menu bar
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Archivo");
        JMenu productsMenu = new JMenu("Productos");
        JMenu salesMenu = new JMenu("Ventas");
        JMenu reportsMenu = new JMenu("Reportes");
        
        // Crear menú de usuario
        JMenu userMenu = new JMenu("Usuario");        JMenuItem logoutItem = new JMenuItem("Cerrar Sesión");
        logoutItem.addActionListener(_ -> logout());
        userMenu.add(logoutItem);
        
        JMenuItem exitItem = new JMenuItem("Salir");
        exitItem.addActionListener(_ -> {
            int confirm = JOptionPane.showConfirmDialog(
                MainWindow.this,
                "¿Está seguro que desea salir?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        
        JMenuItem productManagementItem = new JMenuItem("Gestionar Productos");
        productManagementItem.addActionListener(_ -> showProductPanel());
        
        JMenuItem newSaleItem = new JMenuItem("Nueva Venta");
        newSaleItem.addActionListener(_ -> showSalesPanel());
        
        // Agregar items al menú de reportes
        JMenuItem inventoryReportItem = new JMenuItem("Reporte de Inventario");
        JMenuItem salesReportItem = new JMenuItem("Reporte de Ventas");
        
        inventoryReportItem.addActionListener(_ -> showReportsPanel(0)); // 0 para inventario
        salesReportItem.addActionListener(_ -> showReportsPanel(1)); // 1 para ventas
        
        // Solo mostrar gestión de productos y reportes si es administrador
        boolean isAdmin = UserController.isCurrentUserAdmin();
        productsMenu.setVisible(isAdmin);
        reportsMenu.setVisible(isAdmin);
        
        fileMenu.add(exitItem);
        productsMenu.add(productManagementItem);
        salesMenu.add(newSaleItem);
        reportsMenu.add(inventoryReportItem);
        reportsMenu.add(salesReportItem);
        
        menuBar.add(fileMenu);
        menuBar.add(productsMenu);
        menuBar.add(salesMenu);
        menuBar.add(reportsMenu);
        menuBar.add(userMenu);
        
        // Initialize panels
        productPanel = new ProductPanel();
        salesPanel = new SalesPanel();
        reportsPanel = new ReportsPanel(productController, saleController);
    }
    
    private void createLayout() {
        setJMenuBar(menuBar);
        setLayout(new CardLayout());
        add(productPanel, "products");
        add(salesPanel, "sales");
        add(reportsPanel, "reports");
        
        // Mostrar panel de ventas por defecto
        showSalesPanel();
    }
    
    private void showProductPanel() {
        if (!UserController.isCurrentUserAdmin()) {
            JOptionPane.showMessageDialog(this,
                "No tiene permisos para acceder a esta función",
                "Acceso Denegado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "products");
    }
    
    private void showSalesPanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "sales");
    }
    
    private void showReportsPanel(int tabIndex) {
        if (!UserController.isCurrentUserAdmin()) {
            JOptionPane.showMessageDialog(this,
                "No tiene permisos para acceder a esta función",
                "Acceso Denegado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "reports");
        reportsPanel.getTabbedPane().setSelectedIndex(tabIndex);
    }
    
    private void logout() {
        userController.logout();
        dispose();
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, "Error setting look and feel", ex);
            }
            new MainWindow().setVisible(true);        });
    }
}