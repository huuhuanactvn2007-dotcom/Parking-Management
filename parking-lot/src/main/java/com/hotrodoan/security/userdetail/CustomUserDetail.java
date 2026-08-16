package com.hotrodoan.security.userdetail;

import com.hotrodoan.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
public class CustomUserDetail implements UserDetails {
    private Long id;
    private String name;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private Collection<? extends GrantedAuthority> roles;

    public static CustomUserDetail build(User user){
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> {
                String roleName = role.getName().name();
                authorities.add(new SimpleGrantedAuthority(roleName));
                if (!roleName.startsWith("ROLE_")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                }
            });
        }
        return new CustomUserDetail(user.getId(), user.getName(), user.getUsername(), user.getPassword(), user.getEmail(), user.getAvatar(), authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
