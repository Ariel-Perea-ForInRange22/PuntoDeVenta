package com.puntoventa.view;

import com.puntoventa.controller.CategoryController;
import com.puntoventa.controller.ProductController;
import com.puntoventa.controller.SaleController;
import com.puntoventa.controller.UserController;
import com.puntoventa.model.Category;
import com.puntoventa.model.Product;
import com.puntoventa.model.Sale;
import com.puntoventa.model.SaleDetail;
import com.puntoventa.view.Boton.inventory.TicketGenerator;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SalesPanel extends JPanel {
    private JTextField barcodeField;
    private JTable cartTable;
    private JTable productsTable;
    private DefaultTableModel cartModel;
    private DefaultTableModel productsModel;
    private JLabel totalLabel;
    private ProductController productController;
    private CategoryController categoryController;
    private JComboBox<Category> categoryCombo;
    private BigDecimal total;
    private List<SaleItem> cartItems;

    private class SaleItem {
        Product product;
        int quantity;
        BigDecimal subtotal;

        SaleItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
            this.subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }

    public SalesPanel() {
        productController = new ProductController();
        categoryController = new CategoryController();
        cartItems = new ArrayList<>();
        total = BigDecimal.ZERO;
        initComponents();
        createLayout();
        loadCategories();
    }

    private void initComponents() {
        // Initialize barcode field
        barcodeField = new JTextField(15);
        barcodeField.addActionListener(e -> addProductToCart());

        // Initialize category combo
        categoryCombo = new JComboBox<>();
        categoryCombo.addActionListener(e -> filterProductsByCategory());

        // Initialize products table
        String[] productColumns = {"ID", "Código", "Nombre", "Precio", "Stock"};
        productsModel = new DefaultTableModel(productColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productsTable = new JTable(productsModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    addSelectedProductToCart();
                }
            }
        });

        // Initialize cart table
        String[] cartColumns = {"Código", "Producto", "Precio Unit.", "Cantidad", "Subtotal"};
        cartModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 3) {
                updateQuantity(e.getFirstRow());
            }
        });

        // Initialize total label
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
    }

    private void createLayout() {
        setLayout(new BorderLayout(10, 10));

        // Top panel for barcode and category filter
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Código de barras: "));
        topPanel.add(barcodeField);
        topPanel.add(new JLabel("Filtrar por categoría: "));
        topPanel.add(categoryCombo);

        // Center panel split between products and cart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Products panel (left side)
        JPanel productsPanel = new JPanel(new BorderLayout());
        productsPanel.add(new JLabel("Productos disponibles:"), BorderLayout.NORTH);
        productsPanel.add(new JScrollPane(productsTable), BorderLayout.CENTER);
        
        // Cart panel (right side)
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.add(new JLabel("Carrito de compras:"), BorderLayout.NORTH);
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        splitPane.setLeftComponent(productsPanel);
        splitPane.setRightComponent(cartPanel);
        splitPane.setResizeWeight(0.5);

        // Bottom panel for total and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton finishButton = new JButton("Finalizar Venta");
        JButton cancelButton = new JButton("Cancelar Venta");
        
        finishButton.addActionListener(e -> finishSale());
        cancelButton.addActionListener(e -> cancelSale());

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(finishButton);

        bottomPanel.add(totalLabel, BorderLayout.WEST);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        // Add panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
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

    private void filterProductsByCategory() {
        try {
            Category selectedCategory = (Category) categoryCombo.getSelectedItem();
            List<Product> products = productController.getAllProducts();
            productsModel.setRowCount(0);
            
            for (Product product : products) {
                if (selectedCategory.getId() == 0 || 
                    (product.getCategory() != null && 
                     product.getCategory().getId() == selectedCategory.getId())) {
                    Object[] row = {
                        product.getId(),
                        product.getCode(),
                        product.getName(),
                        product.getPrice(),
                        product.getStock()
                    };
                    productsModel.addRow(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al filtrar productos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSelectedProductToCart() {
        int row = productsTable.getSelectedRow();
        if (row >= 0) {
            try {
                int id = (int) productsTable.getValueAt(row, 0);
                Product product = productController.getProductById(id);
                if (product != null && product.getStock() > 0) {
                    addOrUpdateCartItem(product);
                    updateTotal();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Producto sin existencias",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al agregar producto: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addProductToCart() {
        String barcode = barcodeField.getText().trim();
        if (!barcode.isEmpty()) {
            try {
                Product product = productController.getProductByCode(barcode);
                if (product != null) {
                    if (product.getStock() > 0) {
                        addOrUpdateCartItem(product);
                        barcodeField.setText("");
                        updateTotal();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Producto sin existencias",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Producto no encontrado",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al agregar producto: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addOrUpdateCartItem(Product product) {
        // Check if product already exists in cart
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (cartModel.getValueAt(i, 0).equals(product.getCode())) {
                int currentQty = (int) cartModel.getValueAt(i, 3);
                if (currentQty < product.getStock()) {
                    cartModel.setValueAt(currentQty + 1, i, 3);
                    updateQuantity(i);
                }
                return;
            }
        }

        // Add new product to cart
        SaleItem item = new SaleItem(product, 1);
        cartItems.add(item);
        Object[] row = {
            product.getCode(),
            product.getName(),
            product.getPrice(),
            1,
            item.subtotal
        };
        cartModel.addRow(row);
    }

    private void updateQuantity(int row) {
        try {
            String code = (String) cartModel.getValueAt(row, 0);
            int newQty = Integer.parseInt(cartModel.getValueAt(row, 3).toString());
            
            // Find the corresponding product and validate stock
            Product product = productController.getProductByCode(code);
            if (product != null) {
                if (newQty <= 0) {
                    cartModel.removeRow(row);
                    cartItems.remove(row);
                } else if (newQty > product.getStock()) {
                    JOptionPane.showMessageDialog(this,
                        "Cantidad excede el stock disponible",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    cartModel.setValueAt(1, row, 3);
                    newQty = 1;
                }
                
                // Update subtotal
                BigDecimal price = (BigDecimal) cartModel.getValueAt(row, 2);
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(newQty));
                cartModel.setValueAt(subtotal, row, 4);
                
                // Update cart item
                cartItems.get(row).quantity = newQty;
                cartItems.get(row).subtotal = subtotal;
                
                updateTotal();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al actualizar cantidad: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTotal() {
        total = BigDecimal.ZERO;
        for (SaleItem item : cartItems) {
            total = total.add(item.subtotal);
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void finishSale() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El carrito está vacío",
                "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Crear diálogo de pago
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog paymentDialog = new JDialog(parentFrame, "Pago", true);
        paymentDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        paymentDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Mostrar total
        gbc.gridx = 0; gbc.gridy = 0;
        paymentDialog.add(new JLabel("Total a pagar: "), gbc);
        gbc.gridx = 1;
        JLabel totalPayLabel = new JLabel(String.format("$%.2f", total));
        totalPayLabel.setFont(new Font("Arial", Font.BOLD, 16));
        paymentDialog.add(totalPayLabel, gbc);

        // Campo para cantidad recibida
        gbc.gridx = 0; gbc.gridy = 1;
        paymentDialog.add(new JLabel("Cantidad recibida: "), gbc);
        gbc.gridx = 1;
        JTextField paymentField = new JTextField(10);
        paymentDialog.add(paymentField, gbc);

        // Label para el cambio
        gbc.gridx = 0; gbc.gridy = 2;
        paymentDialog.add(new JLabel("Cambio: "), gbc);
        gbc.gridx = 1;
        JLabel changeLabel = new JLabel("$0.00");
        changeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        paymentDialog.add(changeLabel, gbc);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("Confirmar");
        JButton cancelButton = new JButton("Cancelar");

        // Calcular cambio en tiempo real mientras se escribe
        paymentField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateChange() {
                try {
                    String text = paymentField.getText().trim();
                    if (!text.isEmpty()) {
                        BigDecimal payment = new BigDecimal(text);
                        if (payment.compareTo(total) >= 0) {
                            BigDecimal change = payment.subtract(total);
                            changeLabel.setText(String.format("$%.2f", change));
                            changeLabel.setForeground(Color.BLACK);
                            confirmButton.setEnabled(true);
                        } else {
                            changeLabel.setText("Pago insuficiente");
                            changeLabel.setForeground(Color.RED);
                            confirmButton.setEnabled(false);
                        }
                    } else {
                        changeLabel.setText("$0.00");
                        changeLabel.setForeground(Color.BLACK);
                        confirmButton.setEnabled(false);
                    }
                } catch (NumberFormatException ex) {
                    changeLabel.setText("Cantidad inválida");
                    changeLabel.setForeground(Color.RED);
                    confirmButton.setEnabled(false);
                }
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }
        });

        // También mantener el listener para la tecla Enter
        paymentField.addActionListener(e -> {
            if (confirmButton.isEnabled()) {
                confirmButton.doClick();
            }
        });

        confirmButton.setEnabled(false);
        confirmButton.addActionListener(e -> {
            try {
                BigDecimal payment = new BigDecimal(paymentField.getText().trim());
                if (payment.compareTo(total) >= 0) {
                    BigDecimal change = payment.subtract(total);
                    
                    // Actualizar el stock de los productos vendidos
                    boolean stockUpdateSuccess = true;
                    StringBuilder errorMessage = new StringBuilder();
                    
                    try {
                        // Crear la venta en la base de datos
                        Sale sale = new Sale();
                        sale.setSaleDate(new Date());
                        sale.setTotal(total);
                        sale.setUser(UserController.getCurrentUser());
                        
                        List<SaleDetail> saleDetails = new ArrayList<>();
                        for (SaleItem item : cartItems) {
                            SaleDetail detail = new SaleDetail();
                            detail.setSale(sale);
                            detail.setProduct(item.product);
                            detail.setQuantity(item.quantity);
                            detail.setUnitPrice(item.product.getPrice());
                            saleDetails.add(detail);
                            
                            // Actualizar stock
                            int newStock = item.product.getStock() - item.quantity;
                            productController.updateProductStock(item.product.getId(), newStock);
                            item.product.setStock(newStock);
                        }
                        sale.setDetails(saleDetails);
                        
                        // Guardar la venta
                        SaleController saleController = new SaleController();
                        saleController.saveSale(sale);
                        
                        paymentDialog.dispose();
                        
                        // Generar y mostrar el ticket
                        TicketGenerator.generateAndShowTicket(
                            sale.getId(),
                            sale.getSaleDate(),
                            saleDetails,
                            total,
                            payment,
                            change
                        );
                        
                        clearCart();
                        
                    } catch (Exception ex) {
                        stockUpdateSuccess = false;
                        errorMessage.append("Error al procesar la venta: ").append(ex.getMessage());
                    }
                    
                    if (!stockUpdateSuccess) {
                        JOptionPane.showMessageDialog(paymentDialog,
                            errorMessage.toString(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(paymentDialog,
                    "Por favor, ingrese una cantidad válida",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> paymentDialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        paymentDialog.add(buttonPanel, gbc);

        // Configurar el tamaño y posición del diálogo
        paymentDialog.pack();
        paymentDialog.setLocationRelativeTo(parentFrame);
        paymentField.requestFocusInWindow(); // Dar foco al campo de pago
        paymentDialog.setVisible(true);
    }

    private void cancelSale() {
        if (!cartItems.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea cancelar la venta?",
                "Confirmar Cancelación",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                clearCart();
            }
        }
    }

    private void clearCart() {
        cartItems.clear();
        cartModel.setRowCount(0);
        total = BigDecimal.ZERO;
        totalLabel.setText("Total: $0.00");
        barcodeField.setText("");
        barcodeField.requestFocus();
    }
}