
package com.example.hotelmanager.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hotelmanager.model.User;
import com.example.hotelmanager.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/detail")
    public String showAccountDetail(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login"; 
        }
        
        model.addAttribute("user", user);
        return "account/detail";
    }

    // Xử lý cập nhật thông tin
    @PostMapping("/update") 
    public String updateAccount(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            HttpSession session,
            RedirectAttributes attributes) {

        try {
            User user = (User) session.getAttribute("loggedInUser");
            if (user == null) {
                return "redirect:/login"; 
            }

            
            if (newPassword != null && !newPassword.isEmpty()) {
                if (!currentPassword.equals(user.getPassword())) {
                    attributes.addFlashAttribute("error", "Current password is incorrect");
                    return "redirect:/account/detail";
                }
                user.setPassword(newPassword);
            }

            user.setUsername(username);
            user.setEmail(email);
            userRepository.save(user); 
            attributes.addFlashAttribute("success", "Profile updated successfully");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:/";
    }
}
