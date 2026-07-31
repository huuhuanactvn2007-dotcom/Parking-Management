package com.hotrodoan.model.dto;

import com.hotrodoan.model.Customer;
import com.hotrodoan.model.RegularPass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAndRegularPass {
    Customer customer;
    RegularPass regularPass;
}
