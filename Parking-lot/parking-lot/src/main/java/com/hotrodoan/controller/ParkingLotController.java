package com.hotrodoan.controller;

import com.hotrodoan.model.Block;
import com.hotrodoan.model.ParkingLot;
import com.hotrodoan.model.ParkingLotDetails;
import com.hotrodoan.model.ParkingSlot;
import com.hotrodoan.model.dto.BlockAndParkingSlot;
import com.hotrodoan.model.dto.ParkingLotAndBlockForm;
import com.hotrodoan.model.dto.ResponseMessage;
import com.hotrodoan.service.BlockService;
import com.hotrodoan.service.ParkingLotService;
import com.hotrodoan.service.ParkingSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/parking-lots")
@CrossOrigin(origins = "*")
public class ParkingLotController {
    @Autowired
    private ParkingLotService parkingLotService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private ParkingSlotService parkingSlotService;

    @GetMapping("")
    public ResponseEntity<Page<ParkingLot>> getAllParkingLots(@RequestParam(defaultValue = "") String name,
                                                              @RequestParam(defaultValue = "") String address,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(defaultValue = "id") String sortBy,
                                                              @RequestParam(defaultValue = "desc") String order) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc(sortBy)));
        if (name.equals("") && address.equals(""))
            return new ResponseEntity<>(parkingLotService.getAllParkingLots(pageable), HttpStatus.OK);
        return new ResponseEntity<>(parkingLotService.getAllParkingLots(name, address, pageable), HttpStatus.OK);
    }

    @PostMapping("/admin/add")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParkingLotAndBlockForm> createParkingLot(@RequestBody ParkingLotAndBlockForm parkingLotAndBlockForm) {
        ParkingLot parkingLot = new ParkingLot();
        Block block = new Block();
        parkingLot.setSlotAvailable(true);
        parkingLot.setNumberOfBlocks(1);
        parkingLot.setName(parkingLotAndBlockForm.getName());
        parkingLot.setAddress(parkingLotAndBlockForm.getAddress());
        parkingLot.setReentryAllowed(parkingLotAndBlockForm.isReentryAllowed());
        parkingLot.setOperatingCompanyName(parkingLotAndBlockForm.getOperatingCompanyName());
        parkingLot.setValetParkingAvailable(parkingLotAndBlockForm.isValetParkingAvailable());

        ParkingLot newParkingLot = parkingLotService.createParkingLot(parkingLot);
        int dBlock = parkingLotAndBlockForm.getBlockAndParkingSlots().size();

        for (BlockAndParkingSlot blockAndParkingSlot : parkingLotAndBlockForm.getBlockAndParkingSlots()) {
            Block bl = new Block();
            bl.setBlockCode(blockAndParkingSlot.getBlock().getBlockCode());
            bl.setParkingLot(newParkingLot);
//            dBlock += 1;
            int dParkingSlot = 0;
            Block newBlock = blockService.createBlock(bl);

            int numberOfSlots = blockAndParkingSlot.getNumberOfParkingSlots();
            for (int i = 0; i < numberOfSlots; i++) {
                dParkingSlot += 1;
                ParkingSlot parkingSlot = new ParkingSlot();
                parkingSlot.setBlock(newBlock);
                parkingSlot.setSlotNumber(i + 1);
                parkingSlotService.addParkingSlot(parkingSlot);
            }
            newBlock.setNumberOfParkingSlots(dParkingSlot);
            blockService.updateBlock(newBlock, newBlock.getId());
        }

        newParkingLot.setNumberOfBlocks(dBlock);
        parkingLotService.updateParkingLot(newParkingLot, newParkingLot.getId());

        return new ResponseEntity<>(parkingLotAndBlockForm, HttpStatus.OK);
    }

    @PutMapping("/admin/update/{id}")
    public ResponseEntity<ParkingLotAndBlockForm> updateParkingLot(@RequestBody ParkingLotAndBlockForm parkingLotAndBlockForm, @PathVariable("id") Long id) {
        ParkingLotAndBlockForm pAndBForm = parkingLotService.getParkingLotAndBlockForm(id);
        ParkingLot updateParkingLot = parkingLotService.showParkingLot(id);

        updateParkingLot.setName(parkingLotAndBlockForm.getName());
        updateParkingLot.setAddress(parkingLotAndBlockForm.getAddress());
        updateParkingLot.setReentryAllowed(parkingLotAndBlockForm.isReentryAllowed());
        updateParkingLot.setOperatingCompanyName(parkingLotAndBlockForm.getOperatingCompanyName());
        updateParkingLot.setValetParkingAvailable(parkingLotAndBlockForm.isValetParkingAvailable());

        int dBlock = parkingLotAndBlockForm.getBlockAndParkingSlots().size();

        updateParkingLot.setNumberOfBlocks(dBlock);

        ParkingLot updateP = parkingLotService.updateParkingLot(updateParkingLot, id);

        List<Block> blocks = blockService.getBlockByParkingLotId(updateP.getId());

        List<BlockAndParkingSlot> blockAndParkingSlots = pAndBForm.getBlockAndParkingSlots();
        List<BlockAndParkingSlot> newBlockAndParkingSlots = parkingLotAndBlockForm.getBlockAndParkingSlots();

        //bug
        if (blockAndParkingSlots.size() > newBlockAndParkingSlots.size()) {
            for (int i = newBlockAndParkingSlots.size(); i < blockAndParkingSlots.size(); i++) {
                blockService.deleteBlock(blockAndParkingSlots.get(i).getBlock().getId());
            }

            for (int i = 0; i < newBlockAndParkingSlots.size(); i++) {
                Block bl = blockAndParkingSlots.get(i).getBlock();
                bl.setBlockCode(newBlockAndParkingSlots.get(i).getBlock().getBlockCode());
                Block updateBlock = blockService.updateBlock(bl, bl.getId());
            }

        }
        
        else if (blockAndParkingSlots.size() == newBlockAndParkingSlots.size()) {
            for(int i=0; i< newBlockAndParkingSlots.size(); i++){
                Block bl = blockAndParkingSlots.get(i).getBlock();
                bl.setBlockCode(newBlockAndParkingSlots.get(i).getBlock().getBlockCode());

                int dParkingSlot = newBlockAndParkingSlots.get(i).getNumberOfParkingSlots();
                bl.setNumberOfParkingSlots(dParkingSlot);

                Block updateBlock = blockService.updateBlock(bl, bl.getId());

                if (dParkingSlot < blockAndParkingSlots.get(i).getNumberOfParkingSlots()) {
                    for (int j = dParkingSlot; j < blockAndParkingSlots.get(i).getNumberOfParkingSlots(); j++) {
                        ParkingSlot parkingSlot = parkingSlotService.getParkingSlotBySlotNumberAndBlockId(j+1, updateBlock.getId());
                        parkingSlotService.deleteParkingSlot(parkingSlot.getId());
                    }
                } else if (dParkingSlot > blockAndParkingSlots.get(i).getNumberOfParkingSlots()) {
                    int maxOfSlotNumber = parkingSlotService.findMaxSlotNumber(updateBlock.getId());
                    for (int j = maxOfSlotNumber; j < dParkingSlot; j++) {
                        ParkingSlot parkingSlot = new ParkingSlot();
                        parkingSlot.setBlock(updateBlock);
                        parkingSlot.setSlotNumber(j + 1);
                        parkingSlotService.addParkingSlot(parkingSlot);
                    }
                }
            }
        }
        else {
            for (int i = blockAndParkingSlots.size(); i < newBlockAndParkingSlots.size(); i++) {
                BlockAndParkingSlot blockAndParkingSlot = newBlockAndParkingSlots.get(i);
                Block bl = new Block();
                bl.setBlockCode(blockAndParkingSlot.getBlock().getBlockCode());
                bl.setParkingLot(updateP);
                int dParkingSlot = newBlockAndParkingSlots.get(i).getNumberOfParkingSlots();

                Block newBlock = blockService.createBlock(bl);

                int numberOfSlots = blockAndParkingSlot.getNumberOfParkingSlots();
                for (int j = 0; j < numberOfSlots; j++) {
                    dParkingSlot += 1;
                    ParkingSlot parkingSlot = new ParkingSlot();
                    parkingSlot.setBlock(newBlock);
                    parkingSlot.setSlotNumber(j + 1);
                    parkingSlotService.addParkingSlot(parkingSlot);
                }
                newBlock.setNumberOfParkingSlots(dParkingSlot);

                for(int j=0; j< newBlockAndParkingSlots.size(); j++){
                    Block blck = blockAndParkingSlots.get(j).getBlock();
                    blck.setBlockCode(newBlockAndParkingSlots.get(i).getBlock().getBlockCode());
    
                    blck.setNumberOfParkingSlots(dParkingSlot);
    
                    Block updateBlock = blockService.updateBlock(blck, blck.getId());
    
                     if (dParkingSlot < blockAndParkingSlots.get(i).getNumberOfParkingSlots()) {
                        for (int k = dParkingSlot; k < blockAndParkingSlots.get(i).getNumberOfParkingSlots(); k++) {
                            ParkingSlot parkingSlot = parkingSlotService.getParkingSlotBySlotNumberAndBlock(k+1, blck);
                            parkingSlotService.deleteParkingSlot(parkingSlot.getId());
                        }
                    } else if (dParkingSlot > blockAndParkingSlots.get(i).getNumberOfParkingSlots()) {
                        int maxOfSlotNumber = parkingSlotService.findMaxSlotNumber(updateBlock.getId());
                        for (int k = maxOfSlotNumber; k < dParkingSlot; k++) {
                            ParkingSlot parkingSlot = new ParkingSlot();
                            parkingSlot.setBlock(updateBlock);
                            parkingSlot.setSlotNumber(k + 1);
                            parkingSlotService.addParkingSlot(parkingSlot);
                        }
                    }
                }
            }
        }

        updateP.setNumberOfBlocks(dBlock);
        parkingLotService.updateParkingLot(updateP, updateP.getId());

        return new ResponseEntity<>(parkingLotAndBlockForm, HttpStatus.OK);
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<ResponseMessage> deleteParkingLot(@PathVariable Long id) {
        parkingLotService.deleteParkingLot(id);
        return new ResponseEntity<>(new ResponseMessage("Deleted parking lot successfully"), HttpStatus.OK);
    }

//    @GetMapping("/mana")
//    public ResponseEntity<?> countUsedParkingSlots() {
//        return new ResponseEntity<>(parkingLotService.countUsedParkingSlots(), HttpStatus.OK);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingLotDetails> showParkingLot(@PathVariable Long id) {
        ParkingLot parkingLot = parkingLotService.showParkingLot(id);
        parkingLot.setUsedSlots(parkingLotService.countUsedParkingSlots(id));

        List<ParkingLot> suggestions = parkingLotService.getParkingLotsByAddressAndReentryAllowedAndIdNot(parkingLot.getAddress(), parkingLot.isReentryAllowed(), id);
        for (ParkingLot suggestion : suggestions) {
            suggestion.setUsedSlots(parkingLotService.countUsedParkingSlots(suggestion.getId()));
        }

        ParkingLotDetails parkingLotDetails = new ParkingLotDetails();
        parkingLotDetails.setParkingLot(parkingLot);
        parkingLotDetails.setSuggestions(suggestions);
        return new ResponseEntity<>(parkingLotDetails, HttpStatus.OK);
    }

    @GetMapping("/admin/show/{id}")
    public ResponseEntity<ParkingLotAndBlockForm> showParkingLotAndBlock(@PathVariable Long id) {
        return new ResponseEntity<>(parkingLotService.getParkingLotAndBlockForm(id), HttpStatus.OK);
    }
//
//    @PutMapping("admin/update/{id}")
//    public ResponseEntity<ParkingLot> updateParkingLotAdmin(@RequestBody ParkingLot parkingLot, @PathVariable Long id) {
//        return new ResponseEntity<>(parkingLotService.updateParkingLot(parkingLot, id), HttpStatus.OK);
//    }
}
