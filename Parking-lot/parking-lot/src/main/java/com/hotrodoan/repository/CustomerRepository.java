package com.hotrodoan.repository;

import com.hotrodoan.model.Customer;
import com.hotrodoan.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT c FROM Customer c WHERE CONCAT(c.vehicleNumber, ' ', c.contactNumber) LIKE %:keyword%" )
    Page<Customer> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    Customer findByUser(User user);
}
