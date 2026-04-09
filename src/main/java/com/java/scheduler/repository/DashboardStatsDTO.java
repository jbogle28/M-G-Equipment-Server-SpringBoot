package com.java.scheduler.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private long activeRentals;      // Count of ONGOING bookings
    private long availableAssets;    // Count of AVAILABLE assets
    private long overdueReturns;     // Count of ONGOING bookings past return date
    private long pendingInvoices;    // Count of UNPAID invoices
    private long totalCustomers;     // Total client base size
}