package com.hotrodoan.controller;

import com.hotrodoan.model.Customer;
import com.hotrodoan.model.Image;
import com.hotrodoan.model.User;
import com.hotrodoan.model.dto.ParkingLotAndBlockForm;
import com.hotrodoan.model.dto.UpdateProfileForm;
import com.hotrodoan.security.jwt.JwtProvider;
import com.hotrodoan.security.jwt.JwtTokenFilter;
import com.hotrodoan.service.CustomerService;
import com.hotrodoan.service.ImageService;
import com.hotrodoan.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "*")
public class ProfileController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private JwtTokenFilter jwtTokenFilter;
    @Autowired
    private ImageService imageService;

    @GetMapping("")
    public ResponseEntity<UpdateProfileForm> getCustomer(HttpServletRequest request) {
        String token = jwtTokenFilter.getJwt(request);
        String username = jwtProvider.getUsernameFromToken(token);
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        UpdateProfileForm updateProfileForm = new UpdateProfileForm();
        Customer customer = customerService.getCustomerByUser(user);
        updateProfileForm.setName(user.getName());
        updateProfileForm.setUsername(user.getUsername());
        updateProfileForm.setEmail(user.getEmail());
        updateProfileForm.setAvatar(user.getAvatar());
        updateProfileForm.setContactNumber(customer.getContactNumber());
        updateProfileForm.setVehicleNumber(customer.getVehicleNumber());
        updateProfileForm.setRegularCustomer(customer.isRegularCustomer());
        return new ResponseEntity(updateProfileForm, HttpStatus.OK);
    }

    @GetMapping("/avatar")
    public ResponseEntity<Resource> viewImage(HttpServletRequest request) throws Exception {
        String token = jwtTokenFilter.getJwt(request);
        String username = jwtProvider.getUsernameFromToken(token);
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Image image = user.getImage();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getFileType()))
                .body(new ByteArrayResource(image.getData()));
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCustomer(HttpServletRequest request,
                                            @RequestParam("name") String name,
//                                            @RequestParam("username") String username,
//                                            @RequestParam("email") String email,
                                            @RequestParam("contactNumber") String contactNumber,
                                            @RequestParam("vehicleNumber") String vehicleNumber,
                                            @RequestParam(value="file", required=false)MultipartFile file) throws Exception {
        String token = jwtTokenFilter.getJwt(request);
        String usname = jwtProvider.getUsernameFromToken(token);
        User user = userService.findByUsername(usname).orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer1 = customerService.getCustomerByUser(user);

        user.setName(name);
//        user.setUsername(username);
//        user.setEmail(email);
        customer1.setContactNumber(contactNumber);
        customer1.setVehicleNumber(vehicleNumber);
//        user.setEmail(updateProfileForm.getEmail());
//        user.setAvatar(updateProfileForm.getAvatar());
//
//        customer1.setVehicleNumber(updateProfileForm.getVehicleNumber());
//        customer1.setContactNumber(updateProfileForm.getContactNumber());

        if (file != null && !file.isEmpty()) {
            String oldImageId = null;
            if (user.getImage() != null && !user.getImage().equals("")){
                oldImageId = user.getImage().getId();
            }
            Image image = imageService.saveImage(file);
            user.setImage(image);
            userService.save(user);
            if (oldImageId != null){
                imageService.deleteImage(oldImageId);
            }
        }
        customerService.updateCustomer(customer1, customer1.getId());
        return new ResponseEntity<>(name, HttpStatus.OK);
    }
}
