package com.example.hotelmanager;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.example.hotelmanager.model.User;

@Component
public class RoleInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String requestURI = request.getRequestURI();
        
        // Cho phép truy cập trang chủ, các trang phòng, chi tiết phòng và trang đặt lại mật khẩu/xác thực mã mà không cần đăng nhập
        if (requestURI.equals("/") 
            || requestURI.startsWith("/rooms") 
            || requestURI.matches("/room/\\d+") 
            || requestURI.matches("/forgot-password") 
            || requestURI.matches("/verify-code") 
            || requestURI.matches("/reset-password")) {
            return true; // Cho phép yêu cầu tiếp tục
        }
        
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (loggedInUser == null) {
            // Lưu lại URL hiện tại để có thể chuyển hướng sau khi đăng nhập
            session.setAttribute("redirectUrl", request.getRequestURI());
            response.sendRedirect("/login"); // Chuyển hướng đến trang đăng nhập
            return false; // Ngừng yêu cầu tiếp tục
        }

        // Kiểm tra quyền truy cập theo vai trò (ví dụ đối với Admin)
        if (requestURI.startsWith("/api/admin") && !"Admin".equals(loggedInUser.getRole())) {
            response.sendRedirect("/error/accessDenied"); // Chuyển hướng đến trang thông báo không có quyền
            return false; // Ngừng yêu cầu tiếp tục
        }

        return true; // Cho phép yêu cầu tiếp tục
    }
}
