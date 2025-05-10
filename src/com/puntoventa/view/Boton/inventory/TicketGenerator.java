package com.puntoventa.view.Boton.inventory;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.puntoventa.model.SaleDetail;

public class TicketGenerator {
    private static final String TICKETS_FOLDER = "TICKETS";
    private static final float TICKET_WIDTH = 230f; // ~8 cm
    private static final float TICKET_HEIGHT = 600f; // ~20 cm

    public static void generateAndShowTicket(int saleId, Date saleDate, List<SaleDetail> details, BigDecimal total, BigDecimal payment, BigDecimal change) {
        try {
            // Asegurarse de que existe la carpeta TICKETS
            File ticketsFolder = new File(TICKETS_FOLDER);
            if (!ticketsFolder.exists()) {
                ticketsFolder.mkdir();
            }

            // Generar nombre del archivo
            String fileName = String.format("%s/Ticket_%s.pdf", 
                TICKETS_FOLDER,
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
            );

            // Crear el documento PDF con tamaño personalizado
            Rectangle pagesize = new Rectangle(TICKET_WIDTH, TICKET_HEIGHT);
            Document document = new Document(pagesize, 10, 10, 10, 10);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Configurar fuentes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

            // Agregar título
            Paragraph title = new Paragraph("TICKET DE VENTA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Información de la venta
            Paragraph info = new Paragraph();
            info.add(new Chunk("Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(saleDate) + "\n", normalFont));
            info.add(new Chunk("No. Venta: " + saleId + "\n", normalFont));
            document.add(info);
            document.add(Chunk.NEWLINE);

            // Crear tabla de productos
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 1, 1, 1});
            
            // Encabezados
            String[] headers = {"Producto", "Cant", "Precio", "Total"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(3);
                table.addCell(cell);
            }

            // Datos
            for (SaleDetail detail : details) {
                PdfPCell nameCell = new PdfPCell(new Phrase(detail.getProduct().getName(), normalFont));
                nameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(nameCell);

                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(detail.getQuantity()), normalFont));
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(qtyCell);

                PdfPCell priceCell = new PdfPCell(new Phrase(String.format("$%.2f", detail.getUnitPrice()), normalFont));
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(priceCell);

                PdfPCell subtotalCell = new PdfPCell(new Phrase(String.format("$%.2f", detail.getSubtotal()), normalFont));
                subtotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(subtotalCell);
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            // Totales
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(100);
            totalsTable.setWidths(new float[]{1, 1});

            // Total
            PdfPCell labelCell = new PdfPCell(new Phrase("Total:", boldFont));
            labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            labelCell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(labelCell);

            PdfPCell valueCell = new PdfPCell(new Phrase(String.format("$%.2f", total), boldFont));
            valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            valueCell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(valueCell);

            // Pagado
            labelCell = new PdfPCell(new Phrase("Pagado:", normalFont));
            labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            labelCell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(labelCell);

            valueCell = new PdfPCell(new Phrase(String.format("$%.2f", payment), normalFont));
            valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            valueCell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(valueCell);

            // Cambio
            labelCell = new PdfPCell(new Phrase("Cambio:", normalFont));
            labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            labelCell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(labelCell);

            valueCell = new PdfPCell(new Phrase(String.format("$%.2f", change), normalFont));
            valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            valueCell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(valueCell);

            document.add(totalsTable);
            document.add(Chunk.NEWLINE);

            // Mensaje de agradecimiento
            Paragraph thanks = new Paragraph("¡Gracias por su compra!", normalFont);
            thanks.setAlignment(Element.ALIGN_CENTER);
            document.add(thanks);

            document.close();

            // Mostrar el ticket y opciones
            int option = JOptionPane.showConfirmDialog(null,
                "Ticket generado exitosamente.\n¿Desea imprimir el ticket?",
                "Imprimir Ticket",
                JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                printPDF(fileName);
            } else {
                // Si no quiere imprimir, al menos mostrar el PDF
                Desktop.getDesktop().open(new File(fileName));
            }

        } catch (IOException | DocumentException e) {
            JOptionPane.showMessageDialog(null,
                "Error al generar el ticket: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void printPDF(String fileName) {
        try {
            // En lugar de intentar imprimir directamente, mostrar mensaje de éxito
            JOptionPane.showMessageDialog(null,
                "Ticket generado exitosamente en:\n" + fileName,
                "Ticket Generado",
                JOptionPane.INFORMATION_MESSAGE);
                
            // Intentar abrir el archivo con el visor de PDF predeterminado
            try {
                Desktop.getDesktop().open(new File(fileName));
            } catch (IOException ex) {
                // Si falla al abrir, solo mostrar la ubicación del archivo
                System.out.println("Ticket guardado en: " + fileName);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error al procesar el ticket: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}