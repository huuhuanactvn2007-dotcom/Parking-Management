package com.hotrodoan.service;

import com.hotrodoan.model.ParkingLot;
import com.hotrodoan.model.dto.ParkingLotAndBlockForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ParkingLotService {
    ParkingLot createParkingLot(ParkingLot parkingLot);
    Page<ParkingLot> getAllParkingLots(String name, String address, Pageable pageable);
    ParkingLot updateParkingLot(ParkingLot parkingLot, Long id);
    void deleteParkingLot(Long id);
    ParkingLot getParkingLot(Long id);
    List<Object[]> countUsedParkingSlots();
    List<ParkingLot> getParkingLotsByAddressAndReentryAllowedAndIdNot(String address, boolean reentryAllowed, Long id);
    ParkingLot showParkingLot(Long id);
    int countUsedParkingSlots(Long id);
    ParkingLotAndBlockForm getParkingLotAndBlockForm(Long id);
    Page<ParkingLot> getAllParkingLots(Pageable pageable);
}
