package com.hotrodoan.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotDetails {
    private ParkingLot parkingLot;
    private List<ParkingLot> suggestions;
}
