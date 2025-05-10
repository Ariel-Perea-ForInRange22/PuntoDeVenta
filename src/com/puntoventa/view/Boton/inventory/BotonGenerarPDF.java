package com.puntoventa.view.Boton.inventory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop;

public class BotonGenerarPDF extends JButton {
    private DefaultTableModel inventoryModel;

    public BotonGenerarPDF(DefaultTableModel inventoryModel) {
        super("Generar PDF");
        this.inventoryModel = inventoryModel;
        addActionListener(e -> generatePDF());
    }

    private void generatePDF() {
        try {            File inventoryDir = new File("R_INVENTARIO");
            if (!inventoryDir.exists()) {
                inventoryDir.mkdir();
            }
            String fileName = "Inventario_" + new SimpleDateFormat("yyyyMMdd_HH-mm-ss").format(new Date()) + ".pdf";
            File pdfFile = new File(inventoryDir, fileName);
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Agregar título
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte de Inventario", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Agregar fecha
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph date = new Paragraph("Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), dateFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph("\n")); // Espacio

            // Crear tabla
            PdfPTable table = new PdfPTable(5); // 5 columnas
            table.setWidthPercentage(100);
            
            // Encabezados
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            String[] headers = {"Código", "Nombre", "Precio", "Stock", "Estado"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Datos
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (int i = 0; i < inventoryModel.getRowCount(); i++) {
                for (int j = 0; j < inventoryModel.getColumnCount(); j++) {
                    PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(inventoryModel.getValueAt(i, j)), contentFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();            // Mostrar mensaje y abrir el PDF
            JOptionPane.showMessageDialog(this,
                "Reporte de inventario generado exitosamente en:\nR_INVENTARIO/" + fileName,
                "PDF Generado",
                JOptionPane.INFORMATION_MESSAGE);
                
            // Abrir el PDF con el visor predeterminado
            try {
                Desktop.getDesktop().open(pdfFile);
            } catch (IOException ex) {
                // Si falla al abrir, solo mostrar la ubicación del archivo
                System.out.println("PDF guardado en: " + pdfFile.getAbsolutePath());
            }

        } catch (DocumentException | IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error al generar PDF: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}