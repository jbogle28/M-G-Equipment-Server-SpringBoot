package com.java.scheduler.util;

import com.java.scheduler.domain.Invoice;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoiceReportPDFExporter {

    public static void generateReport(List<Invoice> invoices, LocalDateTime start, LocalDateTime end, String filePath) {
        try {
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.LETTER);

            PdfFont bold = PdfFontFactory.createFont("Helvetica-Bold");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            // --- PROFESSIONAL BRANDING HEADER ---
            document.add(new Paragraph("M&G EQUIPMENT")
                    .setFont(bold)
                    .setFontSize(22)
                    .setFontColor(new DeviceRgb(37, 99, 235)) // Matching your blue accent
                    .setTextAlignment(TextAlignment.LEFT));
            
            document.add(new Paragraph("Official Financial Summary Report")
                    .setFontSize(12)
                    .setItalic()
                    .setMarginBottom(10));

            document.add(new Paragraph("Report Period: " + start.format(dtf) + " to " + end.format(dtf))
                    .setFontSize(10));
            document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(dtf))
                    .setFontSize(10)
                    .setMarginBottom(20));

            // --- FINANCIAL SUMMARY ---
            double paidTotal = invoices.stream()
                    .filter(i -> "PAID".equalsIgnoreCase(i.getStatus()))
                    .mapToDouble(i -> i.getTotal() != null ? i.getTotal() : 0.0).sum();

            Table summaryTable = new Table(2).useAllAvailableWidth().setMarginBottom(20);
            summaryTable.addCell(new Cell().add(new Paragraph("Total Revenue Collected").setFont(bold)));
            summaryTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", paidTotal))
                    .setTextAlignment(TextAlignment.RIGHT)));
            document.add(summaryTable);

            // --- DATA TABLE ---
            float[] columnWidths = {1, 3, 2, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
            
            // Header Row
            String[] headers = {"ID", "Client Name", "Date", "Status", "Amount"};
            for (String header : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(header).setFont(bold).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(ColorConstants.DARK_GRAY));
            }

            for (Invoice invoice : invoices) {
                table.addCell(String.valueOf(invoice.getInvoiceId()));
                table.addCell(invoice.getClientName() != null ? invoice.getClientName() : "N/A");
                table.addCell(invoice.getCreationDate() != null ? invoice.getCreationDate().format(dtf) : "N/A");
                table.addCell(invoice.getStatus());
                table.addCell("$" + String.format("%.2f", invoice.getTotal()));
            }

            document.add(table);
            document.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}