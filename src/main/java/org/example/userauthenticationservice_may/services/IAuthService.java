package org.example.userauthenticationservice_may.services;

import org.example.userauthenticationservice_may.models.User;
import org.springframework.util.MultiValueMap;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Set;

public interface IAuthService {
    User signup(String email, String password, Set<String> roles);

    Pair<User, MultiValueMap<String,String>> login(String email, String password);

    Boolean validateToken(String token,Long userId);
}
