package org.example.userauthenticationservice_may.controllers;

import org.example.userauthenticationservice_may.dtos.UserDto;
import org.example.userauthenticationservice_may.models.Role;
import org.example.userauthenticationservice_may.models.User;
import org.example.userauthenticationservice_may.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        User user= userService.getUser(id);
        if(user == null) {
            throw new IllegalArgumentException("User not found!");
        }
        return new ResponseEntity<>(from(user), HttpStatus.OK);
    }

    private UserDto from(User user) {
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        for (Role role : user.getRoles()) {
            userDto.getRoles().add(role.getValue());
        }
        return userDto;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<String> handleException(IllegalArgumentException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
