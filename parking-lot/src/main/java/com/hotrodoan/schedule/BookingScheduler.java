package com.hotrodoan.schedule;

import com.hotrodoan.model.BookingHistory;
import com.hotrodoan.model.ParkingSlot;
import com.hotrodoan.model.ParkingSlotReservation;
import com.hotrodoan.service.BookingHistoryService;
import com.hotrodoan.service.ParkingSlotReservationService;
import com.hotrodoan.service.ParkingSlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class BookingScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BookingScheduler.class);
    @Autowired
    private ParkingSlotReservationService parkingSlotReservationService;
    @Autowired
    private BookingHistoryService bookingHistoryService;
    @Autowired
    private ParkingSlotService parkingSlotService;

    @Scheduled(fixedRate = 60000)
    public void addHistoryFromBooking() {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        List<ParkingSlotReservation> expiredBookings = parkingSlotReservationService.findPastReservations();
        List<ParkingSlotReservation> bookingsToCancel = parkingSlotReservationService.getAllParkingSlotReservations();
        List<ParkingSlot> parkingSlotsToUpdate = new ArrayList<>();
        List<BookingHistory> historiesToAdd = new ArrayList<>();
        for (ParkingSlotReservation booking : expiredBookings) {
            Long parkingSlotId = booking.getParkingSlot().getId();
            ParkingSlot parkingSlot = parkingSlotService.getParkingSlot(parkingSlotId);
            parkingSlot.setSlotAvailable(true);
            parkingSlotService.updateParkingSlot(parkingSlot, parkingSlotId);

            if (booking.isPair() == true){
                BookingHistory history = new BookingHistory();
                history.setCustomer(booking.getCustomer());
                history.setStartTimestamp(booking.getStartTimestamp());
                history.setDurationInMinutes(booking.getDurationInMinutes());
                history.setBookingDate(booking.getBookingDate());
                history.setParkingSlot(booking.getParkingSlot());
                history.setCost(booking.getCost());

                bookingHistoryService.addBookingHistory(history);
                parkingSlotReservationService.deleteParkingSlotReservation(booking.getId());
            }
        }
        for (ParkingSlotReservation booking : bookingsToCancel) {
            if (booking.isPair() == false) {
                parkingSlotReservationService.deleteParkingSlotReservation(booking.getId());
            }
        }
    }
}
