package com.hotrodoan.controller;

import com.hotrodoan.model.*;
import com.hotrodoan.model.dto.AvailableParkingSlotsInfo;
import com.hotrodoan.model.dto.ParkingSlotReservationSub;
import com.hotrodoan.model.dto.ResponseMessage;
import com.hotrodoan.model.dto.VNPayMessage;
import com.hotrodoan.repository.ParkingSlotReservationSubRepository;
import com.hotrodoan.security.jwt.JwtProvider;
import com.hotrodoan.security.jwt.JwtTokenFilter;
import com.hotrodoan.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private ParkingSlotReservationSubRepository parkingSlotReservationSubRepository;
    @Autowired
    private RedissonClient redissonClient;

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
        } else {
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
    public ResponseEntity<?> createParkingSlotReservation(HttpServletRequest request, @RequestBody ParkingSlotReservation parkingSlotReservation) {
        if (parkingSlotReservation.getParkingSlot() == null || parkingSlotReservation.getParkingSlot().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("Parking slot is required"));
        }

        Long slotId = parkingSlotReservation.getParkingSlot().getId();
        RLock lock = redissonClient.getLock("lock:slot:" + slotId);
        boolean isAcquired = false;

        try {
            // Khóa phân tán: Chờ tối đa 50ms (Fail-fast để chặn Race Condition), tự gia hạn Watchdog
            isAcquired = lock.tryLock(50, -1, TimeUnit.MILLISECONDS);
            if (!isAcquired) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMessage("Slot đang có giao dịch xử lý đồng thời! Vui lòng thử lại."));
            }

            // Kiểm tra trùng lịch đặt trước trong DB
            Timestamp startTimestamp = parkingSlotReservation.getStartTimestamp();
            if (startTimestamp.before(Timestamp.valueOf(LocalDateTime.now()))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("Thời gian đặt phải ở tương lai"));
            }

            LocalDateTime start = startTimestamp.toLocalDateTime();
            LocalDateTime desiredEnd = start.plusMinutes(parkingSlotReservation.getDurationInMinutes());

            List<ParkingSlotReservationSub> existingSubs = parkingSlotReservationSubRepository.findByParkingSlot(parkingSlotReservation.getParkingSlot());
            boolean isConflict = existingSubs.stream().anyMatch(sub -> {
                LocalDateTime subStart = sub.getStartTimestamp().toLocalDateTime();
                LocalDateTime subEnd = subStart.plusMinutes(sub.getDurationInMinutes());
                return start.isBefore(subEnd) && desiredEnd.isAfter(subStart);
            });

            if (isConflict) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMessage("Vị trí đỗ xe đã được giữ chỗ trong khung giờ này!"));
            }

            String jwt = jwtTokenFilter.getJwt(request);
            String username = jwtProvider.getUsernameFromToken(jwt);
            User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            Customer customer = customerService.getCustomerByUser(user);
            parkingSlotReservation.setCustomer(customer);
            parkingSlotReservation.setBookingDate(Timestamp.valueOf(LocalDateTime.now()));

            ParkingSlot parkingSlot = parkingSlotService.getParkingSlot(slotId);
            if (parkingSlot == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage("Không tìm thấy slot"));
            }

            RegularPass regularPass = regularPassService.getRegularByCustomer(customer);
            if (regularPass != null && regularPass.getStartDate().before(Timestamp.valueOf(LocalDateTime.now())) && regularPass.getEndDate().after(Timestamp.valueOf(LocalDateTime.now()))) {
                parkingSlotReservation.setCost(0);
            } else {
                if (parkingSlotReservation.getDurationInMinutes() <= 60) {
                    parkingSlotReservation.setCost(20000);
                } else {
                    int cost = 20000 + (parkingSlotReservation.getDurationInMinutes() - 60) * (20000 / 60);
                    parkingSlotReservation.setCost(cost);
                }
            }

            ParkingSlotReservationSub parkingSlotReservationSub = parkingSlotReservationSubService.createParkingSlotReservationSub(parkingSlotReservation);

            if (parkingSlotReservation.getCost() > 0) {
                String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                String vnpayUrl = vnPayService.createOrder(parkingSlotReservationSub.getCost(), parkingSlotReservationSub.getId().toString() + "thu2", baseUrl);
                return ResponseEntity.ok(new VNPayMessage("payment", vnpayUrl));
            } else {
                parkingSlotReservationSub.setPair(true);
                parkingSlotReservationSubService.updateParkingSlotReservationSub(parkingSlotReservationSub, parkingSlotReservationSub.getId());
                parkingSlotReservationService.createParkingSlotReservationBySub(parkingSlotReservationSub);
                parkingSlot.setSlotAvailable(false);
                parkingSlotService.updateParkingSlot(parkingSlot, slotId);
                return ResponseEntity.ok(new VNPayMessage("no-payment", "free"));
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage("Giao dịch bị gián đoạn"));
        } finally {
            if (isAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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
        } else {
            parkingSlotReservationService.deleteParkingSlotReservation(id);
            return new ResponseEntity<>(new ResponseMessage("Deleted Success"), HttpStatus.OK);
        }
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<ParkingSlotReservation> getParkingSlotReservation(@PathVariable Long id) {
        return new ResponseEntity<>(parkingSlotReservationService.getParkingSlotReservation(id), HttpStatus.OK);
    }
}
