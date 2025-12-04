package com.oops.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    
    @GetMapping("/test-thymeleaf")
    public String testThymeleaf(Model model) {
        System.out.println("TEST THYMELEAF ENDPOINT CALLED");
        model.addAttribute("testMessage", "Hello Thymeleaf!");
        model.addAttribute("testNumber", 42);
        return "test-template";
    }
}