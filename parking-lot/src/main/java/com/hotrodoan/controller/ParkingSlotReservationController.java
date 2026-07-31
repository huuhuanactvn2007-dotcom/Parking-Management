package com.hotrodoan.controller;

import com.hotrodoan.model.*;
import com.hotrodoan.model.dto.AvailableParkingSlotsInfo;
import com.hotrodoan.model.dto.ParkingSlotReservationSub;
import com.hotrodoan.model.dto.ResponseMessage;
import com.hotrodoan.model.dto.VNPayMessage;
import com.hotrodoan.security.jwt.JwtProvider;
import com.hotrodoan.security.jwt.JwtTokenFilter;
import com.hotrodoan.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("/parking-slot-reservations")
@CrossOrigin(origins = "*")
public class ParkingSlotReservationController {
    @Autowired
    private ParkingSlotReservationService parkingSlotReservationService;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private JwtTokenFilter jwtTokenFilter;
    @Autowired
    private UserService userService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ParkingSlotService parkingSlotService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private ParkingLotService parkingLotService;
    @Autowired
    private RegularPassService regularPassService;
    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private ParkingSlotReservationSubService parkingSlotReservationSubService;

    @GetMapping("/admin")
    public ResponseEntity<Page<ParkingSlotReservation>> getAllParkingSlotReservations(@RequestParam(defaultValue = "") String dateStr,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "10") int size,
                                                                                    @RequestParam(defaultValue = "id") String sortBy,
                                                                                    @RequestParam(defaultValue = "desc") String order) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sortBy));
        Date sqlSDate = null;
        if (!dateStr.isEmpty()) {
            try {
                java.util.Date parsed = format.parse(dateStr);
                sqlSDate = new Date(parsed.getTime());
            } catch (ParseException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(parkingSlotReservationService.getAllParkingSlotReservations(sqlSDate, pageable), HttpStatus.OK);
        }else {
            return new ResponseEntity<>(parkingSlotReservationService.getAllParkingSlotReservations(pageable), HttpStatus.OK);
        }
    }

    @GetMapping("/show")
    public ResponseEntity<Page<ParkingSlotReservation>> showParkingSlotReservations(HttpServletRequest request,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "10") int size,
                                                                                    @RequestParam(defaultValue = "id") String sortBy,
                                                                                    @RequestParam(defaultValue = "desc") String order) {
        String jwt = jwtTokenFilter.getJwt(request);
        String username = jwtProvider.getUsernameFromToken(jwt);
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer = customerService.getCustomerByUser(user);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc(sortBy)));
        return new ResponseEntity<>(parkingSlotReservationService.getAllParkingSlotReservationsByCustomer(customer, pageable), HttpStatus.OK);
    }

    @GetMapping("/add")
    public ResponseEntity<List<AvailableParkingSlotsInfo>> getAllParkingSlotAvailable(@RequestParam("startTimestamp") String startTimestampStr,
                                                                                      @RequestParam("durationInMinutes") int durationInMinutes,
                                                                                      @RequestParam("id") Long id) {
        Timestamp startTimestamp = Timestamp.valueOf(startTimestampStr);
        if (startTimestamp.before(Timestamp.valueOf(LocalDateTime.now()))) {
            throw new RuntimeException("Time in future");
        }
        return new ResponseEntity<>(parkingSlotReservationService.findAvailableParkingSlotsAndBlockAndParkingLot(startTimestamp, durationInMinutes, id), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<VNPayMessage> createParkingSlotReservation(HttpServletRequest request, @RequestBody ParkingSlotReservation parkingSlotReservation) {
        String jwt = jwtTokenFilter.getJwt(request);
        String username = jwtProvider.getUsernameFromToken(jwt);
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer = customerService.getCustomerByUser(user);
        parkingSlotReservation.setCustomer(customer);
        parkingSlotReservation.setBookingDate(Timestamp.valueOf(LocalDateTime.now()));

        Timestamp startTimestamp = parkingSlotReservation.getStartTimestamp();
        if (startTimestamp.before(Timestamp.valueOf(LocalDateTime.now()))) {
            throw new RuntimeException("Time in future");
        }
        else {
            RegularPass regularPass = regularPassService.getRegularByCustomer(customer);
            if (regularPass != null && regularPass.getStartDate().before(Timestamp.valueOf(LocalDateTime.now())) && regularPass.getEndDate().after(Timestamp.valueOf(LocalDateTime.now()))){
                parkingSlotReservation.setCost(0);
            } else {
                if (parkingSlotReservation.getDurationInMinutes() <= 60) {
                    parkingSlotReservation.setCost(20000);
                }
                else {
                    int cost = 20000 + (parkingSlotReservation.getDurationInMinutes() - 60)*(20000/60);
                    parkingSlotReservation.setCost(cost);
                }
            }
//        ParkingSlotReservation newParkingSlotReservation = parkingSlotReservationService.createParkingSlotReservation(parkingSlotReservation);

            ParkingSlotReservationSub parkingSlotReservationSub = parkingSlotReservationSubService.createParkingSlotReservationSub(parkingSlotReservation);
            ParkingSlot parkingSlot = parkingSlotReservation.getParkingSlot();
            if(parkingSlot.isSlotAvailable() == false) {
                throw new RuntimeException("Parking slot is not available");
            }else{
                Long parkingSlotId = parkingSlot.getId();
                parkingSlot = parkingSlotService.getParkingSlot(parkingSlotId);
//            return new ResponseEntity<>(newParkingSlotReservation, HttpStatus.OK);

                if (parkingSlotReservation.getCost() > 0){
                    String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                    String vnpayUrl = vnPayService.createOrder(parkingSlotReservationSub.getCost(), parkingSlotReservationSub.getId().toString()+"thu2", baseUrl);
                    VNPayMessage VNPayMessage = new VNPayMessage("payment", vnpayUrl);
                    return new ResponseEntity<>(VNPayMessage, HttpStatus.OK);
                }else {
                    parkingSlotReservationSub.setPair(true);
                    parkingSlotReservationSubService.updateParkingSlotReservationSub(parkingSlotReservationSub, parkingSlotReservationSub.getId());
                    ParkingSlotReservation newParkingSlotReservation1 = parkingSlotReservationService.createParkingSlotReservationBySub(parkingSlotReservationSub);
                    parkingSlot.setSlotAvailable(false);
                    parkingSlotService.updateParkingSlot(parkingSlot, parkingSlotId);
                    return new ResponseEntity<>(new VNPayMessage("no-payment", "free"), HttpStatus.OK);
                }
            }
        }
    }

    public boolean isParkingSlotAvailable(ParkingSlot parkingSlot) {
        return parkingSlot.isSlotAvailable();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ParkingSlotReservation> updateParkingSlotReservation(@RequestBody ParkingSlotReservation parkingSlotReservation, @PathVariable Long id) {
        return new ResponseEntity<>(parkingSlotReservationService.updateParkingSlotReservation(parkingSlotReservation, id), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseMessage> deleteParkingSlotReservation(@PathVariable Long id) {
        ParkingSlotReservation parkingSlotReservation = parkingSlotReservationService.getParkingSlotReservation(id);
        Timestamp bookingDateTime = parkingSlotReservation.getBookingDate();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(bookingDateTime);
        cal2.setTime(now);
        long hoursBetween = Math.abs(cal1.getTimeInMillis() - cal2.getTimeInMillis()) / (60 * 60 * 1000);

        if (hoursBetween > 1) {
            return new ResponseEntity<>(new ResponseMessage("Cannot delete booking > 1 hour"), HttpStatus.BAD_REQUEST);
        }
        else {
            parkingSlotReservationService.deleteParkingSlotReservation(id);
//            Pa
            return new ResponseEntity<>(new ResponseMessage("Deleted Success"), HttpStatus.OK);
        }
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<ParkingSlotReservation> getParkingSlotReservation(@PathVariable Long id) {
        return new ResponseEntity<>(parkingSlotReservationService.getParkingSlotReservation(id), HttpStatus.OK);
    }
}
