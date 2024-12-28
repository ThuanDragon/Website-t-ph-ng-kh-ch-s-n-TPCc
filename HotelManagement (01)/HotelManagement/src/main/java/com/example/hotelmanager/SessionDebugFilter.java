package com.example.hotelmanager;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.example.hotelmanager.model.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SessionDebugFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, 
                        ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(false);
        
        if (session != null) {
            log.debug("Request URI: {}", httpRequest.getRequestURI());
            log.debug("Session ID: {}", session.getId());
            
            User user = (User) session.getAttribute("loggedInUser");
            if (user != null) {
                log.debug("Logged in user in session: {}", user.getUsername());
            } else {
                log.debug("No user in session");
            }
        }
        
        chain.doFilter(request, response);
    }
}