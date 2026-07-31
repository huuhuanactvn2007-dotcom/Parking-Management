package com.hotrodoan.service;

import com.hotrodoan.model.ParkingSlotReservation;
import com.hotrodoan.model.dto.ParkingSlotReservationSub;

public interface ParkingSlotReservationSubService {
    ParkingSlotReservationSub createParkingSlotReservationSub(ParkingSlotReservation parkingSlotReservation);
    void deleteParkingSlotReservationSub(Long id);
    ParkingSlotReservationSub updateParkingSlotReservationSub(ParkingSlotReservationSub parkingSlotReservationSub, Long id);
    ParkingSlotReservationSub getParkingSlotReservationSub(Long id);
}
