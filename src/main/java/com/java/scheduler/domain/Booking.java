package com.java.scheduler.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookingId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User creator;
    
    @Column(nullable = false)
    private LocalDateTime bookDate;
    
    @Column(nullable = false)
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ONGOING;

    private Boolean late;
    private Long dayCount= 0L;
    private Integer quotationId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "booking_assets",
        joinColumns = @JoinColumn(name = "booking_id"),
        inverseJoinColumns = @JoinColumn(name = "asset_id")
    )
    private List<Asset> assetList;
    @PrePersist
    protected void onCreate() {
        if (this.bookDate == null) {
            this.bookDate = LocalDateTime.now();
        }
        
        if (this.dayCount == null) {
            this.dayCount = 0L;
        }
        
        if (this.late == null) {
            this.late = false;
        }

        if (this.status == null) {
            this.status = Status.ONGOING;
        }
    }
 
    public enum Status { ONGOING, CLOSED, CANCELLED }
}