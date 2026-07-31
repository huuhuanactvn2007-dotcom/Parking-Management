package com.hotrodoan.repository;

import com.hotrodoan.model.VnPayPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface VnPayPaymentRepository extends JpaRepository<VnPayPayment, Long> {
//    @Query("SELECT MONTH(v.vnpPayDate) AS month, YEAR(v.vnpPayDate) AS year, SUM(v.vnpAmount) AS totalAmount " +
//            "FROM VnPayPayment v " +
//            "GROUP BY MONTH(v.vnpPayDate), YEAR(v.vnpPayDate)")
//    List<Object[]> getTotalRevenueByMonth();

    @Query("SELECT MONTH(vnpPayDate) AS month, YEAR(vnpPayDate) AS year, SUM(vnpAmount) AS totalAmount " +
            "FROM VnPayPayment " +
            "WHERE vnpPayDate BETWEEN :startTime AND :endTime " +
            "GROUP BY YEAR(vnpPayDate), MONTH(vnpPayDate)")
    List<Object[]> getTotalRevenueByMonth(Date startTime, Date endTime);
}
