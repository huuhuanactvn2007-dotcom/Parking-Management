package com.hotrodoan.repository;

import com.hotrodoan.model.Block;
import com.hotrodoan.model.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    List<Block> findByParkingLot(ParkingLot parkingLot);
    Block findByParkingLotAndBlockCode(ParkingLot parkingLot, String blockCode);
    List<Block> findByParkingLotId(Long parkingLotId);
}
