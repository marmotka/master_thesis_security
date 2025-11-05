package com.fmi.spring.security.controller.web;

import com.fmi.spring.security.model.User;
import com.fmi.spring.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public String listUsers(Model model, Authentication auth) {
        List<User> users = userService.findAllUsers(); // add this in service
        model.addAttribute("users", users);
        model.addAttribute("currentUsername", auth.getName());
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             Authentication auth,
                             RedirectAttributes ra) {
        try {
            // Optional: prevent deleting yourself by id/username
            var target = userService.findById(id)
                    .orElse(null);

            if (target == null) {
                ra.addFlashAttribute("errorMessage", "User not found.");
                return "redirect:/admin/users";
            }
            if (target.getUsername().equals(auth.getName())) {
                ra.addFlashAttribute("errorMessage", "You cannot delete your own account.");
                return "redirect:/admin/users";
            }

            userService.deleteUserById(id);
            ra.addFlashAttribute("successMessage", "User deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
