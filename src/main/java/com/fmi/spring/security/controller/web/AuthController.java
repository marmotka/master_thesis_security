package com.fmi.spring.security.controller.web;

import com.fmi.spring.security.dto.LoginRequest;
import com.fmi.spring.security.dto.RegisterRequest;
import com.fmi.spring.security.exception.DuplicateUserException;
import com.fmi.spring.security.model.User;
import com.fmi.spring.security.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid RegisterRequest request,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        User savedUser;
        try {
            savedUser = userService.register(request);
        } catch (DuplicateUserException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/login?registered=true";
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "registered", required = false) String registered,
                                Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        if (registered != null) {
            model.addAttribute("message", "Registration successful! Please log in.");
        }

        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    // ---------- Home ----------
    @GetMapping("/home")
    public String showHomePage(Model model) {
        // Optional: Get the logged-in user
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        model.addAttribute("username", username);
        return "home";
    }
}

