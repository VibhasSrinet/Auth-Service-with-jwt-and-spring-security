package org.example.userauthenticationservice_may.dtos;

import lombok.Getter;
import lombok.Setter;
import org.example.userauthenticationservice_may.models.Role;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class UserDto {
    private String email;

    private List<String> roles = new ArrayList<>();
}
