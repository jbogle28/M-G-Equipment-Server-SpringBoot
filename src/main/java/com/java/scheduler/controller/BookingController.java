package com.java.scheduler.controller;

import com.java.scheduler.domain.Booking;
import com.java.scheduler.repository.BookingRepository;
import com.java.scheduler.service.BookingService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") //Vite/React dev server
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepo;

    // 1. CREATE BOOKING
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            bookingService.createBooking(booking);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Booking and Invoice created successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. READ BOOKING
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable int id) {
        Booking booking = bookingService.readBooking(id);
        return booking != null ? ResponseEntity.ok(booking) : ResponseEntity.notFound().build();
    }

    // 3. SHOW ALL
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.showAllBookings());
    }

    // 4. CLOSE BOOKING 
    @PutMapping("/{id}/close")
    public ResponseEntity<?> closeBooking(@PathVariable int id) {
        try {
            bookingService.closeBooking(id);
            return ResponseEntity.ok(Map.of("message", "Booking closed and assets released."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 5. DELETE BOOKING
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable int id) {
        try {
            bookingService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Booking deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 6. STATISTICS
    @GetMapping("/stats")
    public ResponseEntity<?> getBookingStats() {
        return ResponseEntity.ok(Map.of(
            "ongoing", bookingService.getOngoingCount(),
            "completed", bookingService.getCompletedCount()
        ));
    }
    
    @GetMapping("/reports/usage")
    public ResponseEntity<?> getUsageReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        List<Booking> bookings = bookingRepo.findAllBookingsInPeriod(start, end);
        
        Map<String, Object> report = new HashMap<>();
        report.put("generatedAt", LocalDateTime.now());
        report.put("summary", bookings.size() + " Total Bookings");
        report.put("details", bookings);
        
        return ResponseEntity.ok(report);
    }
}