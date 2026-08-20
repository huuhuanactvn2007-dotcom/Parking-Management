package com.hotrodoan.repository;

import com.hotrodoan.model.Customer;
import com.hotrodoan.model.ParkingSlot;
import com.hotrodoan.model.dto.ParkingSlotReservationSub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingSlotReservationSubRepository extends JpaRepository<ParkingSlotReservationSub, Long> {
    List<ParkingSlotReservationSub> findByCustomer(Customer customer);
    List<ParkingSlotReservationSub> findByParkingSlot(ParkingSlot parkingSlot);
}
