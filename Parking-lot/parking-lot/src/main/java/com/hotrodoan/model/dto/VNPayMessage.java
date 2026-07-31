package com.hotrodoan.model.dto;

import com.hotrodoan.model.RegularPass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPayMessage {
    private String message;
    private String vnpayUrl;
}
