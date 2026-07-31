package com.hotrodoan.service;

import com.hotrodoan.model.Role;
import com.hotrodoan.model.RoleName;

import java.util.Optional;

public interface RoleService {
    Optional<Role> findByName(RoleName name);
}
