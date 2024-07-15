package org.example.userauthenticationservice_may.dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequestDto {
    private String email;

    private String password;
}
