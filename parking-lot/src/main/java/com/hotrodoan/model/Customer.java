package com.hotrodoan.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(min = 2, max = 20)
    @Column(unique = true)
    private String vehicleNumber; //bien so xe
    //ngày đăng ký ban đầu
    private Date registrationDate;
    @Column(nullable = true)
    private boolean regularCustomer = false;
    @Column(unique = true)
    @Pattern(regexp = "(\\d{4}[-.]?\\d{3}[-.]?\\d{3})", message = "Số điện thoại phải bao gồm 10 chữ số và có thể có dấu chấm hoặc dấu gạch ngang giữa các phần tử")
    private String contactNumber;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
