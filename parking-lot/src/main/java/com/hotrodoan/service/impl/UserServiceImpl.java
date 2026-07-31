package com.hotrodoan.service.impl;

import com.hotrodoan.model.User;
import com.hotrodoan.repository.UserRepository;
import com.hotrodoan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Page<User> getAllUser(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> searchUserByKeyword(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, pageable);
    }

    @Override
    public User changePassword(User user, String newPassword) {
        return userRepository.findById(user.getId()).map(u -> {
            u.setPassword(newPassword);
            return userRepository.save(u);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User updateUser(User user, Long id) {
        return userRepository.findById(id).map(u -> {
            u.setName(user.getName());
            u.setUsername(user.getUsername());
            u.setPassword(user.getPassword());
            u.setEmail(user.getEmail());
            u.setImage(user.getImage());
            return userRepository.save(u);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }
}

