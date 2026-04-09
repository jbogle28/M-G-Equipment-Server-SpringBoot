package com.java.scheduler.service;

import com.java.scheduler.domain.Invoice;
import com.java.scheduler.repository.invoiceRepository;
import com.java.scheduler.util.InvoicePDFExporter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
    private final invoiceRepository invoiceRepository;

    public Invoice readInvoice(int invoiceId) {
        return invoiceRepository.findById(invoiceId).orElse(null);
    }

    @Transactional
    public void receivePayment(int invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
            logger.warn("Invoice ID {} is already paid.", invoiceId);
            return;
        }

  
        invoice.setPaymentDate(LocalDateTime.now());
        invoice.setStatus("PAID");
        invoiceRepository.save(invoice);
        
        logger.info("Payment received for invoice ID: {}", invoiceId);
    }

    @Transactional
    public void deleteInvoice(int invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Only allow deletion if status is PAID
        if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
            invoiceRepository.delete(invoice);
            logger.info("Invoice ID {} deleted successfully.", invoiceId);
        } else {
            logger.warn("Cannot delete unpaid invoice ID: {}", invoiceId);
        }
    }

    public List<Invoice> showAllInvoices() {
        return invoiceRepository.findAll();
    }

    public void exportInvoice(int invoiceId, String filePath) {
        Invoice invoice = readInvoice(invoiceId);
        if (invoice != null) {
            InvoicePDFExporter.generateInvoicePDF(invoice, filePath);
        }
    }

    public List<Invoice> getInvoicesForReport(LocalDateTime start, LocalDateTime end) {
        return invoiceRepository.findByCreationDateBetween(start, end);
    }
}