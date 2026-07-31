package com.hotrodoan.repository;

import com.hotrodoan.model.BookingHistory;
import com.hotrodoan.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long>{
    Page<BookingHistory> findAll(Pageable pageable);
    Page<BookingHistory> findByCustomer(Customer customer, Pageable pageable);
}
