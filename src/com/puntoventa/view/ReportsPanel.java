package com.puntoventa.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Date;
import java.util.Calendar;
import java.util.stream.Stream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileOutputStream;
import com.puntoventa.controller.ProductController;
import com.puntoventa.controller.SaleController;
import com.puntoventa.model.Product;
import com.puntoventa.model.Sale;
import com.puntoventa.model.SaleDetail;
import com.puntoventa.view.Boton.inventory.BotonGenerarPDF;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ReportsPanel extends JPanel {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final ProductController productController;
    private final SaleController saleController;
    
    private JTabbedPane tabbedPane;
    private DefaultTableModel inventoryModel;
    private JTable inventoryTable;
    private DefaultTableModel salesModel;
    private JTable salesTable;
    private JDateChooser datePicker;

    public ReportsPanel(ProductController productController, SaleController saleController) {
        this.productController = productController;
        this.saleController = saleController;
        initComponents();
        
        // Agregar un ComponentListener para detectar cuando el panel se muestra
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadInventoryData();
                loadSalesData();
            }
        });
        
        // Carga inicial de datos
        loadInventoryData();
        loadSalesData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Initialize main components
        this.tabbedPane = new JTabbedPane();
        
        // Set up Inventory tab
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        String[] columns = {"Código", "Nombre", "Precio", "Stock", "Estado"};
        this.inventoryModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.inventoryTable = new JTable(inventoryModel);
        
        JPanel inventoryButtonPanel = new JPanel();
        JButton refreshButton = new JButton("Actualizar");
        BotonGenerarPDF generatePdfButton = new BotonGenerarPDF(inventoryModel);        
        refreshButton.addActionListener(e -> loadInventoryData());
        
        inventoryButtonPanel.add(refreshButton);
        inventoryButtonPanel.add(generatePdfButton);
        inventoryPanel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        inventoryPanel.add(inventoryButtonPanel, BorderLayout.SOUTH);
        
        // Set up Sales tab
        JPanel salesPanel = new JPanel(new BorderLayout());
        String[] salesColumns = {"ID", "Fecha", "Total", "Cantidad Productos"};
        this.salesModel = new DefaultTableModel(salesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.salesTable = new JTable(salesModel);
        
        salesPanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        
        // Botones del panel de ventas
        JPanel salesButtonPanel = new JPanel();
        this.datePicker = new JDateChooser();
        datePicker.setDate(new Date());
        
        JButton filterButton = new JButton("Últimos 15 días");
        filterButton.addActionListener(e -> filterSalesLast15Days());
        
        JButton salesPdfButton = new JButton("Generar PDF");
        salesPdfButton.addActionListener(e -> generateAndOpenSalesPDF());

        JButton topProductButton = new JButton("Producto más vendido hoy");
        topProductButton.addActionListener(e -> showTopSellingProduct());
        
        salesButtonPanel.add(datePicker);
        salesButtonPanel.add(filterButton);
        salesButtonPanel.add(salesPdfButton);
        salesButtonPanel.add(topProductButton);
        
        salesPanel.add(salesButtonPanel, BorderLayout.SOUTH);
        
        // Add tabs
        tabbedPane.addTab("Inventario", inventoryPanel);
        tabbedPane.addTab("Ventas", salesPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void loadInventoryData() {
        try {
            List<Product> products = productController.getAllProducts();
            inventoryModel.setRowCount(0);
            
            for (Product product : products) {
                Object[] row = {
                    product.getCode(),
                    product.getName(),
                    product.getPrice(),
                    product.getStock(),
                    product.isActive() ? "Activo" : "Inactivo"
                };
                inventoryModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar datos de inventario: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSalesData() {
        try {
            List<Sale> sales = saleController.getAllSales();
            if (sales.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No se encontraron ventas registradas",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            }
            updateSalesTable(sales);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.trim().isEmpty()) {
                errorMsg = "Error desconocido al cargar datos de ventas";
            }
            JOptionPane.showMessageDialog(this,
                "Error al cargar datos de ventas: " + errorMsg,
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterSalesLast15Days() {
        try {
            Date selectedDate = datePicker.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(this,
                    "Por favor seleccione una fecha",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Calculate start and end dates for the last 15 days
            Calendar cal = Calendar.getInstance();
            cal.setTime(selectedDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date endDate = cal.getTime();

            cal.add(Calendar.DAY_OF_MONTH, -14);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Date startDate = cal.getTime();

            // Fetch sales within the date range
            List<Sale> sales = saleController.getSalesByDateRange(startDate, endDate);
            updateSalesTable(sales);

            // Display the date range in a message
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            JOptionPane.showMessageDialog(this,
                "Mostrando ventas desde " + sdf.format(startDate) + " hasta " + sdf.format(endDate),
                "Rango de fechas", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al filtrar ventas: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSalesTable(List<Sale> sales) {
        salesModel.setRowCount(0);
        double totalVentas = 0.0;
        
        for (Sale sale : sales) {
            // Obtener la cantidad total de productos en esta venta
            int totalProducts = 0;
            if (sale.getDetails() != null && !sale.getDetails().isEmpty()) {
                totalProducts = sale.getDetails().stream()
                    .mapToInt(detail -> detail.getQuantity())
                    .sum();
            }
            
            Object[] row = {
                sale.getId(),
                dateFormat.format(sale.getSaleDate()),
                sale.getTotal() != null ? String.format("$%.2f", sale.getTotal().doubleValue()) : "$0.00",
                totalProducts
            };
            salesModel.addRow(row);
            totalVentas += sale.getTotal() != null ? sale.getTotal().doubleValue() : 0.0;
        }
        
        // Si hay ventas, mostrar el total
        if (!sales.isEmpty()) {
            int totalProductos = sales.stream()
                .filter(sale -> sale.getDetails() != null && !sale.getDetails().isEmpty())
                .flatMap(sale -> sale.getDetails().stream())
                .mapToInt(detail -> detail.getQuantity())
                .sum();

            Object[] totalRow = {
                "TOTAL",
                "",
                String.format("$%.2f", totalVentas),
                totalProductos
            };
            salesModel.addRow(totalRow);
        }
    }
      private void generateAndOpenSalesPDF() {
        try {
            // Crear el directorio R_VENTAS si no existe
            File salesDir = new File("R_VENTAS");
            if (!salesDir.exists()) {
                salesDir.mkdir();
            }
            
            // Crear el nombre del archivo con la fecha actual
            String fileName = "ReporteVentas_" + new SimpleDateFormat("yyyyMMdd_HH-mm-ss").format(new Date()) + ".pdf";
            File pdfFile = new File(salesDir, fileName);

            // Crear el documento PDF
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Agregar título
            Font titleFont = new Font(FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Reporte de Ventas", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n")); // Espacio

            // Crear tabla
            PdfPTable table = new PdfPTable(4); // 4 columnas
            table.setWidthPercentage(100);

            // Encabezados
            Font headerFont = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
            Stream.of("ID", "Fecha", "Total", "Cantidad")
                  .forEach(header -> {
                      PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                      cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                      table.addCell(cell);
                  });

            // Agregar datos de ventas
            double totalVentas = 0.0;
            for (int i = 0; i < salesModel.getRowCount(); i++) {
                // Omitir la última fila si es el total
                if (i == salesModel.getRowCount() - 1 && 
                    salesModel.getValueAt(i, 0).toString().equals("TOTAL")) {
                    continue;
                }

                table.addCell(salesModel.getValueAt(i, 0).toString());
                table.addCell(salesModel.getValueAt(i, 1).toString());
                String totalStr = salesModel.getValueAt(i, 2).toString().replace("$", "").trim();
                table.addCell(salesModel.getValueAt(i, 2).toString());
                table.addCell(salesModel.getValueAt(i, 3).toString());
                
                totalVentas += Double.parseDouble(totalStr);
            }

            document.add(table);

            // Agregar total
            document.add(new Paragraph("\n"));
            Paragraph total = new Paragraph(
                "Total de Ventas: $" + String.format("%.2f", totalVentas),
                new Font(FontFamily.HELVETICA, 12, Font.BOLD)
            );
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();

            // Abrir el PDF
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                JOptionPane.showMessageDialog(this,
                    "El PDF ha sido generado pero no se puede abrir automáticamente.\nUbicación: " + pdfFile.getAbsolutePath(),
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            }

            // Mostrar mensaje de éxito incluyendo la ubicación del archivo
            JOptionPane.showMessageDialog(this,
                "Reporte generado exitosamente en:\nR_VENTAS/" + fileName,
                "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al generar el reporte: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTopSellingProduct() {
        try {
            // Obtener las fechas de inicio y fin del día actual
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startDate = cal.getTime();
            
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            Date endDate = cal.getTime();
            
            // Obtener las ventas del día
            List<Sale> sales = saleController.getSalesByDateRange(startDate, endDate);
            
            if (sales.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No hay ventas registradas hoy.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Mapa para contar las cantidades vendidas por producto
            Map<Product, Integer> productQuantities = new HashMap<>();
            
            // Procesar todas las ventas y sus detalles
            for (Sale sale : sales) {
                if (sale.getDetails() != null) {
                    for (SaleDetail detail : sale.getDetails()) {
                        Product product = detail.getProduct();
                        productQuantities.merge(product, detail.getQuantity(), Integer::sum);
                    }
                }
            }
            
            if (productQuantities.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No se encontraron detalles de ventas para hoy.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Encontrar el producto más vendido
            Map.Entry<Product, Integer> maxEntry = Collections.max(
                productQuantities.entrySet(),
                Map.Entry.comparingByValue()
            );
            
            Product topProduct = maxEntry.getKey();
            int quantity = maxEntry.getValue();
            
            // Mostrar la información en un diálogo
            StringBuilder message = new StringBuilder();
            message.append("Producto más vendido hoy:\n\n");
            message.append("Nombre: ").append(topProduct.getName()).append("\n");
            message.append("Código: ").append(topProduct.getCode()).append("\n");
            message.append("Cantidad vendida: ").append(quantity).append(" unidades\n");
            message.append("Precio unitario: $").append(String.format("%.2f", topProduct.getPrice()));
            
            JOptionPane.showMessageDialog(this,
                message.toString(),
                "Producto más vendido", JOptionPane.INFORMATION_MESSAGE);
            
            // Generar el PDF
            generateTopSellingProductPDF(topProduct, quantity);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al obtener el producto más vendido: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateTopSellingProductPDF(Product product, int quantity) {
        try {
            // Crear el directorio ProductoMasVendido si no existe
            File dir = new File("ProductoMasVendido");
            if (!dir.exists()) {
                dir.mkdir();
            }
            
            // Crear el nombre del archivo con la fecha actual
            String fileName = "ProductoMasVendido_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".pdf";
            File pdfFile = new File(dir, fileName);
            
            // Crear el documento PDF
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
            
            // Agregar título
            Font titleFont = new Font(FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Producto Más Vendido del Día", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n")); // Espacio
            
            // Agregar fecha
            Font dateFont = new Font(FontFamily.HELVETICA, 12, Font.NORMAL);
            Paragraph date = new Paragraph("Fecha: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()), dateFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph("\n")); // Espacio
            
            // Crear tabla de información del producto
            PdfPTable table = new PdfPTable(2); // 2 columnas
            table.setWidthPercentage(100);
            
            // Estilo para las celdas de encabezado
            Font headerFont = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
            PdfPCell headerCell;
            
            // Agregar detalles del producto
            String[][] details = {
                {"Código:", product.getCode()},
                {"Nombre:", product.getName()},
                {"Cantidad vendida:", quantity + " unidades"},
                {"Precio unitario:", "$" + String.format("%.2f", product.getPrice())},
                {"Total vendido:", "$" + String.format("%.2f", product.getPrice().multiply(new BigDecimal(quantity)))}
            };
            
            for (String[] row : details) {
                headerCell = new PdfPCell(new Phrase(row[0], headerFont));
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(headerCell);
                table.addCell(new Phrase(row[1], dateFont));
            }
            
            document.add(table);
            document.close();
            
            // Abrir el PDF
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }
            
            // Mostrar mensaje de éxito
            JOptionPane.showMessageDialog(this,
                "Reporte del producto más vendido generado exitosamente en:\nProductoMasVendido/" + fileName,
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al generar el PDF: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
}