package org.example.userauthenticationservice_may.dtos;

import lombok.Getter;
import lombok.Setter;
import org.example.userauthenticationservice_may.models.Role;

import java.util.Set;

@Getter
@Setter
public class SignupRequestDto {
    private String email;

    private String password;

    private Set<String> roles;
}
