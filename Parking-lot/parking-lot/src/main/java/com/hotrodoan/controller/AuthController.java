package com.hotrodoan.controller;

import com.hotrodoan.model.*;
import com.hotrodoan.model.dto.*;
import com.hotrodoan.security.jwt.JwtProvider;
import com.hotrodoan.security.jwt.JwtTokenFilter;
import com.hotrodoan.security.userdetail.CustomUserDetail;
import com.hotrodoan.service.CustomerService;
import com.hotrodoan.service.RegularPassService;
import com.hotrodoan.service.RoleService;
import com.hotrodoan.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RegularPassService regularPassService;

    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody SignupForm signupForm, HttpServletRequest request) {
        if(userService.existsByUsername(signupForm.getUsername())){
            return new ResponseEntity<>(new ResponseMessage("username_exists"), HttpStatus.OK);
        }
        if(userService.existsByEmail(signupForm.getEmail())){
            return new ResponseEntity<>(new ResponseMessage("email_exists"), HttpStatus.OK);
        }

        User user = new User(signupForm.getName(), signupForm.getUsername(), passwordEncoder.encode(signupForm.getPassword()), signupForm.getEmail(), signupForm.getAvatar());
        Set<String> strRoles = signupForm.getRoles();
        Set<Role> roles = new HashSet<>();
        if (strRoles == null){
            Role userRole = roleService.findByName(RoleName.USER).orElseThrow(() -> new RuntimeException("Role not found"));
            roles.add(userRole);
        }
        else {
            strRoles.forEach(role -> {
                switch (role){
                    case "admin":
                        Role adminRole = roleService.findByName(RoleName.ADMIN).orElseThrow(() -> new RuntimeException("Role not found"));
                        roles.add(adminRole);
                        break;
                    case "pm":
                        Role pmRole = roleService.findByName(RoleName.PM).orElseThrow(() -> new RuntimeException("Role not found"));
                        roles.add(pmRole);
                        break;
                    default:
                        Role userRole = roleService.findByName(RoleName.USER).orElseThrow(() -> new RuntimeException("Role not found"));
                        roles.add(userRole);
                        break;
                }
            });
        }
//        roles.add(roleService.findByName(RoleName.USER).orElseThrow(() -> new RuntimeException("Role not found")));
        user.setRoles(roles);
        userService.save(user);
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setRegistrationDate(new Date(System.currentTimeMillis()));
        customerService.addCustomer(customer);
        return new ResponseEntity<>(new ResponseMessage("create_success"), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginForm loginForm){
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginForm.getUsername(), loginForm.getPassword())
            );
            // Lấy thông tin người dùng từ Principal
            CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
            if (!userDetail.isEnabled()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User is not enabled. Please contact administrator.");
            } else {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = jwtProvider.createToken(authentication);
                return ResponseEntity.ok(new JwtResponse(token, userDetail.getId(), userDetail.getName(), userDetail.getAuthorities(), userDetail.getAvatar()));
            }
        } catch (BadCredentialsException ex) {
            if (userService.findByUsername(loginForm.getUsername()).isPresent()) {
                return new ResponseEntity<>(new ResponseMessage("wrong_password"), HttpStatus.UNAUTHORIZED);
            } else {
                return new ResponseEntity<>(new ResponseMessage("username_not_exists"), HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @RequestBody ChangePasswordForm changePasswordForm){
        String token = jwtTokenFilter.getJwt(request);
        String username = jwtProvider.getUsernameFromToken(token);
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        if(passwordEncoder.matches(changePasswordForm.getOldPassword(), user.getPassword())){
            if(!changePasswordForm.getNewPassword().equals(changePasswordForm.getConfirmPassword())){
                return new ResponseEntity<>(new ResponseMessage("confirm_password_not_match"), HttpStatus.BAD_REQUEST);
            }
            user.setPassword(passwordEncoder.encode(changePasswordForm.getNewPassword()));
            userService.save(user);
            return new ResponseEntity<>(new ResponseMessage("change_password_success"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ResponseMessage("change_password_fail"), HttpStatus.UNAUTHORIZED);
    }
//        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        User user = userService.findByUsername(userDetail.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

    @GetMapping("/profile")
    public ResponseEntity<CustomerAndRegularPass> getProfile(HttpServletRequest request) {
        String token = jwtTokenFilter.getJwt(request);
        String username = jwtProvider.getUsernameFromToken(token);
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer = customerService.getCustomerByUser(user);
        RegularPass regularPass = regularPassService.getRegularByCustomer(customer);
        CustomerAndRegularPass customerAndRegularPass = new CustomerAndRegularPass();
        customerAndRegularPass.setCustomer(customer);
        customerAndRegularPass.setRegularPass(regularPass);
        return new ResponseEntity<>(customerAndRegularPass, HttpStatus.OK);
    }
}
