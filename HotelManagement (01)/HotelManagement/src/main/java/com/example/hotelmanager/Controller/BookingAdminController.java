package com.example.hotelmanager.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hotelmanager.model.Booking;
import com.example.hotelmanager.model.Room;
import com.example.hotelmanager.model.RoomType;
import com.example.hotelmanager.service.BookingService;
import com.example.hotelmanager.service.RoomTypeService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/bookings")
@Slf4j
public class BookingAdminController {

    private final BookingService bookingService;
    private final RoomTypeService roomTypeService;

    public BookingAdminController(BookingService bookingService, RoomTypeService roomTypeService) {
        this.bookingService = bookingService;
        this.roomTypeService = roomTypeService;
    }

    @GetMapping
    public String showBookingsPage(Model model) {
        List<Booking> confirmedBookings = bookingService.getConfirmedBookings();
        List<Booking> pendingBookings = bookingService.getPendingBookings();

        int totalBookings = confirmedBookings.size() + pendingBookings.size();
        BigDecimal totalRevenue = confirmedBookings.stream()
                .map(this::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalConfirmed", confirmedBookings.size());
        model.addAttribute("totalPending", pendingBookings.size());
        model.addAttribute("totalRevenue", totalRevenue);

        log.info("Displaying all bookings. Total: {}, Confirmed: {}, Pending: {}, Revenue: {}",
                totalBookings, confirmedBookings.size(), pendingBookings.size(), totalRevenue);
        return "admin/booking/bookings";
    }


    @GetMapping("/view/{id}")
    public String viewBookingDetails(@PathVariable Long id, Model model) {
        Booking booking = bookingService.getBookingById(id);

        if (booking == null) {
            log.warn("Booking with ID {} not found", id);
            model.addAttribute("errorMessage", "Booking not found");
            return "error/404";
        }

        BigDecimal totalPrice = calculateTotalPrice(booking);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("booking", booking);
        model.addAttribute("user", booking.getUser());
        model.addAttribute("room", booking.getRoom());

        log.info("Viewing details for booking ID: {}", id);
        return "admin/booking/booking-details";
    }

    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            bookingService.cancelBooking(id);
            log.info("Cancelled booking with ID: {}", id);
            attributes.addFlashAttribute("success", "Booking cancelled successfully.");
        } catch (Exception e) {
            log.error("Error cancelling booking with ID {}: {}", id, e.getMessage());
            attributes.addFlashAttribute("error", "Failed to cancel booking.");
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/accept/{id}")
    public String confirmBooking(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            bookingService.acceptBooking(id);
            log.info("Confirmed booking with ID: {}", id);
            attributes.addFlashAttribute("success", "Booking confirmed successfully.");
        } catch (Exception e) {
            log.error("Error confirming booking with ID {}: {}", id, e.getMessage());
            attributes.addFlashAttribute("error", "Failed to confirm booking.");
        }
        return "redirect:/admin/bookings";
    }

    private BigDecimal calculateTotalPrice(Booking booking) {
        Room room = booking.getRoom(); // Lấy Room từ Booking
        BigDecimal price = room.getPrice(); // Lấy giá từ Room
        long days = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
        return price.multiply(BigDecimal.valueOf(days));
    }
    
    @GetMapping("/edit/{id}")
    public String editBooking(@PathVariable Long id, Model model) {
        Booking booking = bookingService.getBookingById(id);

        if (booking == null) {
            model.addAttribute("error", "Không tìm thấy booking.");
            return "redirect:/admin/bookings";
        }

        Room room = booking.getRoom(); 
        model.addAttribute("booking", booking);
        model.addAttribute("room", room); 

        return "admin/booking/booking-edit"; 
    }
    
    @PostMapping("/edit/{id}")
    public String updateBooking(@PathVariable Long id,
                                @RequestParam String checkIn,
                                @RequestParam String checkOut,
                                @RequestParam int guests,
                                RedirectAttributes attributes) {
        try {
            Booking booking = bookingService.getBookingById(id);

            if (booking == null) {
                attributes.addFlashAttribute("error", "Không tìm thấy booking.");
                return "redirect:/admin/bookings";
            }

            Room room = booking.getRoom(); // Lấy thông tin phòng
            if (room != null && guests > room.getCapacity()) {
                attributes.addFlashAttribute("error", "Số lượng khách vượt quá sức chứa của phòng (" + room.getCapacity() + " khách).");
                return "redirect:/admin/bookings/edit/" + id; // Điều hướng về trang chỉnh sửa
            }

            // Cập nhật thông tin booking
            booking.setCheckIn(LocalDate.parse(checkIn));
            booking.setCheckOut(LocalDate.parse(checkOut));
            booking.setGuests(guests); 
            
            bookingService.updateBooking(id, booking);
            attributes.addFlashAttribute("success", "Cập nhật booking thành công.");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Lỗi khi cập nhật booking: " + e.getMessage());
        }

        return "redirect:/admin/bookings";
    }
}
