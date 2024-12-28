package com.example.hotelmanager.Controller;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hotelmanager.model.User;
import com.example.hotelmanager.service.UserService;


@Controller
@RequestMapping("/admin/users")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@GetMapping
	public String listUser(Model model)	{
		model.addAttribute("users", userService.getAllUsers());
		model.addAttribute("user", new User());
		model.addAttribute("roles", Arrays.asList("Guest", "Staff"));
		
		return "admin/user/users";
	}
	
	@GetMapping("/edit/{id}")
	public String editUser(@PathVariable Long id, Model model)	{
		model.addAttribute("user", userService.getUserById(id));
		model.addAttribute("roles", Arrays.asList("Guest", "Staff"));
		return "admin/user/user_form";
	}
	
	@PostMapping("/save")
	public String saveUser(@ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
	    if (bindingResult.hasErrors()) {
	        model.addAttribute("roles", Arrays.asList("Guest", "Staff"));
	        return "admin/user/user_form";
	    }
	    try {
	        userService.saveUser(user);
	        return "redirect:/admin/users";
	    } catch (IllegalArgumentException e) {
	        model.addAttribute("errorMessage", e.getMessage());
	        model.addAttribute("roles", Arrays.asList("Guest", "Staff"));
	        return "admin/user/user_form";
	    }
	}
	
	
	@PostMapping("/toggle-status/{id}")
	public String toggleUserStatus(@PathVariable Long id, RedirectAttributes attributes) {
	    try {
	        userService.toggleStatus(id);
	        attributes.addFlashAttribute("successMessage", "Trạng thái người dùng đã được cập nhật thành công.");
	    } catch (Exception e) {
	        attributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật trạng thái người dùng: " + e.getMessage());
	    }
	    return "redirect:/admin/users";
	}

	

}
