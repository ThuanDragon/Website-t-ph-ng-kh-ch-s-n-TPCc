package com.example.hotelmanager.Controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hotelmanager.model.User;
import com.example.hotelmanager.service.EmailService;
import com.example.hotelmanager.service.UserService;

import jakarta.mail.MessagingException;


@Controller
public class PasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // Hiển thị giao diện quên mật khẩu
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "layout/forgot-password"; // Hiển thị giao diện nhập email
    }

    // Xử lý yêu cầu quên mật khẩu
    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email, Model model) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            model.addAttribute("errorMessage", "Email không tồn tại trong hệ thống.");
            return "layout/forgot-password";
        }

        User user = userOptional.get();
        String resetToken = UUID.randomUUID().toString(); // Tạo token ngẫu nhiên
        user.setResetToken(resetToken); // Lưu token vào user
        user.setResetTokenExpiration(LocalDateTime.now().plusHours(1)); // Hạn sử dụng token là 1 giờ
        userService.saveUser(user);

        // Tạo liên kết đặt lại mật khẩu với token
        String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;

        // Nội dung email
        String subject = "Đặt lại mật khẩu";
        String body = "<p>Chào " + user.getUsername() + ",</p>"
                    + "<p>Vui lòng nhấn vào liên kết dưới đây để đặt lại mật khẩu của bạn:</p>"
                    + "<a href=\"" + resetLink + "\">Đặt lại mật khẩu</a>";

        try {
            emailService.sendConfirmationEmail(user.getEmail(), subject, body); // Gửi email với liên kết reset mật khẩu
            model.addAttribute("successMessage", "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư.");
        } catch (MessagingException e) {
            model.addAttribute("errorMessage", "Lỗi gửi email. Vui lòng thử lại.");
        }
        return "layout/forgot-password";
    }

    // Hiển thị trang nhập mật khẩu mới
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        Optional<User> userOptional = userService.findByResetToken(token);
        if (userOptional.isEmpty()) {
            model.addAttribute("errorMessage", "Token không hợp lệ hoặc đã hết hạn.");
            return "layout/forgot-password";
        }

        User user = userOptional.get();
        // Kiểm tra token có hết hạn không
        if (user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            model.addAttribute("errorMessage", "Token đã hết hạn.");
            return "layout/forgot-password";
        }

        model.addAttribute("token", token); // Truyền token cho giao diện đặt lại mật khẩu
        return "layout/reset-password";
    }

    // Xử lý đặt lại mật khẩu
    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam String newPassword,
                                      @RequestParam String confirmPassword,
                                      Model model) {
        // Kiểm tra mật khẩu và xác nhận mật khẩu có khớp không
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu và xác nhận mật khẩu không khớp.");
            model.addAttribute("token", token);
            return "layout/reset-password";
        }

        Optional<User> userOptional = userService.findByResetToken(token);
        if (userOptional.isEmpty()) {
            model.addAttribute("errorMessage", "Token không hợp lệ hoặc đã hết hạn.");
            return "layout/forgot-password";
        }

        User user = userOptional.get();
        // Kiểm tra xem token có hết hạn không
        if (user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            model.addAttribute("errorMessage", "Token đã hết hạn.");
            return "layout/forgot-password";
        }

        // Mã hóa mật khẩu trước khi lưu (ví dụ: sử dụng BCrypt)
        user.setPassword(newPassword);
        user.setResetToken(null); // Xóa token sau khi đặt lại mật khẩu
        user.setResetTokenExpiration(null); // Xóa thời gian hết hạn token
        userService.saveUser(user);

        model.addAttribute("successMessage", "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập.");
        return "layout/login"; // Chuyển hướng đến trang đăng nhập
    }
}
