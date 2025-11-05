package com.fmi.spring.security.controller.web;

import com.fmi.spring.security.dto.UpdatePasswordRequest;
import com.fmi.spring.security.model.User;
import com.fmi.spring.security.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/view")
    public String showProfile(Model model, Authentication auth) {
        String currentUsername = auth.getName();

        var userOpt = userService.findByUsername(currentUsername);
        String email = userOpt.map(User::getEmail).orElse("");

        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("currentEmail", email);

        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new UpdatePasswordRequest());
        }
        return "profile";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") UpdatePasswordRequest form,
                                 BindingResult result,
                                 RedirectAttributes ra,
                                 Authentication auth) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.passwordForm", result);
            ra.addFlashAttribute("passwordForm", form);
            ra.addFlashAttribute("errorMessage", "Please correct the highlighted errors.");
            ra.addFlashAttribute("openPassword", true);
            return "redirect:/profile/view";
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            ra.addFlashAttribute("passwordForm", form);
            ra.addFlashAttribute("errorMessage", "New password and confirmation do not match.");
            ra.addFlashAttribute("openPassword", true);
            return "redirect:/profile/view";
        }

        try {
            userService.changePassword(auth.getName(), form.getCurrentPassword(), form.getNewPassword());
            ra.addFlashAttribute("successMessage", "Password updated successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("passwordForm", form);
            ra.addFlashAttribute("errorMessage", e.getMessage());
            ra.addFlashAttribute("openPassword", true);
        }
        return "redirect:/profile/view";
    }
}
