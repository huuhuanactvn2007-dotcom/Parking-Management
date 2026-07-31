package com.hotrodoan.repository;

import com.hotrodoan.model.dto.ParkingSlotReservationSub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingSlotReservationSubRepository extends JpaRepository<ParkingSlotReservationSub, Long> {
}
