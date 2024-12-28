package com.example.hotelmanager.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hotelmanager.model.Booking;
import com.example.hotelmanager.model.Room;
import com.example.hotelmanager.model.RoomType;
import com.example.hotelmanager.service.BookingService;
import com.example.hotelmanager.service.RoomTypeService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/statistical")
@Slf4j
public class AdminStatisticalController {

    private final BookingService bookingService;
    private final RoomTypeService roomTypeService;

    public AdminStatisticalController(BookingService bookingService, RoomTypeService roomTypeService) {
        this.bookingService = bookingService;
        this.roomTypeService = roomTypeService;
    }

    @GetMapping
    public String showStatisticalPage(
        @RequestParam(required = false) LocalDate fromDate,
        @RequestParam(required = false) LocalDate toDate,
        @RequestParam(required = false) Integer year,
        Model model) {
        
        // Default to current year if no year specified
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        // If fromDate and toDate are not provided, set default range for the specified year
        if (fromDate == null || toDate == null) {
            fromDate = LocalDate.of(year, 1, 1);
            toDate = LocalDate.of(year, 12, 31);
        }

        // Fetch filtered bookings
        List<Booking> filteredBookings = bookingService.getFilteredBookings(fromDate, toDate);

        // Calculate statistics based on filtered bookings
        int totalBookings = filteredBookings.size();
        int totalConfirmed = (int) filteredBookings.stream()
            .filter(booking -> "Confirmed".equals(booking.getStatus()))
            .count();
        int totalPending = (int) filteredBookings.stream()
            .filter(booking -> "Pending".equals(booking.getStatus()))
            .count();

        BigDecimal totalRevenue = filteredBookings.stream()
            .filter(booking -> "Confirmed".equals(booking.getStatus()))
            .map(this::calculateTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add attributes to model
        model.addAttribute("confirmedBookings", filteredBookings.stream()
            .filter(booking -> "Confirmed".equals(booking.getStatus()))
            .collect(Collectors.toList()));
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalConfirmed", totalConfirmed);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("totalRevenue", totalRevenue);

        // Preserve filter parameters for form
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("year", year);

        return "admin/statistical/statisticals";
    }

    private BigDecimal calculateTotalPrice(Booking booking) {
        Room room = booking.getRoom(); // Lấy Room từ Booking
        BigDecimal price = room.getPrice(); // Lấy giá từ Room
        long days = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
        return price.multiply(BigDecimal.valueOf(days));
    }

}
