package com.puntoventa.view;

import com.puntoventa.controller.CategoryController;
import com.puntoventa.controller.ProductController;
import com.puntoventa.controller.UserController;
import com.puntoventa.model.Category;
import com.puntoventa.model.Product;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ProductPanel extends JPanel {
    private ProductController controller;
    private CategoryController categoryController;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<Category> categoryCombo;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton categoryButton;

    public ProductPanel() {
        controller = new ProductController();
        categoryController = new CategoryController();
        initComponents();
        createLayout();
        loadCategories();
        loadProducts();
    }

    private void initComponents() {
        // Create table model with columns
        String[] columns = {"ID", "Código", "Nombre", "Descripción", "Precio", "Stock", "Categoría", "Activo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Create buttons, search field and category combo
        searchField = new JTextField(20);
        categoryCombo = new JComboBox<>();
        addButton = new JButton("Agregar");
        editButton = new JButton("Editar");
        deleteButton = new JButton("Eliminar");
        refreshButton = new JButton("Actualizar");
        categoryButton = new JButton("Gestionar Categorías");

        // Add action listeners
        addButton.addActionListener(e -> showProductDialog(null));
        editButton.addActionListener(e -> editSelectedProduct());
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        refreshButton.addActionListener(e -> loadProducts());
        searchField.addActionListener(e -> searchProducts());
        categoryButton.addActionListener(e -> showCategoryDialog());
        categoryCombo.addActionListener(e -> filterProducts());

        // Configurar visibilidad según el rol
        boolean isAdmin = UserController.isCurrentUserAdmin();
        addButton.setVisible(isAdmin);
        editButton.setVisible(isAdmin);
        deleteButton.setVisible(isAdmin);
        categoryButton.setVisible(isAdmin);
    }

    private void createLayout() {
        setLayout(new BorderLayout(10, 10));

        // Panel superior para búsqueda y filtros
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        
        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Buscar: "));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Filtrar por categoría: "));
        searchPanel.add(categoryCombo);
        searchPanel.add(refreshButton);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(categoryButton);
        
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        // Agregar componentes al panel principal
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(productTable), BorderLayout.CENTER);
    }

    private void loadCategories() {
        try {
            categoryCombo.removeAllItems();
            categoryCombo.addItem(new Category(0, "Todas las categorías", ""));
            List<Category> categories = categoryController.getAllCategories();
            for (Category category : categories) {
                categoryCombo.addItem(category);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar categorías: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterProducts() {
        try {
            Category selectedCategory = (Category) categoryCombo.getSelectedItem();
            String searchText = searchField.getText().trim();
            List<Product> products;
            
            if (searchText.isEmpty()) {
                products = controller.getAllProducts();
            } else {
                products = controller.searchProducts(searchText);
            }
            
            // Filtrar por categoría si no es "Todas las categorías"
            if (selectedCategory != null && selectedCategory.getId() != 0) {
                products = products.stream()
                    .filter(p -> p.getCategory() != null && 
                               p.getCategory().getId() == selectedCategory.getId())
                    .collect(Collectors.toList());
            }
            
            updateTableModel(products);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al filtrar productos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProducts() {
        try {
            List<Product> products = controller.getAllProducts();
            updateTableModel(products);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar productos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchProducts() {
        filterProducts(); // Usar el mismo método de filtrado
    }

    private void updateTableModel(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product product : products) {
            Object[] row = {
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory() != null ? product.getCategory().getName() : "Sin categoría",
                product.isActive()
            };
            tableModel.addRow(row);
        }
    }

    private void showProductDialog(Product product) {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, 
                                   product == null ? "Nuevo Producto" : "Editar Producto", 
                                   true);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields
        JTextField codeField = new JTextField(20);
        JTextField nameField = new JTextField(20);
        JTextField descField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JTextField stockField = new JTextField(20);
        JComboBox<Category> categoryCombo = new JComboBox<>();
        JCheckBox activeCheck = new JCheckBox("Activo", true);

        // Load categories into combo box
        try {
            List<Category> categories = categoryController.getAllCategories();
            for (Category category : categories) {
                categoryCombo.addItem(category);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar categorías: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        // If editing, populate fields
        if (product != null) {
            codeField.setText(product.getCode());
            nameField.setText(product.getName());
            descField.setText(product.getDescription());
            priceField.setText(product.getPrice().toString());
            stockField.setText(String.valueOf(product.getStock()));
            activeCheck.setSelected(product.isActive());
            categoryCombo.setSelectedItem(product.getCategory());
        }

        // Add components to panel
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1;
        panel.add(codeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        panel.add(descField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Precio:"), gbc);
        gbc.gridx = 1;
        panel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1;
        panel.add(stockField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        panel.add(categoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(activeCheck, gbc);

        // Add buttons
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                Product newProduct = product == null ? new Product() : product;
                newProduct.setCode(codeField.getText());
                newProduct.setName(nameField.getText());
                newProduct.setDescription(descField.getText());
                newProduct.setPrice(new BigDecimal(priceField.getText()));
                newProduct.setStock(Integer.parseInt(stockField.getText()));
                newProduct.setCategory((Category) categoryCombo.getSelectedItem());
                newProduct.setActive(activeCheck.isSelected());

                if (product == null) {
                    controller.addProduct(newProduct);
                } else {
                    controller.updateProduct(newProduct);
                }
                loadProducts();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error al guardar el producto: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void editSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int id = (int) productTable.getValueAt(selectedRow, 0);
                Product product = controller.getProductById(id);
                if (product != null) {
                    showProductDialog(product);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al cargar el producto: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor, seleccione un producto para editar.",
                "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar este producto?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int id = (int) productTable.getValueAt(selectedRow, 0);
                    controller.deleteProduct(id);
                    loadProducts();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "Error al eliminar el producto: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un producto para eliminar.",
                "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showCategoryDialog() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, "Gestionar Categorías", true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Create table for categories
        String[] columns = {"ID", "Nombre", "Descripción"};
        DefaultTableModel categoryModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable categoryTable = new JTable(categoryModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Create buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addCatButton = new JButton("Agregar");
        JButton editCatButton = new JButton("Editar");
        JButton deleteCatButton = new JButton("Eliminar");

        buttonPanel.add(addCatButton);
        buttonPanel.add(editCatButton);
        buttonPanel.add(deleteCatButton);

        // Load categories immediately
        loadCategories(categoryModel);

        // Add action listeners
        addCatButton.addActionListener(e -> {
            try {
                showCategoryEditDialog(null, categoryModel);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error al mostrar el diálogo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        editCatButton.addActionListener(e -> {
            int row = categoryTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) categoryModel.getValueAt(row, 0);
                try {
                    Category category = categoryController.getCategoryById(id);
                    if (category != null) {
                        showCategoryEditDialog(category, categoryModel);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Error al cargar la categoría: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Por favor, seleccione una categoría para editar",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        deleteCatButton.addActionListener(e -> {
            int row = categoryTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) categoryModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(dialog,
                    "¿Está seguro de que desea eliminar esta categoría?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        categoryController.deleteCategory(id);
                        loadCategories(categoryModel);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al eliminar la categoría: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Por favor, seleccione una categoría para eliminar",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        dialog.add(new JScrollPane(categoryTable), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
    }

    private void showCategoryEditDialog(Category category, DefaultTableModel categoryModel) {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame,
            category == null ? "Nueva Categoría" : "Editar Categoría",
            true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields
        JTextField nameField = new JTextField(20);
        JTextField descField = new JTextField(20);

        if (category != null) {
            nameField.setText(category.getName());
            descField.setText(category.getDescription());
        }

        // Add components
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        dialog.add(descField, gbc);

        // Add buttons
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                Category cat = category == null ? new Category() : category;
                cat.setName(nameField.getText());
                cat.setDescription(descField.getText());

                if (category == null) {
                    categoryController.addCategory(cat);
                } else {
                    categoryController.updateCategory(cat);
                }

                loadCategories(categoryModel);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error al guardar la categoría: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private void loadCategories(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<Category> categories = categoryController.getAllCategories();
            for (Category category : categories) {
                Object[] row = {
                    category.getId(),
                    category.getName(),
                    category.getDescription()
                };
                model.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar categorías: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}