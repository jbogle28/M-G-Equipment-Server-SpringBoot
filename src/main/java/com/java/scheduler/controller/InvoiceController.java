package com.java.scheduler.controller;

import com.java.scheduler.domain.Invoice;
import com.java.scheduler.repository.invoiceRepository;
import com.java.scheduler.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final invoiceRepository invRepo;
    // 1. SHOW ALL
    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.showAllInvoices());
    }

    
    // 2. READ SINGLE INVOICE
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable int id) {
        Invoice invoice = invoiceService.readInvoice(id);
        return invoice != null ? ResponseEntity.ok(invoice) : ResponseEntity.notFound().build();
    }

    // 3. RECEIVE PAYMENT
    @PutMapping("/{id}/pay")
    public ResponseEntity<?> payInvoice(@PathVariable int id) {
        try {
            invoiceService.receivePayment(id);
            return ResponseEntity.ok(Map.of("message", "Payment processed successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 4. DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable int id) {
        try {
            invoiceService.deleteInvoice(id);
            return ResponseEntity.ok(Map.of("message", "Invoice deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 5. EXPORT/REPORT
    @GetMapping("/report")
    public ResponseEntity<List<Invoice>> getReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(invoiceService.getInvoicesForReport(start, end));
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadInvoicePDF(@PathVariable int id) {
        try {
            Invoice invoice = invoiceService.readInvoice(id);
            if (invoice == null) return ResponseEntity.notFound().build();

            // Use a temporary path or stream
            String filePath = "invoice_" + id + ".pdf";
            com.java.scheduler.util.InvoicePDFExporter.generateInvoicePDF(invoice, filePath);
            
            java.io.File file = new java.io.File(filePath);
            byte[] contents = java.nio.file.Files.readAllBytes(file.toPath());
            
            // Clean up temp file
            file.delete();

            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=Invoice_" + id + ".pdf")
                .body(contents);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/reports/income")
    public ResponseEntity<?> getIncomeReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Map<String, Object> report = new HashMap<>();
        
        try {
            List<Invoice> invoices = invRepo.findPaidInvoicesInPeriod(start, end);
            Double total = invRepo.getTotalIncomeInPeriod(start, end);

            report.put("generatedAt", LocalDateTime.now());
            report.put("summary", total != null ? total : 0.0);
            report.put("details", invoices);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to generate report"));
        }
    }   
}