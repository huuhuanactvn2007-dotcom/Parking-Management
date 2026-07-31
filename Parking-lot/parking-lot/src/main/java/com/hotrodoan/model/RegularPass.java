package com.hotrodoan.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegularPass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Customer customer;

    private Date purchaseDate;
    private Date startDate;
    private Date endDate;
    private int durationInDays;
    private int cost;
    private boolean pair = false;
    private boolean renewPair = false;
    private boolean statusNow = false;
}
