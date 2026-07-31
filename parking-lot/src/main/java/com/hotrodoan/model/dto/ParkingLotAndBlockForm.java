package com.hotrodoan.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotAndBlockForm {
    private String name;
    private String address;
    @Size(max = 3)
    private String zip="v";
    private boolean reentryAllowed;
    private String operatingCompanyName;
    private boolean valetParkingAvailable;

    @NotBlank(message = "Block code is required")
    @Size(min = 1, max = 3)
    private List<BlockAndParkingSlot> blockAndParkingSlots;
}
