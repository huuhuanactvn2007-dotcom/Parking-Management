package com.hotrodoan.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSlotReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Customer customer;

    private Timestamp startTimestamp;
    private int durationInMinutes;
    private Timestamp bookingDate;

    @ManyToOne
    private ParkingSlot parkingSlot;

    private int cost;
    @Column(nullable = false)
    private String confirmName;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private String confirmVehicleNumber;
    private boolean pair = false;
}
