package com.hotrodoan.controller;

import com.hotrodoan.model.Customer;
import com.hotrodoan.model.RegularPass;
import com.hotrodoan.model.User;
import com.hotrodoan.model.dto.RegularPassSub;
import com.hotrodoan.model.dto.ResponseMessage;
import com.hotrodoan.model.dto.VNPayMessage;
import com.hotrodoan.security.jwt.JwtProvider;
import com.hotrodoan.security.jwt.JwtTokenFilter;
import com.hotrodoan.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.Date;
import java.util.Calendar;

@RestController
@RequestMapping("/regular-passes")
@CrossOrigin(origins = "*")
public class RegularPassController {
    @Autowired
    private RegularPassService regularPassService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private JwtTokenFilter jwtTokenFilter;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private RegularPassSubService regularPassSubService;

    @GetMapping("/admin")
    public ResponseEntity<Page<RegularPass>> getAllRegularPass(@RequestParam(defaultValue = "") String name,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(defaultValue = "id") String sortBy,
                                                               @RequestParam(defaultValue = "desc") String order) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc(sortBy)));
        Page<RegularPass> regularPasses = regularPassService.getByCustomerName(name, pageable);
        return new ResponseEntity<>(regularPasses, regularPasses.isEmpty() ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<RegularPass> getRegularPass(@PathVariable Long id) {
        RegularPass regularPass = regularPassService.getRegularPass(id);
        return new ResponseEntity<>(regularPass, regularPass == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @GetMapping("/show")
    public ResponseEntity<RegularPass> showRegularPass(HttpServletRequest request,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(defaultValue = "id") String sortBy,
                                                             @RequestParam(defaultValue = "desc") String order) {
        String token = jwtTokenFilter.getJwt(request);
        String username = jwtProvider.getUsernameFromToken(token);
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer = customerService.getCustomerByUser(user);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc(sortBy)));
        RegularPass regularPass = regularPassService.getRegularByCustomer(customer);
        return new ResponseEntity<>(regularPass, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<VNPayMessage> addRegularPass(HttpServletRequest request, @RequestBody RegularPass regularPass) {
        String token = jwtTokenFilter.getJwt(request);
        String username = jwtProvider.getUsernameFromToken(token);
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer = customerService.getCustomerByUser(user);
        regularPass.setCustomer(customer);
        int duration = regularPass.getDurationInDays();
        int totalPrice = 5000*duration;
        regularPass.setCost(totalPrice);

        Date startDate = regularPass.getStartDate();
        int durationInDays = regularPass.getDurationInDays();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DATE, durationInDays);
        Date endDate = new Date(c.getTimeInMillis());
        regularPass.setEndDate(endDate);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        RegularPassSub regularPassSub = regularPassSubService.createRegularPassSub(regularPass);
//        RegularPass newRegularPass = regularPassService.addRegularPass(regularPass);

        String vnpayUrl = vnPayService.createOrder(totalPrice, regularPassSub.getId().toString(), baseUrl);

        VNPayMessage VNPayMessage = new VNPayMessage("payment", vnpayUrl);
        return new ResponseEntity<>(VNPayMessage, HttpStatus.OK);
    }

    @PutMapping("renew")
    public ResponseEntity<VNPayMessage> renewRegularPass(HttpServletRequest request, @RequestBody RegularPass regularPass) {
        String token = jwtTokenFilter.getJwt(request);
        String username = jwtProvider.getUsernameFromToken(token);
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer = customerService.getCustomerByUser(user);
        RegularPass r = regularPassService.getRegularByCustomer(customer);

        if (r.getEndDate().after(regularPass.getStartDate())) {
            int totalPrice = 5000 * regularPass.getDurationInDays();
            int duration = regularPass.getDurationInDays();
            Date oldEndDate = r.getEndDate();
            Calendar c = Calendar.getInstance();
            c.setTime(oldEndDate);
            c.add(Calendar.DATE, regularPass.getDurationInDays());
            Date newEndDate = new Date(c.getTimeInMillis());
            regularPass.setEndDate(newEndDate);

            regularPass.setDurationInDays(r.getDurationInDays() + regularPass.getDurationInDays());
            regularPass.setStartDate(r.getStartDate());
            regularPass.setCustomer(customer);
            regularPass.setPurchaseDate(new Date(System.currentTimeMillis()));
            regularPass.setCost(5000 * regularPass.getDurationInDays());

            RegularPass updatedRegularPass = regularPassService.updateRegularPass(regularPass, r.getId());

            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String vnpayUrl = vnPayService.createOrder(totalPrice, updatedRegularPass.getId().toString()+"upd", baseUrl);
            //        return new ResponseEntity<>(regularPassService.updateRegularPass(regularPass, r.getId()), HttpStatus.OK);
            VNPayMessage VNPayMessage = new VNPayMessage("payment for renew", vnpayUrl);
            return new ResponseEntity<>(VNPayMessage, HttpStatus.OK);
        }
        else {
            if (r.getEndDate().before(regularPass.getStartDate()) && regularPass.getStartDate().after(new Date(System.currentTimeMillis()))) {
                int totalPrice = 5000 * regularPass.getDurationInDays();
                r.setCost(totalPrice);
                regularPass.setPurchaseDate(new Date(System.currentTimeMillis()));
                RegularPass updatedRegularPass = regularPassService.updateRegularPass(regularPass, r.getId());
                String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                String vnpayUrl = vnPayService.createOrder(totalPrice, updatedRegularPass.getId().toString()+"upd", baseUrl);
                VNPayMessage VNPayMessage = new VNPayMessage("payment for renew", vnpayUrl);
                return new ResponseEntity<>(VNPayMessage, HttpStatus.OK);
            }
            else {
                throw new RuntimeException("Your card is still valid and the months you entered are not consecutive");
            }
        }
    }
}
