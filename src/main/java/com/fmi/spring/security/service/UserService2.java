//package com.fmi.spring.security.service;
//
//import com.fmi.spring.security.dto.LoginRequest;
//import com.fmi.spring.security.dto.RegisterRequest;
//import com.fmi.spring.security.exception.DuplicateUserException;
//import com.fmi.spring.security.model.Role;
//import com.fmi.spring.security.model.User;
//import com.fmi.spring.security.repository.UserRepository;
//import com.fmi.spring.security.security.JwtService;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Log4j2
//@Service
//public class UserService2 {
//
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//    @Autowired
//    private JwtService jwtService;
//
//
//    public User register(RegisterRequest request) throws DuplicateUserException {
//        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
//        if (existingUser.isPresent()) {
//            throw new DuplicateUserException("User with that email already exists");
//        }
//
//        User user = new User();
//        user.setUsername(request.getUsername());
//        user.setEmail(request.getEmail());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRole(Role.USER);
//
//        User registeredUser = userRepository.save(user);
//        log.info("Registered user with id {}", registeredUser.getId());
//        return registeredUser;
//    }
//
//    public Optional<User> authenticate(LoginRequest request) {
//        return userRepository.findByUsername(request.getUsername())
//                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()));
//    }
//
//    public Optional<User> findByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }
//
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }
//
////    public ResponseEntity<?> authenticate(LoginRequest request) {
////        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
////        if (userOpt.isEmpty()) {
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
////        }
////
////        User user = userOpt.get();
////        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
////        }
////
////        UserDetails userDetails = org.springframework.security.core.userdetails.User
////                .withUsername(user.getUsername())
////                .password(user.getPassword())
////                .roles(user.getRole().name())
////                .build();
////
////        String token;
////        try {
////            token = jwtUtils.generateToken(userDetails);
////        } catch (ParseException e) {
////            return ResponseEntity.internalServerError().build();
////        }
////        return ResponseEntity.ok(new LoginResponse(token));
////    }
//
//
//
////    public User getUserByUsername(String username) {
////        return userRepository.findByUsername(username).orElseThrow();
////    }
//}
