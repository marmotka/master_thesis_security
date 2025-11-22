package com.fmi.spring.security.controller.web;

import com.fmi.spring.security.dto.LoginRequest;
import com.fmi.spring.security.dto.RegisterRequest;
import com.fmi.spring.security.exception.DuplicateUserException;
import com.fmi.spring.security.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping
public class AuthController {

    public static final String REGISTER = "register";
    public static final String REQUEST = "request";
    public static final String MESSAGE = "message";
    public static final String ERROR = "error";
    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute(REQUEST, new RegisterRequest());
        return REGISTER;
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute(REQUEST) @Valid RegisterRequest request,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return REGISTER;
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match.");
            return REGISTER;
        }

        try {
            userService.register(request);
        } catch (DuplicateUserException e) {
            result.rejectValue(e.getField(), "duplicate", e.getMessage());
            return REGISTER;
        }

        return "redirect:/login?registered=true";
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = ERROR, required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "registered", required = false) String registered,
                                Model model) {

        if (error != null) {
            log.error(error);
            model.addAttribute(ERROR, "Invalid username or password");
        }
        if (logout != null) {
            log.error(error);
            model.addAttribute(MESSAGE, "You have been logged out successfully");
        }
        if (registered != null && registered.equals("true")) {
            model.addAttribute(MESSAGE, "Registration successful! Please log in.");
        }

        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @GetMapping("/home")
    public String showHomePage(Model model) {
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        model.addAttribute("username", username);
        return "home";
    }
}

