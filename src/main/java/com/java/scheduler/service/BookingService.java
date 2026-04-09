package com.java.scheduler.service;

import com.java.scheduler.domain.*;
import com.java.scheduler.repository.*; // This covers all repositories in the package

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    
    // Added these two missing repositories
    private final BookingRepository bookingRepository;
    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository; // Fix: was missing
    private final UserRepository userRepository;         // Fix: was missing
    private final invoiceRepository invoiceRepository;

    public Booking readBooking(int bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }
    
    @Transactional
    public void createBooking(Booking booking) {
        // 1. Fetch managed Customer and User (Prevents "Detached Entity" issues)
        Customer managedCustomer = customerRepository.findById(booking.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + booking.getCustomer().getId()));
        
        User managedCreator = userRepository.findById(booking.getCreator().getId())
                .orElseThrow(() -> new RuntimeException("System User not found with ID: " + booking.getCreator().getId()));

        logger.info("Creating booking for customer: {} {}", managedCustomer.getFirstName(), managedCustomer.getLastName());

        // 2. Prepare the Booking object
        booking.setCustomer(managedCustomer);
        booking.setCreator(managedCreator);
        booking.setStatus(Booking.Status.ONGOING); // Ensure status is set
        
        // Temporarily detach assets list to prevent Cascade issues during initial save
        List<Asset> incomingAssets = booking.getAssetList();
        booking.setAssetList(new ArrayList<>()); 
        
        // Save booking first to get a generated ID
        Booking savedBooking = bookingRepository.save(booking);

        // 3. Process Assets (Availability Check & Status Update)
        List<Asset> validAssets = new ArrayList<>();
        if (incomingAssets != null) {
            for (Asset assetRef : incomingAssets) {
                Asset dbAsset = assetRepository.findById(assetRef.getAssetId()).orElse(null);
                
                if (dbAsset != null && dbAsset.getStatus() == Asset.Status.AVAILABLE) {
                    dbAsset.setStatus(Asset.Status.BOOKED); 
                    validAssets.add(dbAsset);
                }
            }
        }

        if (validAssets.isEmpty()) {
            throw new RuntimeException("No assets available for booking. Transaction rolled back.");
        }

        // 4. Save updated assets and sync the booking object
        assetRepository.saveAll(validAssets);
        savedBooking.setAssetList(validAssets); // Important for calculation logic
        bookingRepository.save(savedBooking);
        
        // 5. Generate Invoice with proper Data Mapping
        Invoice invoice = new Invoice();
        invoice.setBooking(savedBooking);
        invoice.setStatus("UNPAID");
        invoice.setCreationDate(LocalDateTime.now());
        
        // FIX: Set the client name explicitly so it's not null in the DB
        String fullName = managedCustomer.getFirstName() + " " + managedCustomer.getLastName();
        invoice.setClientName(fullName);
        
        // FIX: Calculate total AFTER assets are attached to savedBooking
        invoice.calculateInvoiceTotal(); 
        
        invoiceRepository.save(invoice);
        
        logger.info("Booking ID: {} and Invoice for {} created successfully with total: {}", 
                    savedBooking.getBookingId(), fullName, invoice.getTotal());
    }
    
    @Transactional
    public void closeBooking(int bookingId) {
        logger.info("Closing booking ID: {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        for (Asset asset : booking.getAssetList()) {
            asset.setStatus(Asset.Status.AVAILABLE);
        }
        assetRepository.saveAll(booking.getAssetList());

        booking.setStatus(Booking.Status.CLOSED);
        bookingRepository.save(booking);
        
        logger.info("Booking {} closed and assets released.", bookingId);
    }

    @Transactional
    public void delete(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == Booking.Status.ONGOING) {
            throw new RuntimeException("Cannot delete an ongoing booking.");
        }

        bookingRepository.delete(booking);
    }
    
    public List<Booking> showAllBookings() {
        return bookingRepository.findAllByOrderByBookingIdDesc();
    }

    public int getOngoingCount() {
        return (int) bookingRepository.countByStatus(Booking.Status.ONGOING);
    }

    public int getCompletedCount() {
        return (int) bookingRepository.countByStatus(Booking.Status.CLOSED);
    }
}