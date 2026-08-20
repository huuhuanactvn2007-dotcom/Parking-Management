package com.hotrodoan.service.impl;

import com.hotrodoan.model.*;
import com.hotrodoan.model.dto.AvailableParkingSlotsInfo;
import com.hotrodoan.model.dto.ParkingSlotReservationSub;
import com.hotrodoan.repository.ParkingSlotReservationRepository;
import com.hotrodoan.service.BlockService;
import com.hotrodoan.service.ParkingLotService;
import com.hotrodoan.service.ParkingSlotReservationService;
import com.hotrodoan.service.ParkingSlotService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ParkingSlotReservationServiceImpl implements ParkingSlotReservationService {
    @Autowired
    private ParkingSlotReservationRepository parkingSlotReservationRepository;
    @Autowired
    private ParkingSlotService parkingSlotService;
    @Autowired
    private ParkingLotService parkingLotService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Page<ParkingSlotReservation> getAllParkingSlotReservations(Pageable pageable) {
        return parkingSlotReservationRepository.findAll(pageable);
    }

    @Override
    public ParkingSlotReservation createParkingSlotReservation(ParkingSlotReservation parkingSlotReservation) {
        Long slotId = parkingSlotReservation.getParkingSlot().getId();
        RLock lock = redissonClient.getLock("lock:slot:" + slotId);
        boolean isAcquired = false;

        try {
            // Chờ tối đa 5s để lấy khóa, tự động gia hạn với Watchdog (leaseTime = -1)
            isAcquired = lock.tryLock(5, -1, TimeUnit.SECONDS);
            if (!isAcquired) {
                throw new RuntimeException("Không thể lấy khóa phân tán cho slot ID: " + slotId);
            }

            // Kiểm tra trùng lịch đặt chỗ trong DB
            Timestamp startTimestamp = parkingSlotReservation.getStartTimestamp();
            int duration = parkingSlotReservation.getDurationInMinutes();
            LocalDateTime start = startTimestamp.toLocalDateTime();
            LocalDateTime desiredEnd = start.plusMinutes(duration);

            List<ParkingSlotReservation> existingList = parkingSlotReservationRepository.findByParkingSlot(parkingSlotReservation.getParkingSlot());
            boolean isConflict = existingList.stream().anyMatch(res -> {
                LocalDateTime resStart = res.getStartTimestamp().toLocalDateTime();
                LocalDateTime resEnd = resStart.plusMinutes(res.getDurationInMinutes());
                return start.isBefore(resEnd) && desiredEnd.isAfter(resStart);
            });

            if (isConflict) {
                throw new RuntimeException("Vị trí đỗ xe đã có người đặt trong khung giờ này!");
            }

            Timestamp now = Timestamp.from(Instant.now());
            parkingSlotReservation.setBookingDate(now);
            return parkingSlotReservationRepository.save(parkingSlotReservation);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Giao dịch bị gián đoạn: " + e.getMessage());
        } finally {
            if (isAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Page<ParkingSlotReservation> getAllParkingSlotReservations(Date date, Pageable pageable) {
        return parkingSlotReservationRepository.findByBookingDate(date, pageable);
    }

    @Override
    public ParkingSlotReservation updateParkingSlotReservation(ParkingSlotReservation parkingSlotReservation, Long id) {
        return parkingSlotReservationRepository.findById(id).map(ps -> {
            ps.setCustomer(parkingSlotReservation.getCustomer());
            ps.setDurationInMinutes(parkingSlotReservation.getDurationInMinutes());
            ps.setBookingDate(parkingSlotReservation.getBookingDate());
            ps.setParkingSlot(parkingSlotReservation.getParkingSlot());
            ps.setCost(parkingSlotReservation.getCost());
            ps.setConfirmName(parkingSlotReservation.getConfirmName());
            ps.setPhoneNumber(parkingSlotReservation.getPhoneNumber());
            ps.setConfirmVehicleNumber(parkingSlotReservation.getConfirmVehicleNumber());
            ps.setPair(parkingSlotReservation.isPair());
            return parkingSlotReservationRepository.save(ps);
        }).orElseThrow(() -> new RuntimeException("Not found parking slot reservation"));
    }

    @Override
    public void deleteParkingSlotReservation(Long id) {
        parkingSlotReservationRepository.deleteById(id);
    }

    @Override
    public ParkingSlotReservation getParkingSlotReservation(Long id) {
        return parkingSlotReservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found parking slot reservation"));
    }

    @Override
    public Page<ParkingSlotReservation> getAllParkingSlotReservationsByCustomer(Customer customer, Pageable pageable) {
        return parkingSlotReservationRepository.findByCustomer(customer, pageable);
    }

    @Override
    public List<ParkingSlot> findAvailableParkingSlots(int slotNumber, Date bookingDate, int durationInMinutes) {
        return null;
    }

    @Override
    public List<ParkingSlotReservation> getParkingSlotReservationsByParkingSlot(ParkingSlot parkingSlot) {
        return parkingSlotReservationRepository.findByParkingSlot(parkingSlot);
    }

    @Override
    public List<AvailableParkingSlotsInfo> findAvailableParkingSlotsAndBlockAndParkingLot(Timestamp startTimestamp, int durationInMinutes, Long id) {
        ParkingLot parkingLot = parkingLotService.getParkingLot(id);
        List<Block> allBlocks = blockService.getBlockByParkingLot(parkingLot);
        List<ParkingSlot> allParkingSlots = new ArrayList<>();

        for (Block block : allBlocks) {
            List<ParkingSlot> parkingSlots = parkingSlotService.getParkingSlotByBlock(block);
            allParkingSlots.addAll(parkingSlots);
        }

        LocalDateTime startLocalDateTime = startTimestamp.toLocalDateTime();

        Map<Block, List<ParkingSlot>> availableSlotsByBlock = allParkingSlots.stream()
                .filter(parkingSlot -> {
                    List<ParkingSlotReservation> reservations = parkingSlotReservationRepository.findByParkingSlot(parkingSlot);
                    return reservations.stream()
                            .noneMatch(reservation -> {
                                LocalDateTime reservationEnd = reservation.getStartTimestamp().toLocalDateTime().plusMinutes(reservation.getDurationInMinutes());
                                LocalDateTime desiredEnd = startLocalDateTime.plusMinutes(durationInMinutes);
                                return startLocalDateTime.isBefore(reservationEnd) && desiredEnd.isAfter(reservation.getStartTimestamp().toLocalDateTime());
                            });
                })
                .collect(Collectors.groupingBy(ParkingSlot::getBlock));

        List<AvailableParkingSlotsInfo> result = new ArrayList<>();
        for (Map.Entry<Block, List<ParkingSlot>> entry : availableSlotsByBlock.entrySet()) {
            AvailableParkingSlotsInfo info = new AvailableParkingSlotsInfo();
            info.setParkingLot(entry.getKey().getParkingLot());
            info.setBlock(entry.getKey());
            info.setAvailableParkingSlots(entry.getValue());
            result.add(info);
        }

        return result;
    }

    @Override
    public List<ParkingSlotReservation> findPastReservations() {
        List<ParkingSlotReservation> allReservations = parkingSlotReservationRepository.findAll();
        return allReservations.stream()
                .filter(reservation -> reservation.getStartTimestamp().toInstant()
                        .plusMillis(reservation.getDurationInMinutes() * 60 * 1000)
                        .isBefore(Instant.now()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ParkingSlotReservation> getAllParkingSlotReservations() {
        return parkingSlotReservationRepository.findAll();
    }

    @Override
    public ParkingSlotReservation createParkingSlotReservationBySub(ParkingSlotReservationSub parkingSlotReservationSub) {
        ParkingSlotReservation parkingSlotReservation = new ParkingSlotReservation();
        parkingSlotReservation.setCustomer(parkingSlotReservationSub.getCustomer());
        parkingSlotReservation.setStartTimestamp(parkingSlotReservationSub.getStartTimestamp());
        parkingSlotReservation.setDurationInMinutes(parkingSlotReservationSub.getDurationInMinutes());
        parkingSlotReservation.setBookingDate(parkingSlotReservationSub.getBookingDate());
        parkingSlotReservation.setParkingSlot(parkingSlotReservationSub.getParkingSlot());
        parkingSlotReservation.setCost(parkingSlotReservationSub.getCost());
        parkingSlotReservation.setConfirmName(parkingSlotReservationSub.getConfirmName());
        parkingSlotReservation.setPhoneNumber(parkingSlotReservationSub.getPhoneNumber());
        parkingSlotReservation.setConfirmVehicleNumber(parkingSlotReservationSub.getConfirmVehicleNumber());
        parkingSlotReservation.setPair(parkingSlotReservationSub.isPair());
        return parkingSlotReservationRepository.save(parkingSlotReservation);
    }
}
