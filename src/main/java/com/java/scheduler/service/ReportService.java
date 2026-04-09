package com.java.scheduler.service;

import com.java.scheduler.domain.Asset;
import com.java.scheduler.domain.Booking;
import com.java.scheduler.domain.Invoice;
import com.java.scheduler.repository.BookingRepository;
import com.java.scheduler.repository.invoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private invoiceRepository InvoiceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Map<String, Object> generateIncomeReport(LocalDateTime start, LocalDateTime end) {
        List<Invoice> invoices = InvoiceRepository.findByCreationDateBetween(start, end);
        
        double totalRevenue = invoices.stream()
                .filter(inv -> "PAID".equalsIgnoreCase(inv.getStatus()))
                .mapToDouble(Invoice::getTotal)
                .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("generatedAt", LocalDateTime.now());
        response.put("summary", totalRevenue);
        response.put("details", invoices);
        return response;
    }
    
    public List<Invoice> fetchInvoicesForRange(LocalDateTime start, LocalDateTime end) {
        return InvoiceRepository.findPaidInvoicesInPeriod(start, end);
    }
    
    public Map<String, Integer> getUsageMapForRange(LocalDateTime start, LocalDateTime end) {
        List<Booking> bookings = bookingRepository.findAllBookingsInPeriod(start, end);
        Map<String, Integer> usageMap = new HashMap<>();
        
        for (Booking b : bookings) {
            for (Asset a : b.getAssetList()) {
                usageMap.put(a.getName(), usageMap.getOrDefault(a.getName(), 0) + 1);
            }
        }
        
        // Return sorted map by value descending
        return usageMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    Map.Entry::getValue, 
                    (e1, e2) -> e1, LinkedHashMap::new));
    }
    public Map<String, Object> generateAssetUtilizationReport(LocalDateTime start, LocalDateTime end) {
        List<Booking> bookings = bookingRepository.findAllBookingsInPeriod(start, end);
        
        // Map to count occurrences: Asset Name -> Usage Count
        Map<String, Integer> usageMap = new HashMap<>();
        
        for (Booking b : bookings) {
            for (Asset a : b.getAssetList()) {
                usageMap.put(a.getName(), usageMap.getOrDefault(a.getName(), 0) + 1);
            }
        }

        // Sort by value (usage count) descending
        List<Map<String, Object>> details = usageMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("label", entry.getKey());
                    item.put("value", entry.getValue() + " Bookings");
                    // Matching frontend 'details' structure
                    item.put("date", "N/A"); 
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("generatedAt", LocalDateTime.now());
        response.put("summary", bookings.size() + " Total Bookings");
        response.put("details", details);
        return response;
    }
}