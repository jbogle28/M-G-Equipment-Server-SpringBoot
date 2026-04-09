package com.java.scheduler.repository;

import com.java.scheduler.domain.Booking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    
    long countByStatus(Booking.Status status);
    
    //Count overdue bookings
    long countByStatusAndReturnDateBefore(Booking.Status status, LocalDateTime dateTime);
    
    List<Booking> findAllByOrderByBookingIdDesc();
    
    // Finds all bookings created within a date range to analyze usage
    @Query("SELECT b FROM Booking b WHERE b.bookDate BETWEEN :startDate AND :endDate")
    List<Booking> findAllBookingsInPeriod(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
}