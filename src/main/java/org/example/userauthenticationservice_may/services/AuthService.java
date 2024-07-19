package org.example.userauthenticationservice_may.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.antlr.v4.runtime.misc.Pair;
import org.example.userauthenticationservice_may.clients.KafkaProducerClient;
import org.example.userauthenticationservice_may.dtos.MessageDto;
import org.example.userauthenticationservice_may.models.Role;
import org.example.userauthenticationservice_may.models.Session;
import org.example.userauthenticationservice_may.models.SessionStatus;
import org.example.userauthenticationservice_may.models.User;
import org.example.userauthenticationservice_may.repositories.RoleRepository;
import org.example.userauthenticationservice_may.repositories.SessionRepository;
import org.example.userauthenticationservice_may.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.http.HttpHeaders;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AuthService implements IAuthService {

    private UserRepository userRepository;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private SessionRepository sessionRepository;

    private SecretKey secretKey;

    private RoleRepository roleRepository;

    private KafkaProducerClient kafkaProducerClient;

    private ObjectMapper objectMapper;

    public AuthService(UserRepository userRepository,BCryptPasswordEncoder bCryptPasswordEncoder,SessionRepository sessionRepository,SecretKey secretKey, RoleRepository roleRepository, KafkaProducerClient kafkaProducerClient, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.sessionRepository = sessionRepository;
        this.secretKey = secretKey;
        this.roleRepository = roleRepository;
        this.kafkaProducerClient = kafkaProducerClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public User signup(String email, String password, Set<String> roles) {
         Optional<User> userOptional = userRepository.findByEmail(email);
         if(userOptional.isPresent()) {
             return userOptional.get();
         }

         User user = new User();
         user.setEmail(email);
         user.setPassword(bCryptPasswordEncoder.encode(password));
         Set<Role> roleList = new HashSet<>();
            for(String role:roles) {
                Optional<Role> roleOptional = Optional.ofNullable(roleRepository.findByValue(role));
                if(roleOptional.isPresent()){
                    roleList.add(roleOptional.get());
                    continue;
                }
                Role roleObj = new Role();
                roleObj.setValue(role);
                roleList.add(roleObj);
                roleRepository.save(roleObj);
            }
         user.setRoles(roleList);
         userRepository.save(user);
        MessageDto messageDto = new MessageDto();
        messageDto.setTo(email);
        messageDto.setFrom("vibhassrinet@gmail.com");
        messageDto.setSubject("Welcome to the platform");
        messageDto.setBody("Hope you liked the platform");
        try{
            kafkaProducerClient.sendMessage("signup",objectMapper.writeValueAsString(messageDto));
        } catch ( JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
         return user;
    }

    @Override
    public Pair<User,MultiValueMap<String,String>> login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();

        if(!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return null;
        }


        //Generating Token
        //Token Generation
//        String message = "{\n" +
//                "   \"email\": \"anurag@scaler.com\",\n" +
//                "   \"roles\": [\n" +
//                "      \"instructor\",\n" +
//                "      \"buddy\"\n" +
//                "   ],\n" +
//                "   \"expirationDate\": \"2ndJuly2024\"\n" +
//                "}";

        //byte[] content = message.getBytes(StandardCharsets.UTF_8);
        //String token = Jwts.builder().content(content).compact();

        Map<String,Object> jwtData = new HashMap<>();
        jwtData.put("roles",user.getRoles().stream().map(role->"ROLE_"+role.getValue()).toArray());
        Long nowInMillis = System.currentTimeMillis();
        jwtData.put("iat",nowInMillis);
        jwtData.put("exp", nowInMillis+1000000);

        //String token = Jwts.builder().content(content).signWith(secretKey).compact();
        String token = Jwts.builder().setSubject(user.getEmail()).claims(jwtData).signWith(secretKey).compact();

        Session session = new Session();
        session.setUser(user);
        session.setToken(token);
        session.setSessionStatus(SessionStatus.ACTIVE);
        sessionRepository.save(session);

        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.SET_COOKIE,token);

        return new Pair<User,MultiValueMap<String,String>>(user,headers);
    }

    public Boolean validateToken(String token,Long userId) {
        Optional<Session> optionalSession = sessionRepository.findByTokenEquals(token);
        if(optionalSession.isEmpty()) {
            return false;
        }

        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        // Store the encodedKey securely
        System.out.println("Encoded Secret Key: " + encodedKey);


        JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

        Long expiryInEpoch = (Long)claims.get("exp");
        Long currentTime = System.currentTimeMillis();
        System.out.println("current Time" +currentTime);
        System.out.println("token expiry"+expiryInEpoch);

        if(currentTime > expiryInEpoch) {
            //Add Logic for turning session status as expired and persist in DB
            return false;
        }

        return true;
    }
}