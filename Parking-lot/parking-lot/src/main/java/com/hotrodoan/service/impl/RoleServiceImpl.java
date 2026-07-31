package com.hotrodoan.service.impl;

import com.hotrodoan.model.Role;
import com.hotrodoan.model.RoleName;
import com.hotrodoan.repository.RoleRepository;
import com.hotrodoan.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Optional<Role> findByName(RoleName name) {
        return roleRepository.findByName(name);
    }
}
