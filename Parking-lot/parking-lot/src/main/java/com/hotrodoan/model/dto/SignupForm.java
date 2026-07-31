package com.hotrodoan.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupForm {
    private String name;

    private String username;

    private String password;

    private String email;

    private Set<String> roles;

    private String avatar = "https://firebasestorage.googleapis.com/v0/b/lvkmusic.appspot.com/o/umgu5ofe2y?alt=media&token=d9756614-c86c-4269-b46e-8d4b8b2f02d3";
}
