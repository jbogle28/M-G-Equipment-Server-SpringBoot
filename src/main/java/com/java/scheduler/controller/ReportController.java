package com.java.scheduler.controller;

import com.java.scheduler.domain.Invoice;
import com.java.scheduler.service.ReportService;
import com.java.scheduler.util.AssetUtilizationPDFExporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:5173") 
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/income")
    public ResponseEntity<Map<String, Object>> getIncomeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.generateIncomeReport(start, end));
    }

    @GetMapping("/usage")
    public ResponseEntity<Map<String, Object>> getAssetUtilizationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.generateAssetUtilizationReport(start, end));
    }
    
    @GetMapping("/download/income")
    public ResponseEntity<byte[]> downloadIncomeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            // 1. Fetch data
            List<Invoice> invoices = reportService.fetchInvoicesForRange(start, end);
            
            // 2. Set up temp file path
            String filePath = "income_report_" + System.currentTimeMillis() + ".pdf";
            
            // 3. Generate the file using your Exporter
            com.java.scheduler.util.InvoiceReportPDFExporter.generateReport(invoices, start, end, filePath);
            
            // 4. Read bytes and clean up (Your logic)
            java.io.File file = new java.io.File(filePath);
            byte[] contents = java.nio.file.Files.readAllBytes(file.toPath());
            file.delete();

            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=MG_Equipment_Income_Report.pdf")
                .body(contents);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/download/usage")
    public ResponseEntity<byte[]> downloadUsageReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            // 1. Get the raw usage map from service
            Map<String, Integer> usageData = reportService.getUsageMapForRange(start, end);
            
            // 2. Temp file setup
            String filePath = "usage_report_" + System.currentTimeMillis() + ".pdf";
            
            // 3. Generate
            AssetUtilizationPDFExporter.generateUsageReport(usageData, start, end, filePath);
            
            // 4. Stream and Clean up (Your pattern)
            java.io.File file = new java.io.File(filePath);
            byte[] contents = java.nio.file.Files.readAllBytes(file.toPath());
            file.delete();

            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=MG_Asset_Utilization_Report.pdf")
                .body(contents);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}