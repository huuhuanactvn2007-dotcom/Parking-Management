package com.hotrodoan.controller;

import com.hotrodoan.model.Block;
import com.hotrodoan.model.ParkingSlot;
import com.hotrodoan.model.dto.CreateParkingSlotForm;
import com.hotrodoan.service.ParkingSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parking-slots")
@CrossOrigin(origins = "*")
public class ParkingSlotController {
    @Autowired
    private ParkingSlotService parkingSlotService;

    @GetMapping("")
    public ResponseEntity<List<ParkingSlot>> getAllParkingSlots(Block block) {
        return ResponseEntity.ok(parkingSlotService.getParkingSlotByBlock(block));
    }

    // @PostMapping("/admin/add")
    // public ResponseEntity<ParkingSlot> addParkingSlot(@RequestBody CreateParkingSlotForm createParkingSlotForm) {
    //     ParkingSlot parkingSlot = new ParkingSlot();
    //     parkingSlot.setBlock(createParkingSlotForm.getBlock());
        
    //     int maxOfSlot = parkingSlotService.findMaxSlotNumber();
    //     for (int i = 0; i < createParkingSlotForm.getWantManySlots(); i++) {
    //         parkingSlot.setSlotNumber(maxOfSlot+i+1);
    //     }
    //     return new ResponseEntity<>(parkingSlotService.addParkingSlot(parkingSlot), HttpStatus.CREATED);
    // }

    @PutMapping("/admin/update/{id}")
    public ResponseEntity<ParkingSlot> updateParkingSlot(@RequestBody ParkingSlot parkingSlot, @PathVariable Long id) {
        return ResponseEntity.ok(parkingSlotService.updateParkingSlot(parkingSlot, id));
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<Void> deleteParkingSlot(@PathVariable Long id) {
        parkingSlotService.deleteParkingSlot(id);
        return ResponseEntity.ok().build();
    }
}
