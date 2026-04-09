package com.java.scheduler.util;

import com.java.scheduler.domain.Invoice;
import com.java.scheduler.domain.Asset;
import com.java.scheduler.domain.Customer;
import com.java.scheduler.domain.User;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class InvoicePDFExporter {

    public static void generateInvoicePDF(Invoice invoice, String filePath) {
        try {
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.LETTER);
            document.setMargins(36, 36, 36, 36);

            PdfFont bold = PdfFontFactory.createFont("Helvetica-Bold");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

            // --- HEADER SECTION ---
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            
            // Company Branding (Left side)
            headerTable.addCell(new Cell().add(new Paragraph("M&G EQUIPMENT")
                    .setFont(bold).setFontSize(20).setFontColor(new DeviceRgb(37, 99, 235))) // Blue accent
                    .setBorder(Border.NO_BORDER));
            
            // Invoice Title (Right side)
            headerTable.addCell(new Cell().add(new Paragraph("INVOICE")
                    .setFont(bold).setFontSize(26).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER));
            
            document.add(headerTable);

            // --- INFO BAR (ID and Dates) ---
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            infoTable.setMarginTop(20);
            
            Cell invoiceDetails = new Cell().add(new Paragraph("Invoice #: " + invoice.getInvoiceId()).setFont(bold))
                    .add(new Paragraph("Date Issued: " + invoice.getCreationDate().format(dtf)).setFontSize(10))
                    .add(new Paragraph("Status: " + invoice.getStatus()).setFont(bold).setFontColor(
                            "PAID".equals(invoice.getStatus()) ? ColorConstants.GREEN : ColorConstants.RED))
                    .setBorder(Border.NO_BORDER);
            
            infoTable.addCell(invoiceDetails);
            infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)); // Empty spacer
            
            document.add(infoTable);

            // --- BILL TO / PROCESSED BY ---
            Table billToTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            billToTable.setMarginTop(20);

            Customer customer = invoice.getBooking().getCustomer();
            User agent = invoice.getBooking().getCreator();

            billToTable.addCell(new Cell().add(new Paragraph("BILL TO").setFont(bold).setFontSize(12).setUnderline())
                    .add(new Paragraph(customer.getFirstName() + " " + customer.getLastName()))
                    .add(new Paragraph(customer.getEmail()).setFontSize(10))
                    .add(new Paragraph(customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "").setFontSize(10))
                    .setBorder(Border.NO_BORDER));

            billToTable.addCell(new Cell().add(new Paragraph("PREPARED BY").setFont(bold).setFontSize(12).setUnderline())
                    .add(new Paragraph(agent.getFirstName() + " " + agent.getLastName()))
                    .add(new Paragraph("Booking ID: " + invoice.getBooking().getBookingId()).setFontSize(10))
                    .setBorder(Border.NO_BORDER));

            document.add(billToTable);

            // --- LINE ITEMS TABLE ---
            Table itemTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1})).useAllAvailableWidth();
            itemTable.setMarginTop(30);

            // Table Header
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Equipment / Asset Description").setFont(bold).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(ColorConstants.DARK_GRAY));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Rental Period").setFont(bold).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(ColorConstants.DARK_GRAY).setTextAlignment(TextAlignment.CENTER));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Daily Rate").setFont(bold).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(ColorConstants.DARK_GRAY).setTextAlignment(TextAlignment.RIGHT));

            // Assets as Line Items
            if (invoice.getBooking().getAssetList() != null) {
                String duration = invoice.getBooking().getBookDate().format(DateTimeFormatter.ofPattern("MM/dd")) + 
                                  " - " + invoice.getBooking().getReturnDate().format(DateTimeFormatter.ofPattern("MM/dd"));
                
                for (Asset asset : invoice.getBooking().getAssetList()) {
                    itemTable.addCell(new Cell().add(new Paragraph(asset.getName()).setFont(bold))
                            .add(new Paragraph("S/N: " + asset.getSerialNumber()).setFontSize(8).setItalic()));
                    itemTable.addCell(new Cell().add(new Paragraph(duration)).setTextAlignment(TextAlignment.CENTER));
                    itemTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", asset.getPricePerDay()))).setTextAlignment(TextAlignment.RIGHT));
                }
            }

            document.add(itemTable);

            // --- SUMMARY SECTION ---
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1})).useAllAvailableWidth();
            
            // Subtotal
            summaryTable.addCell(new Cell(1, 2).add(new Paragraph("Subtotal")).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setPaddingTop(10));
            summaryTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", invoice.getPrice()))).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setPaddingTop(10));
            
            // Tax
            summaryTable.addCell(new Cell(1, 2).add(new Paragraph("Tax (15% GCT)")).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", invoice.getTax()))).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
            
            // Total
            summaryTable.addCell(new Cell(1, 2).add(new Paragraph("TOTAL AMOUNT")).setFont(bold).setFontSize(14).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", invoice.getTotal()))).setFont(bold).setFontSize(14).setFontColor(new DeviceRgb(37, 99, 235)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));

            document.add(summaryTable);

            // --- FOOTER ---
            Paragraph footer = new Paragraph("\n\nThank you for your business!\nIf you have any questions regarding this invoice, please contact support@yourcompany.com")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY);
            document.add(footer);

            document.close();
            System.out.println("Modern Invoice PDF generated successfully at " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}