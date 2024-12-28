package com.example.hotelmanager.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
    @GetMapping("/error/accessDenied")
    public String accessDenied() {
        return "error/403"; 
    }
}

