package com.java.scheduler.util;

import com.java.scheduler.domain.Asset;
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
import java.util.Map;

public class AssetUtilizationPDFExporter {

    public static void generateUsageReport(Map<String, Integer> usageData, LocalDateTime start, LocalDateTime end, String filePath) {
        try {
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.LETTER);

            PdfFont bold = PdfFontFactory.createFont("Helvetica-Bold");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            // --- HEADER ---
            document.add(new Paragraph("M&G EQUIPMENT")
                    .setFont(bold).setFontSize(22)
                    .setFontColor(new DeviceRgb(37, 99, 235))
                    .setTextAlignment(TextAlignment.LEFT));
            
            document.add(new Paragraph("Asset Utilization & Usage Analysis")
                    .setFontSize(12).setItalic().setMarginBottom(10));

            document.add(new Paragraph("Analysis Period: " + start.format(dtf) + " to " + end.format(dtf))
                    .setFontSize(10));
            document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(dtf))
                    .setFontSize(10).setMarginBottom(20));

            // --- UTILIZATION TABLE ---
            float[] columnWidths = {4, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
            
            // Styled Headers
            table.addHeaderCell(new Cell().add(new Paragraph("Asset Name").setFont(bold).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(ColorConstants.DARK_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Total Bookings").setFont(bold).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(ColorConstants.DARK_GRAY).setTextAlignment(TextAlignment.CENTER));

            // Populate from the usage map (Sorted data from service)
            for (Map.Entry<String, Integer> entry : usageData.entrySet()) {
                table.addCell(new Cell().add(new Paragraph(entry.getKey())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(entry.getValue())))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            document.add(table);

            // --- FOOTER ---
            document.add(new Paragraph("\n* This report ranks assets based on frequency of booking within the selected dates.")
                    .setFontSize(9).setItalic().setFontColor(ColorConstants.GRAY));

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}