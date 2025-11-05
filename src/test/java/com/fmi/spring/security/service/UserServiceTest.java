//package com.fmi.spring.security.service;
//
//import com.fmi.spring.security.dto.RegisterRequest;
//import com.fmi.spring.security.model.Role;
//import com.fmi.spring.security.model.User;
//import com.fmi.spring.security.repository.UserRepository;
//import com.fmi.spring.security.service.UserService;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for UserService (password/username changes).
// * Uses Mockito to isolate repository + encoder.
// */
//@ExtendWith(MockitoExtension.class)
//class UserServiceTest {
//
//    @Mock private UserRepository userRepository;
//    @Mock private PasswordEncoder passwordEncoder;
//
//    @InjectMocks
//    private UserService userService;
//
//    private User existingUser;
//
//    @BeforeEach
//    void setUp() {
//        existingUser = new User();
//        existingUser.setId(1L);
//        existingUser.setUsername("alice");
//        existingUser.setEmail("alice@example.com");
//        existingUser.setPassword("ENC_OLD"); // encoded
//        existingUser.setRole(Role.USER);
//
//        // Put an Authentication into the SecurityContext (typical when user is logged in)
//        var auth = new UsernamePasswordAuthenticationToken(
//                "alice",
//                existingUser.getPassword(),
//                List.of(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//        SecurityContext context = SecurityContextHolder.createEmptyContext();
//        context.setAuthentication(auth);
//        SecurityContextHolder.setContext(context);
//    }
//
//    @AfterEach
//    void tearDown() {
//        SecurityContextHolder.clearContext();
//    }
//
//    // ---------------- changePassword ----------------
//
//    @Test
//    void changePassword_success_updatesEncodedPasswordAndSaves() {
//        when(userRepository.findByUsername("alice"))
//                .thenReturn(Optional.of(existingUser));
//        when(passwordEncoder.matches("oldpass", "ENC_OLD"))
//                .thenReturn(true);
//        when(passwordEncoder.encode("newPass123!"))
//                .thenReturn("ENC_NEW");
//
//        userService.changePassword("alice", "oldpass", "newPass123!");
//
//        assertEquals("ENC_NEW", existingUser.getPassword());
//        verify(userRepository, times(1)).save(existingUser);
//    }
//
//    @Test
//    void changePassword_wrongCurrent_throws() {
//        when(userRepository.findByUsername("alice"))
//                .thenReturn(Optional.of(existingUser));
//        when(passwordEncoder.matches("wrong", "ENC_OLD"))
//                .thenReturn(false);
//
//        var ex = assertThrows(IllegalArgumentException.class,
//                () -> userService.changePassword("alice", "wrong", "whatever123"));
//        assertTrue(ex.getMessage().toLowerCase().contains("current password"));
//
//        verify(userRepository, never()).save(any());
//    }
//
//    @Test
//    void changePassword_userNotFound_throws() {
//        when(userRepository.findByUsername("alice"))
//                .thenReturn(Optional.empty());
//
//        assertThrows(UsernameNotFoundException.class,
//                () -> userService.changePassword("alice", "oldpass", "newPass123!"));
//        verify(userRepository, never()).save(any());
//    }
//
//    // ---------------- changeUsername ----------------
//
//    @Test
//    void changeUsername_success_updatesRepo_andRefreshesSecurityContext() {
//        // Arrange current user
//        when(userRepository.findByUsername("alice"))
//                .thenReturn(Optional.of(existingUser));
//        when(passwordEncoder.matches("correct", "ENC_OLD"))
//                .thenReturn(true);
//        when(userRepository.existsByUsername("bob"))
//                .thenReturn(false);
//
//        // After change, loadUserByUsername("bob") will be called by refreshAuthentication()
//        User updated = new User();
//        updated.setId(existingUser.getId());
//        updated.setUsername("bob");
//        updated.setEmail(existingUser.getEmail());
//        updated.setPassword(existingUser.getPassword());
//        updated.setRole(Role.USER);
//
//        // loadUserByUsername → userRepository.findByUsername("bob")
//        when(userRepository.findByUsername("bob"))
//                .thenReturn(Optional.of(updated));
//
//        // Act
//        userService.changeUsername("alice", "bob", "correct");
//
//        // Assert persistent change
//        assertEquals("bob", existingUser.getUsername());
//        verify(userRepository, times(1)).save(existingUser);
//
//        // Assert SecurityContext is refreshed
//        var auth = SecurityContextHolder.getContext().getAuthentication();
//        assertNotNull(auth);
//        assertEquals("bob", auth.getName()); // now the header will show the new username
//    }
//
//    @Test
//    void changeUsername_usernameTaken_throws() {
//        when(userRepository.findByUsername("alice"))
//                .thenReturn(Optional.of(existingUser));
//        when(passwordEncoder.matches("correct", "ENC_OLD"))
//                .thenReturn(true);
//        when(userRepository.existsByUsername("bob"))
//                .thenReturn(true);
//
//        var ex = assertThrows(IllegalArgumentException.class,
//                () -> userService.changeUsername("alice", "bob", "correct"));
//        assertTrue(ex.getMessage().toLowerCase().contains("taken"));
//
//        verify(userRepository, never()).save(any());
//        // SecurityContext unchanged
//        assertEquals("alice", SecurityContextHolder.getContext().getAuthentication().getName());
//    }
//
//    @Test
//    void changeUsername_wrongCurrentPassword_throws() {
//        when(userRepository.findByUsername("alice"))
//                .thenReturn(Optional.of(existingUser));
//        when(passwordEncoder.matches("wrong", "ENC_OLD"))
//                .thenReturn(false);
//
//        var ex = assertThrows(IllegalArgumentException.class,
//                () -> userService.changeUsername("alice", "bob", "wrong"));
//        assertTrue(ex.getMessage().toLowerCase().contains("current password"));
//
//        verify(userRepository, never()).save(any());
//        assertEquals("alice", SecurityContextHolder.getContext().getAuthentication().getName());
//    }
//}
