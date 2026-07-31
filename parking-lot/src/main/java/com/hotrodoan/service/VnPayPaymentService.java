package com.hotrodoan.service;

import com.hotrodoan.model.Report;
import com.hotrodoan.model.VnPayPayment;

import java.sql.Date;
import java.util.List;

public interface VnPayPaymentService {
    VnPayPayment addVnPayPayment(VnPayPayment vnPayPayment);

    List<Report> getTotalRevenueByMonth(Date startTime, Date endTime);
}
