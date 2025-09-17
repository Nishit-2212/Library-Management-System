package com.library.management.controller;

import com.library.management.dto.RegistrationDto;
import com.library.management.service.RegistrationService;
import com.library.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@ModelAttribute RegistrationDto dto,
                               Model model,
                               RedirectAttributes flash) {
        try {
            registrationService.registerUser(dto);
            flash.addFlashAttribute("success", "Sign up completed");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

}
