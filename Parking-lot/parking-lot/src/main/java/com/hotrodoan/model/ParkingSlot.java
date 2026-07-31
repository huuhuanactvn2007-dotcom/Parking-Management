package com.hotrodoan.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Block block;
    @Min(1)
    private int slotNumber;
    @Column(length = 1)
    private String wingCode="v";
    boolean slotAvailable = true;

    @OneToMany(mappedBy = "parkingSlot", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JsonIgnore
    List<ParkingSlotReservation> parkingSlotReservations;
}
