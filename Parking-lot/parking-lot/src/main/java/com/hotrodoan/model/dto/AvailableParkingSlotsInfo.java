package com.hotrodoan.model.dto;

import com.hotrodoan.model.Block;
import com.hotrodoan.model.ParkingLot;
import com.hotrodoan.model.ParkingSlot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableParkingSlotsInfo {
    private ParkingLot parkingLot;
    private Block block;
    private List<ParkingSlot> availableParkingSlots;
}
