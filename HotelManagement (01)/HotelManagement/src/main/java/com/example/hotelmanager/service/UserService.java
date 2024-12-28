package com.example.hotelmanager.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hotelmanager.exception.AuthenticationException;
import com.example.hotelmanager.exception.ResourceNotFoundException;
import com.example.hotelmanager.model.User;
import com.example.hotelmanager.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String username, String password) {
        log.info("Attempting login for username: {}", username);

        // Tìm user theo username
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new AuthenticationException("Tên đăng nhập hoặc mật khẩu không đúng"));

        // Kiểm tra trạng thái tài khoản
        if (!user.isStatus()) { // Kiểm tra nếu tài khoản bị vô hiệu hóa
            log.warn("User {} login attempt failed - account disabled", username);
            throw new AuthenticationException("Tài khoản bị vô hiệu hóa");
        }


        // Kiểm tra mật khẩu
        if (!user.getPassword().equals(password)) {
            log.warn("Invalid password for user: {}", username);
            throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        log.info("Login successful for user: {}", username);
        return user;
    }
    
    public void toggleStatus(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại với ID: " + id));

        // Đổi trạng thái
        boolean newStatus = !user.isStatus();
        user.setStatus(newStatus);
        userRepository.save(user);

        log.info("User ID {} status changed to {}", id, newStatus);
    }

    
    public User registerUser(User user) {
        log.info("Registering new user with username: {}", user.getUsername());
        
        validateNewUser(user);
        
        // Set role mặc định nếu chưa có
        if (user.getRole() == null) {
            user.setRole("Guest");
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully registered user: {}", user.getUsername());
        return savedUser;
    }

    public User updateUser(User user) {
        log.info("Updating user with ID: {}", user.getId());
        
        User existingUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + user.getId()));

        validateExistingUser(user, existingUser);

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated user: {}", user.getUsername());
        return updatedUser;
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("Successfully deleted user with ID: {}", id);
    }

    private void validateNewUser(User user) {
        log.debug("Validating new user: {}", user.getUsername());
        
        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("Username already exists: {}", user.getUsername());
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email đã tồn tại");
        }
    }

    private void validateExistingUser(User newUser, User existingUser) {
        log.debug("Validating existing user update: {}", newUser.getUsername());
        
        // Kiểm tra username
        if (!existingUser.getUsername().equals(newUser.getUsername()) && 
            userRepository.existsByUsername(newUser.getUsername())) {
            log.warn("Username already exists: {}", newUser.getUsername());
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        // Kiểm tra email
        if (!existingUser.getEmail().equals(newUser.getEmail()) && 
            userRepository.existsByEmail(newUser.getEmail())) {
            log.warn("Email already exists: {}", newUser.getEmail());
            throw new IllegalArgumentException("Email đã tồn tại");
        }
    }

    public User saveUser(User user) {
    	if (user.getId() != null)	{
    		User existingUser = userRepository.findById(user.getId())
    					.orElseThrow(() -> new IllegalArgumentException("User not found"));
    		validateExistingUser(user, existingUser);
    	} else {
    		if (userRepository.existsByUsername(user.getUsername()))	{
                throw new IllegalArgumentException("Username đã tồn tại");
    		}
    		if (userRepository.existsByEmail(user.getEmail()))	{
                throw new IllegalArgumentException("Email đã tồn tại");
    		}
    	}
    	return userRepository.save(user);
    }
    // Các phương thức truy vấn
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Lưu người dùng
    public void saveUser1(User user) {
        userRepository.save(user);
    }
	public Optional<User> findByVerificationToken(String token) {
		return userRepository.findByVerificationToken(token);
	}
	public Optional<User> findByResetToken(String resetToken) {
        return userRepository.findByResetToken(resetToken);
    }
    
}