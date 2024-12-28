package com.example.hotelmanager.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hotelmanager.exception.ResourceNotFoundException;
import com.example.hotelmanager.exception.RoomAlreadyBookedException;
import com.example.hotelmanager.model.Booking;
import com.example.hotelmanager.model.Room;
import com.example.hotelmanager.model.RoomType;
import com.example.hotelmanager.model.User;
import com.example.hotelmanager.service.BookingService;
import com.example.hotelmanager.service.EmailService;
import com.example.hotelmanager.service.RoomService;
import com.example.hotelmanager.service.RoomTypeService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class BookingController {
    private final BookingService bookingService;
    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final EmailService emailService;
    
    @Autowired
    public BookingController(
            BookingService bookingService, 
            RoomService roomService,
            RoomTypeService roomTypeService,
            EmailService emailService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;
        this.emailService = emailService;
    }
	
    private User getLoggedInUserOrRedirect(HttpSession session, String redirectUrl) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            session.setAttribute("redirectUrl", redirectUrl);
            return null;
        }
        return loggedInUser;
    }
    @PostMapping("/remove-error-message")
    public ResponseEntity<Void> removeErrorMessage(HttpSession session) {
        session.removeAttribute("errorMessage");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/remove-success-message")
    public ResponseEntity<Void> removeSuccessMessage(HttpSession session) {
        session.removeAttribute("successMessage");
        return ResponseEntity.ok().build();
    }
	//xuly dat phong
    @PostMapping("/booking/create")
    public String createBooking(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam Integer guests,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = getLoggedInUserOrRedirect(session, "/rooms/" + roomId);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            Booking booking = createBookingRequest(roomId, checkIn, checkOut, guests, loggedInUser);
            booking = bookingService.createBooking(booking);
            emailService.sendBookingConfirmationEmail(booking);
            
            session.setAttribute("successMessage", "Đặt phòng thành công!");
            return "redirect:/booking/confirmation/" + booking.getId();

        } catch (RoomAlreadyBookedException | IllegalArgumentException | ResourceNotFoundException e) {
            // Sử dụng session attribute cho error message
            session.setAttribute("errorMessage", e.getMessage());
            return "redirect:/rooms/room/" + roomId;
        } catch (Exception e) {
            log.error("Error creating booking: {}", e.getMessage(), e);
            session.setAttribute("errorMessage", 
                "Có lỗi xảy ra trong quá trình đặt phòng. Vui lòng thử lại sau.");
            return "redirect:/rooms/room/" + roomId;
        }
    }

    private Booking createBookingRequest(Long roomId, LocalDate checkIn, LocalDate checkOut, 
                                       Integer guests, User user) {
        Room room = roomService.getRoomById(roomId);
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setRoomType(room.getRoomType());
        booking.setUser(user);
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkOut);
        booking.setGuests(guests);
        return booking;
    }

	
	@GetMapping("/booking/confirmation/{bookingId}")
	public String getBookingConfirmation(@PathVariable Long bookingId, 
	                                   HttpSession session, 
	                                   Model model) {
	    User loggedInUser = (User) session.getAttribute("loggedInUser");
	    if (loggedInUser == null) {
	        return "redirect:/login";
	    }

	    try {
	        Booking booking = bookingService.getBookingById(bookingId);
	        
	        // So sánh ID của người dùng
	        if (!booking.getUser().getId().equals(loggedInUser.getId())) {
	            return "error/accessDenied";
	        }
	        
	        User user = booking.getUser(); 
	        model.addAttribute("user", user); 
	        
	        Room room = roomService.getRoomById(booking.getRoom().getId());
	        RoomType roomType = roomTypeService.findById(room.getRoomType().getId())
	                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng"));
	        
	        // Tính toán totalPrice
	        long soNgayLuuTru = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
	        BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(soNgayLuuTru));
	        model.addAttribute("totalPrice", totalPrice); // Thêm totalPrice vào model
	        
	        model.addAttribute("booking", booking);
	        model.addAttribute("room", room);
	        model.addAttribute("roomType", roomType);
	        return "booking/confirmation";

	    } catch (ResourceNotFoundException e) {
	        model.addAttribute("errorMessage", "Không tìm thấy đặt phòng hoặc phòng.");
	        return "error/notFound";
	    } catch (Exception e) {
	        model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình xử lý.");
	        return "error/generalError";
	    }
	}


    // Xem lịch sử đặt phòng
    @GetMapping("/booking/history")
    public String getBookingHistory(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingService.getBookingsByUserId(loggedInUser.getId());
        for (Booking booking : bookings) {
            Room room = roomService.getRoomById(booking.getRoom().getId());
            booking.setRoom(room); 
        }
        model.addAttribute("bookings", bookings);
        return "booking/history";
    }
}
