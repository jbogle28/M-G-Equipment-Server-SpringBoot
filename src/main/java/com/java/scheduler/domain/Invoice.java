package com.java.scheduler.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer invoiceId;

    private String clientName;
    private String userId;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    private String status;
    
    private Double price;
    private Double tax;
    private Double total;

    @Column(nullable = true) 
    private LocalDateTime paymentDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creationDate;

    private Boolean isQuote;

    @PrePersist
    public void onPrePersist() {
        this.creationDate = LocalDateTime.now();
        // Defaulting status
        if (this.status == null) {
            this.status = "UNPAID";
        }
        
        if (this.isQuote == null) {
            this.isQuote = false;
        }
    }
    
    public void calculateInvoiceTotal() {
        if (this.booking != null && this.booking.getAssetList() != null) {
            double subtotal = 0.0;
            
            for (Asset asset : this.booking.getAssetList()) {
                if (asset.getPricePerDay() != null) {
                    subtotal += asset.getPricePerDay();
                }
            }

            // Ensure dayCount is handled safely
            long days = (this.booking.getDayCount() != null && this.booking.getDayCount() > 0) 
                        ? this.booking.getDayCount() 
                        : 1;

            this.price = subtotal * days;
            this.tax = this.price * 0.15; // 15% GCT
            this.total = this.price + this.tax;
        } else {
            this.price = 0.0;
            this.tax = 0.0;
            this.total = 0.0;
        }
    }
}