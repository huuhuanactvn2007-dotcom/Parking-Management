package com.hotrodoan.service.impl;

import com.hotrodoan.model.Customer;
import com.hotrodoan.model.RegularPass;
import com.hotrodoan.model.dto.RegularPassSub;
import com.hotrodoan.repository.RegularPassRepository;
import com.hotrodoan.service.RegularPassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

@Service
public class RegularPassServiceImpl implements RegularPassService {
    @Autowired
    private RegularPassRepository regularPassRepository;
    @Override
    public RegularPass addRegularPass(RegularPass regularPass) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(regularPass.getStartDate());
        calendar.add(Calendar.DATE, regularPass.getDurationInDays());
        regularPass.setEndDate(new Date(calendar.getTimeInMillis()));
        regularPass.setCost(regularPass.getDurationInDays() * 5000);
        regularPass.setPurchaseDate(new Date(System.currentTimeMillis()));
        return regularPassRepository.save(regularPass);
    }

    @Override
    public Page<RegularPass> getAllRegularPass(Pageable pageable) {
        return regularPassRepository.findAll(pageable);
    }

    @Override
    public RegularPass getRegularPass(Long id) {
        return regularPassRepository.findById(id).orElseThrow(() -> new RuntimeException("Regular pass not found"));
    }

    @Override
    public void deleteRegularPass(Long id) {
        regularPassRepository.deleteById(id);
    }

    @Override
    public RegularPass updateRegularPass(RegularPass regularPass, Long id) {
        return regularPassRepository.findById(id).map(r -> {
            r.setCustomer(regularPass.getCustomer());
            r.setPurchaseDate(regularPass.getPurchaseDate());
            r.setStartDate(regularPass.getStartDate());
            r.setEndDate(regularPass.getEndDate());
            r.setDurationInDays(regularPass.getDurationInDays());
            r.setCost(regularPass.getCost());
            r.setPair(regularPass.isPair());
            r.setRenewPair(regularPass.isRenewPair());
            r.setStatusNow(regularPass.isStatusNow());
            return regularPassRepository.save(r);
        }).orElseThrow(() -> new RuntimeException("Regular pass not found"));
    }

    @Override
    public RegularPass getRegularByCustomer(Customer customer) {
        return regularPassRepository.findByCustomer(customer);
    }

    @Override
    public Page<RegularPass> getByCustomerName(String name, Pageable pageable) {
        return regularPassRepository.searchByCustomerName(name, pageable);
    }

    @Override
    public boolean checkRenew(RegularPass regularPass) {
        Date currentDate = new Date(System.currentTimeMillis());
        if(regularPass.getEndDate().before(currentDate))
            return false;
        return true;
    }

    @Override
    public List<RegularPass> getAllRegularPass() {
        return regularPassRepository.findAll();
    }

    @Override
    public RegularPass createRegularPassBySub(RegularPassSub regularPassSub) {
        RegularPass regularPass = new RegularPass();
        regularPass.setCustomer(regularPassSub.getCustomer());
        regularPass.setPurchaseDate(regularPassSub.getPurchaseDate());
        regularPass.setStartDate(regularPassSub.getStartDate());
        regularPass.setEndDate(regularPassSub.getEndDate());
        regularPass.setDurationInDays(regularPassSub.getDurationInDays());
        regularPass.setCost(regularPassSub.getCost());
        regularPass.setPair(regularPassSub.isPair());
        regularPass.setRenewPair(regularPassSub.isRenewPair());
        regularPass.setStatusNow(regularPassSub.isStatusNow());
        return regularPassRepository.save(regularPass);
    }
}
