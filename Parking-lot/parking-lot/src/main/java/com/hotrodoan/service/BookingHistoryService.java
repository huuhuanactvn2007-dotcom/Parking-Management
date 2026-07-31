package com.hotrodoan.service;

import com.hotrodoan.model.BookingHistory;
import com.hotrodoan.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingHistoryService {
    BookingHistory addBookingHistory(BookingHistory bookingHistory);
    BookingHistory getBookingHistory(Long id);
    Page<BookingHistory> getAllBookingHistories(Pageable pageable);
    Page<BookingHistory> getBookingHistoriesByCustomer(Customer customer, Pageable pageable);
    void addBookingHistories(List<BookingHistory> histories);
}
