package com.hotrodoan.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileForm {
    private String name;
    private String username;
    private String email;
    private String avatar;
    private String contactNumber;
    private String vehicleNumber;
    private boolean regularCustomer;
}
