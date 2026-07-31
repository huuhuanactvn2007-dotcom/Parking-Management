package com.hotrodoan.model.dto;

import com.hotrodoan.model.Block;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockAndParkingSlot {
    private Block block;
    private int numberOfParkingSlots;
}
