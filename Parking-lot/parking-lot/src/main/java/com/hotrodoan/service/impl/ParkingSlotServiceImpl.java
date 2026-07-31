package com.hotrodoan.service.impl;

import com.hotrodoan.model.Block;
import com.hotrodoan.model.ParkingSlot;
import com.hotrodoan.repository.ParkingSlotRepository;
import com.hotrodoan.service.ParkingSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingSlotServiceImpl implements ParkingSlotService {
    @Autowired
    private ParkingSlotRepository parkingSlotRepository;
    @Override
    public Page<ParkingSlot> getAllParkingSlot(Pageable pageable) {
        return null;
    }

    @Override
    public ParkingSlot addParkingSlot(ParkingSlot parkingSlot) {
        return parkingSlotRepository.save(parkingSlot);
    }

    @Override
    public ParkingSlot updateParkingSlot(ParkingSlot parkingSlot, Long id) {
        return parkingSlotRepository.findById(id).map(ps -> {
            ps.setBlock(parkingSlot.getBlock());
            ps.setSlotNumber(parkingSlot.getSlotNumber());
            ps.setWingCode(parkingSlot.getWingCode());
            ps.setSlotAvailable(parkingSlot.isSlotAvailable());
            return parkingSlotRepository.save(ps);
        }).orElseThrow(() -> new RuntimeException("Not found parking slot"));
    }

    @Override
    public void deleteParkingSlot(Long id) {

    }

    @Override
    public List<ParkingSlot> getParkingSlotByBlock(Block block) {
        return parkingSlotRepository.findByBlock(block);
    }

    @Override
    public List<ParkingSlot> getParkingSlotByBlockId(Long blockId) {
        return parkingSlotRepository.findByBlockId(blockId);
    }

    @Override
    public ParkingSlot getParkingSlot(Long id) {
        return parkingSlotRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found parking slot"));
    }

    @Override
    public int countUsedParkingSlots(Long id) {
        return parkingSlotRepository.countUsedParkingSlots(id);
    }

    @Override
    public List<ParkingSlot> getAllParkingSlots() {
        return parkingSlotRepository.findAll();
    }

    @Override
    public void updateParkingSlots(List<ParkingSlot> parkingSlots) {
        parkingSlotRepository.saveAll(parkingSlots);
    }

    @Override
    public int findMaxSlotNumber(Long blockId) {
        return parkingSlotRepository.findMaxSlotNumber(blockId);
    }

    @Override
    public int countParkingSlotsByBlock(Block block) {
        return parkingSlotRepository.countByBlock(block);
    }

    @Override
    public ParkingSlot getParkingSlotBySlotNumberAndBlockId(int slotNumber, Long blockId) {
        return parkingSlotRepository.findBySlotNumberAndBlockId(slotNumber, blockId);
    }

    @Override
    public ParkingSlot getParkingSlotBySlotNumberAndBlock(int slotNumber, Block block) {
        return parkingSlotRepository.findBySlotNumberAndBlock(slotNumber, block);
    }
}
