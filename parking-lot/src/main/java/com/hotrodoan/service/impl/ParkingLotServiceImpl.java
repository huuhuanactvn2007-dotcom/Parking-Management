package com.hotrodoan.service.impl;

import com.hotrodoan.model.Block;
import com.hotrodoan.model.ParkingLot;
import com.hotrodoan.model.dto.BlockAndParkingSlot;
import com.hotrodoan.model.dto.ParkingLotAndBlockForm;
import com.hotrodoan.repository.ParkingLotRepository;
import com.hotrodoan.service.BlockService;
import com.hotrodoan.service.ParkingLotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParkingLotServiceImpl implements ParkingLotService {
    @Autowired
    private ParkingLotRepository parkingLotRepository;
    @Autowired
    private BlockService blockService;

    @Override
    public ParkingLot createParkingLot(ParkingLot parkingLot) {
        return parkingLotRepository.save(parkingLot);
    }

    @Override
    public Page<ParkingLot> getAllParkingLots(String name, String address, Pageable pageable) {
        Page<ParkingLot> parkingLots = parkingLotRepository.findByKeyword(name, address, pageable);

        for (ParkingLot parkingLot : parkingLots) {
            int usedSlots = parkingLotRepository.countUsedParkingSlots(parkingLot.getId());
            parkingLot.setUsedSlots(usedSlots);
        }

        return parkingLots;
    }

    @Override
    public ParkingLot updateParkingLot(ParkingLot parkingLot, Long id) {
        return parkingLotRepository.findById(id).map(pl -> {
            pl.setName(parkingLot.getName());
            pl.setNumberOfBlocks(parkingLot.getNumberOfBlocks());
            pl.setSlotAvailable(parkingLot.isSlotAvailable());
            pl.setAddress(parkingLot.getAddress());
            pl.setZip(parkingLot.getZip());
            pl.setReentryAllowed(parkingLot.isReentryAllowed());
            pl.setOperatingCompanyName(parkingLot.getOperatingCompanyName());
            pl.setValetParkingAvailable(parkingLot.isValetParkingAvailable());
            return parkingLotRepository.save(pl);
        }).orElse(null);
    }

    @Override
    public void deleteParkingLot(Long id) {
        parkingLotRepository.deleteById(id);
    }

    @Override
    public ParkingLot getParkingLot(Long id) {
        return parkingLotRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found parking lot"));
    }

   @Override
   public List<Object[]> countUsedParkingSlots() {
       return null;
   }

    @Override
    public List<ParkingLot> getParkingLotsByAddressAndReentryAllowedAndIdNot(String address, boolean reentryAllowed, Long id) {
        return parkingLotRepository.findByAddressAndReentryAllowedAndIdNot(address, reentryAllowed, id);
    }

    @Override
    public ParkingLot showParkingLot(Long id) {
        return parkingLotRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found parking lot"));
    }

    @Override
    public int countUsedParkingSlots(Long id) {
        return parkingLotRepository.countUsedParkingSlots(id);
    }

    @Override
    public ParkingLotAndBlockForm getParkingLotAndBlockForm(Long id) {
        ParkingLot parkingLot = parkingLotRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found parking lot"));

        ParkingLotAndBlockForm parkingLotAndBlockForm = new ParkingLotAndBlockForm();
        parkingLotAndBlockForm.setName(parkingLot.getName());
        parkingLotAndBlockForm.setAddress(parkingLot.getAddress());
        parkingLotAndBlockForm.setReentryAllowed(parkingLot.isReentryAllowed());
        parkingLotAndBlockForm.setOperatingCompanyName(parkingLot.getOperatingCompanyName());
        parkingLotAndBlockForm.setValetParkingAvailable(parkingLot.isValetParkingAvailable());

        List<Block> blocks = blockService.getBlockByParkingLot(parkingLot);

        List<BlockAndParkingSlot> blockAndParkingSlots = parkingLotAndBlockForm.getBlockAndParkingSlots();
        if (blockAndParkingSlots == null) {
            blockAndParkingSlots = new ArrayList<>();
            parkingLotAndBlockForm.setBlockAndParkingSlots(blockAndParkingSlots);
        }

        for (Block block : blocks) {
            BlockAndParkingSlot blAndPS = new BlockAndParkingSlot();
            blAndPS.setBlock(block);
            blAndPS.setNumberOfParkingSlots(block.getNumberOfParkingSlots());
            blockAndParkingSlots.add(blAndPS);
        }

        return parkingLotAndBlockForm;
    }

    @Override
    public Page<ParkingLot> getAllParkingLots(Pageable pageable) {
        return parkingLotRepository.findAll(pageable);
    }
}
