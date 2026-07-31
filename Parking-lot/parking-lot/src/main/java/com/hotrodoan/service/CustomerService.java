package com.hotrodoan.service;

import com.hotrodoan.model.Customer;
import com.hotrodoan.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    Customer addCustomer(Customer customer);
    Page<Customer> getAllCustomerByKeyword(String keyword, Pageable pageable);
    Customer getCustomer(Long id);
    void deleteCustomer(Long id);
    Customer updateCustomer(Customer customer, Long id);
    Customer getCustomerByUser(User user);
}
