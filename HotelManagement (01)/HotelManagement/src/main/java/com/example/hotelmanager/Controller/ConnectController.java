package com.example.hotelmanager.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hotelmanager.service.EmailService;

@Controller
public class ConnectController {
	@Autowired
	private EmailService emailService;
	
	@GetMapping("/connect")
    public String showConnectPage() {
        return "/pages/connect_page"; 
    }
	
	@PostMapping("/connect")
	public String submitConnectForm(@RequestParam String hotelName, 
	                                  
	                                 @RequestParam String phone, 
	                                 @RequestParam String email, 
	                                 @RequestParam String ideas, 
	                                 @RequestParam boolean nda) {

	    emailService.sendConnectEmail(hotelName, phone, email, ideas, nda);

	    return "pages/connect_page_success"; 
	}
}
