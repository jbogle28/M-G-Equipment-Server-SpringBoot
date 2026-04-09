package com.java.scheduler.repository;

import com.java.scheduler.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface invoiceRepository extends JpaRepository<Invoice, Integer> {
    
    List<Invoice> findByCreationDateBetween(LocalDateTime start, LocalDateTime end);
    
    long countByStatusIgnoreCase(String status);
    
    @Query("SELECT i FROM Invoice i WHERE i.status = 'PAID' AND i.creationDate BETWEEN :startDate AND :endDate")
    List<Invoice> findPaidInvoicesInPeriod(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(i.total) FROM Invoice i WHERE i.status = 'PAID' AND i.creationDate BETWEEN :startDate AND :endDate")
    Double getTotalIncomeInPeriod(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
}