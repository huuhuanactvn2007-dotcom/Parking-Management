package com.hotrodoan.repository;

import com.hotrodoan.model.Customer;
import com.hotrodoan.model.RegularPass;
import com.hotrodoan.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RegularPassRepository extends JpaRepository<RegularPass, Long> {
    Page<RegularPass> findAll(Pageable pageable);
    RegularPass findByCustomer(Customer customer);
    @Query("SELECT r FROM RegularPass r WHERE r.customer.user.name LIKE %:name%")
    Page<RegularPass> searchByCustomerName(String name, Pageable pageable);
}
