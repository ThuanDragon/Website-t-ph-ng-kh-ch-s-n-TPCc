package com.example.hotelmanager.Controller; // Chỉnh sửa tên gói cho đúng quy tắc

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.hotelmanager.exception.AuthenticationException;

import com.example.hotelmanager.model.User;
import com.example.hotelmanager.service.EmailService;
import com.example.hotelmanager.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class AuthController {
    private static final String ADMIN_ROLE = "Admin";
    private static final String STAFF_ROLE = "Staff";
    private static final String GUEST_ROLE = "Guest";
    
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    private EmailService emailService;


    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        log.info("Login attempt for username: {}", username);
        try {
            User user = userService.login(username, password);
            log.info("Login successful for user: {}", username);
            
            if (!user.isVerified()) {
                log.warn("Login failed for username: {} - Account not activated", username);
                model.addAttribute("errorMessage", "Tài khoản của bạn chưa được kích hoạt. Vui lòng kiểm tra email.");
                return "layout/login";
            }
            
            // Lưu thông tin user đã đăng nhập
            session.setAttribute("loggedInUser", user);

            String redirectUrl = (String) session.getAttribute("redirectUrl");
            if (redirectUrl != null && !redirectUrl.contains("hotel3.jpg") && !redirectUrl.contains("error")) {
                session.removeAttribute("redirectUrl");
                return "redirect:" + redirectUrl;
            }

            return "redirect:/";
        } catch (AuthenticationException e) {
            log.error("Login failed for username: {} - Reason: {}", username, e.getMessage());
            model.addAttribute("errorMessage", e.getMessage()); // Hiển thị thông báo cụ thể
            return "layout/login";
        } catch (RuntimeException e) {
            log.error("Login failed for username: {} with error: {}", username, e.getMessage());
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không đúng");
            return "layout/login";
        }
    }



    @GetMapping("/logout")
    public String logout(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            log.info("Logging out user: {}", loggedInUser.getUsername());
        }
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "layout/login";
    }

    @GetMapping("/signup")
    public String getSignupPage() {
        return "layout/signup";
    }

    @PostMapping("/signup")
    public String register(@ModelAttribute User user, Model model,
                           @RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword) {

        // Kiểm tra mật khẩu và xác nhận mật khẩu có khớp không
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu và xác nhận mật khẩu không khớp");
            return "layout/signup";
        }

        try {
            // Tạo token xác nhận ngẫu nhiên
            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationToken(verificationToken);

            // Đặt trạng thái là 0 (chưa kích hoạt)
            user.setVerified(false);

            // Đăng ký người dùng vào cơ sở dữ liệu
            userService.registerUser(user);

            // Tạo liên kết xác nhận tài khoản
            String confirmationLink = "http://localhost:8080/confirm?token=" + verificationToken;

            // Cấu hình nội dung email
            String subject = "Xác nhận đăng ký tài khoản";
            String body = "<p>Chào " + user.getUsername() + ",</p>"
                        + "<p>Vui lòng nhấn vào liên kết dưới đây để xác nhận tài khoản của bạn:</p>"
                        + "<a href=\"" + confirmationLink + "\">Xác nhận tài khoản</a>";

            // Gửi email xác nhận
            emailService.sendConfirmationEmail(user.getEmail(), subject, body);

            // Thông báo cho người dùng
            model.addAttribute("successMessage", "Đăng ký thành công! Vui lòng kiểm tra email để xác nhận tài khoản.");
            return "layout/signup";

        } catch (IllegalArgumentException | MessagingException e) {
            // Xử lý lỗi nếu có
            model.addAttribute("errorMessage", e.getMessage());
            return "layout/signup";
        }
    }

    @GetMapping("/confirm")
    public String confirmAccount(@RequestParam("token") String token, Model model) {
        Optional<User> userOptional = userService.findByVerificationToken(token);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.isVerified()) {
                model.addAttribute("message", "Tài khoản đã được kích hoạt trước đó! Bạn có thể đăng nhập.");
                return "layout/login";
            }

            // Cập nhật trạng thái tài khoản
            user.setVerified(true);  // Đánh dấu đã xác nhận
            user.setStatus(true);    // Kích hoạt tài khoản
            user.setVerificationToken(null); // Xóa token sau khi sử dụng
            userService.saveUser(user);      // Lưu lại thông tin vào database

            model.addAttribute("message", "Xác nhận tài khoản thành công! Bạn có thể đăng nhập.");
            return "layout/login";
        } else {
            model.addAttribute("errorMessage", "Token không hợp lệ hoặc đã hết hạn.");
            return "layout/signup";
        }

    }



	
	   
}

