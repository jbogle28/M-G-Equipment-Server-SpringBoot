package com.java.scheduler.service;

import com.java.scheduler.domain.Asset;
import com.java.scheduler.domain.Booking;
import com.java.scheduler.repository.DashboardStatsDTO;
import com.java.scheduler.repository.BookingRepository;
import com.java.scheduler.repository.AssetRepository;
import com.java.scheduler.repository.CustomerRepository;
import com.java.scheduler.repository.invoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BookingRepository bookingRepository;
    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository;
    private final invoiceRepository invoiceRepository;

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // 1. Active Rentals (ONGOING status)
        stats.setActiveRentals(bookingRepository.countByStatus(Booking.Status.ONGOING));

        // 2. Available Assets
        stats.setAvailableAssets(assetRepository.countByStatus(Asset.Status.AVAILABLE));

        // 3. Overdue Returns (ONGOING and date is in the past)
        stats.setOverdueReturns(bookingRepository.countByStatusAndReturnDateBefore(
                Booking.Status.ONGOING, LocalDateTime.now()));

        // 4. Pending Invoices (Anything not marked PAID)
        stats.setPendingInvoices(invoiceRepository.countByStatusIgnoreCase("UNPAID"));

        // 5. Total Customers
        stats.setTotalCustomers(customerRepository.count());

        return stats;
    }
}