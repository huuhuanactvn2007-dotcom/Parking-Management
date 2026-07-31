package com.hotrodoan.service;

import com.hotrodoan.model.Block;
import com.hotrodoan.model.ParkingLot;

import java.util.List;

public interface BlockService {
    Block createBlock(Block block);
    Block updateBlock(Block block, Long id);
    void deleteBlock(Long id);
    List<Block> getBlockByParkingLot(ParkingLot parkingLot);
    List<Block> getAllBlock();
    Block getBlock(Long id);
    Block getBlockByParkingLotAndBlockCode(ParkingLot parkingLot, String blockCode);
    List<Block> getBlockByParkingLotId(Long parkingLotId);
}
