package com.hotrodoan.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private Collection<? extends GrantedAuthority> roles;
    private String avatar;

    public JwtResponse(String token, Long id, String name, Collection<? extends GrantedAuthority> authorities, String avatar) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.roles = authorities;
        this.avatar = avatar;
    }
}
