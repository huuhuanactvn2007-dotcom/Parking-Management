package com.hotrodoan.service.impl;

import com.hotrodoan.model.BookingHistory;
import com.hotrodoan.model.Customer;
import com.hotrodoan.repository.BookingHistoryRepository;
import com.hotrodoan.service.BookingHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingHistoryServiceImpl implements BookingHistoryService {
    @Autowired
    private BookingHistoryRepository bookingHistoryRepository;
    @Override
    public BookingHistory addBookingHistory(BookingHistory bookingHistory) {
        return bookingHistoryRepository.save(bookingHistory);
    }

    @Override
    public BookingHistory getBookingHistory(Long id) {
        return bookingHistoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found booking history"));
    }

    @Override
    public Page<BookingHistory> getAllBookingHistories(Pageable pageable) {
        return bookingHistoryRepository.findAll(pageable);
    }

    @Override
    public Page<BookingHistory> getBookingHistoriesByCustomer(Customer customer, Pageable pageable) {
        return bookingHistoryRepository.findByCustomer(customer, pageable);
    }

    @Override
    public void addBookingHistories(List<BookingHistory> histories) {
        bookingHistoryRepository.saveAll(histories);
    }
}
