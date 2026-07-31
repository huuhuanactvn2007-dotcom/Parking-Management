package com.hotrodoan.service.impl;

import com.hotrodoan.model.Block;
import com.hotrodoan.model.ParkingLot;
import com.hotrodoan.repository.BlockRepository;
import com.hotrodoan.service.BlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockServiceImpl implements BlockService {
    @Autowired
    private BlockRepository blockRepository;
    @Override
    public Block createBlock(Block block) {
        return blockRepository.save(block);
    }

    @Override
    public Block updateBlock(Block block, Long id) {
        return blockRepository.findById(id).map(bl -> {
            bl.setParkingLot(block.getParkingLot());
            bl.setBlockCode(block.getBlockCode());
            bl.setNumberOfParkingSlots(block.getNumberOfParkingSlots());
            bl.setBlockFull(block.isBlockFull());
            return blockRepository.save(bl);
        }).orElseGet(() -> blockRepository.save(block));
    }

    @Override
    public void deleteBlock(Long id) {
        blockRepository.deleteById(id);
    }

    @Override
    public List<Block> getBlockByParkingLot(ParkingLot parkingLot) {
        return blockRepository.findByParkingLot(parkingLot);
    }

    @Override
    public List<Block> getAllBlock() {
        return blockRepository.findAll();
    }

    @Override
    public Block getBlock(Long id) {
        return blockRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found block"));
    }

    @Override
    public Block getBlockByParkingLotAndBlockCode(ParkingLot parkingLot, String blockCode) {
        return blockRepository.findByParkingLotAndBlockCode(parkingLot, blockCode);
    }

    @Override
    public List<Block> getBlockByParkingLotId(Long parkingLotId) {
        return blockRepository.findByParkingLotId(parkingLotId);
    }
}
