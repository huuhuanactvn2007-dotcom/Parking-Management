package com.hotrodoan.service.impl;

import com.hotrodoan.model.ParkingSlotReservation;
import com.hotrodoan.model.dto.ParkingSlotReservationSub;
import com.hotrodoan.repository.ParkingSlotReservationSubRepository;
import com.hotrodoan.service.ParkingSlotReservationSubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParkingSlotReservationSubServiceImpl implements ParkingSlotReservationSubService {
    @Autowired
    private ParkingSlotReservationSubRepository parkingSlotReservationSubRepository;
    @Override
    public ParkingSlotReservationSub createParkingSlotReservationSub(ParkingSlotReservation parkingSlotReservation) {
        ParkingSlotReservationSub parkingSlotReservationSub = new ParkingSlotReservationSub();
        parkingSlotReservationSub.setCustomer(parkingSlotReservation.getCustomer());
        parkingSlotReservationSub.setStartTimestamp(parkingSlotReservation.getStartTimestamp());
        parkingSlotReservationSub.setDurationInMinutes(parkingSlotReservation.getDurationInMinutes());
        parkingSlotReservationSub.setBookingDate(parkingSlotReservation.getBookingDate());
        parkingSlotReservationSub.setParkingSlot(parkingSlotReservation.getParkingSlot());
        parkingSlotReservationSub.setCost(parkingSlotReservation.getCost());
        parkingSlotReservationSub.setConfirmName(parkingSlotReservation.getConfirmName());
        parkingSlotReservationSub.setPhoneNumber(parkingSlotReservation.getPhoneNumber());
        parkingSlotReservationSub.setConfirmVehicleNumber(parkingSlotReservation.getConfirmVehicleNumber());
        return parkingSlotReservationSubRepository.save(parkingSlotReservationSub);
    }

    @Override
    public void deleteParkingSlotReservationSub(Long id) {
        parkingSlotReservationSubRepository.deleteById(id);
    }

    @Override
    public ParkingSlotReservationSub updateParkingSlotReservationSub(ParkingSlotReservationSub parkingSlotReservationSub, Long id) {
        return parkingSlotReservationSubRepository.findById(parkingSlotReservationSub.getId()).map(ps -> {
            ps.setCustomer(parkingSlotReservationSub.getCustomer());
            ps.setStartTimestamp(parkingSlotReservationSub.getStartTimestamp());
            ps.setDurationInMinutes(parkingSlotReservationSub.getDurationInMinutes());
            ps.setBookingDate(parkingSlotReservationSub.getBookingDate());
            ps.setParkingSlot(parkingSlotReservationSub.getParkingSlot());
            ps.setCost(parkingSlotReservationSub.getCost());
            ps.setConfirmName(parkingSlotReservationSub.getConfirmName());
            ps.setPhoneNumber(parkingSlotReservationSub.getPhoneNumber());
            ps.setConfirmVehicleNumber(parkingSlotReservationSub.getConfirmVehicleNumber());
            ps.setPair(parkingSlotReservationSub.isPair());
            return parkingSlotReservationSubRepository.save(ps);
        }).orElseGet(() -> parkingSlotReservationSubRepository.save(parkingSlotReservationSub));
    }

    @Override
    public ParkingSlotReservationSub getParkingSlotReservationSub(Long id) {
        return parkingSlotReservationSubRepository.findById(id).orElseThrow(() -> new RuntimeException("Parking slot reservation sub not found"));
    }
}
