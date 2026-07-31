package com.hotrodoan.repository;

import com.hotrodoan.model.ParkingLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    @Query("SELECT p FROM ParkingLot p WHERE CONCAT(p.name, ' ', p.operatingCompanyName) LIKE %:name% AND p.address LIKE :address")
    Page<ParkingLot> findByKeyword(String name, String address, Pageable pageable);

    Page<ParkingLot> findAll(Pageable pageable);

    List<ParkingLot> findByAddressAndReentryAllowedAndIdNot(String address, boolean reentryAllowed, Long id);
    @Query("SELECT COUNT(ps) FROM ParkingSlot ps " +
            "JOIN ps.block b " +
            "JOIN b.parkingLot pl " +
            "WHERE ps.slotAvailable = false AND pl.id = :id")
    int countUsedParkingSlots(Long id);

    // @Query("SELECT pl, COUNT(ps) FROM ParkingSlot ps " +
    //     "JOIN ps.block b " +
    //     "JOIN b.parkingLot pl " +
    //     "WHERE ps.slotAvailable = false " +
    //     "GROUP BY pl")
    // List<Object[]> countUsedParkingSlotsPerParkingLot();
}
