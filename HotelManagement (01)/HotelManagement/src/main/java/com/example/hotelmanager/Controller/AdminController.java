package com.example.hotelmanager.Controller;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.hotelmanager.model.Amenities;
import com.example.hotelmanager.model.Booking;
import com.example.hotelmanager.model.News;
import com.example.hotelmanager.model.Room;
import com.example.hotelmanager.model.RoomType;
import com.example.hotelmanager.model.User;
import com.example.hotelmanager.service.AmenityService;
import com.example.hotelmanager.service.BookingService;
import com.example.hotelmanager.service.NewsService;
import com.example.hotelmanager.service.RoomService;
import com.example.hotelmanager.service.RoomTypeService;
import com.example.hotelmanager.service.UserService;


@Controller
@RequestMapping("/api/admin")
public class AdminController {

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final UserService userService;
    private final BookingService bookingService;
    private final AmenityService amenityService;
    private final NewsService newsService;
    
    public AdminController(RoomService roomService, RoomTypeService roomTypeService,
                           UserService userService, BookingService bookingService,
                           AmenityService amenityService, NewsService newsService) {
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;
        this.userService = userService;
        this.bookingService = bookingService;
		this.amenityService = amenityService;
		this.newsService = newsService;
    }

    @GetMapping
    public String showAdminDashboard(Model model) {
        model.addAttribute("roomCount", roomService.getAllRooms().size());
        model.addAttribute("userCount", userService.getAllUsers().size());
        model.addAttribute("bookingCount", bookingService.getConfirmedBookings().size() + bookingService.getPendingBookings().size());
        return "admin/admin-dashboard";
    }


    @GetMapping("/rooms")
    public String listRooms(Model model) {
        model.addAttribute("room", new Room());
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        model.addAttribute("status", List.of("Available", "Occupied", "Maintenance"));
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/room/list";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", new User());
        model.addAttribute("roles", Arrays.asList("Guest", "Staff"));
        return "admin/user/users";
    }

    @GetMapping("/bookings")
    public String showBookings(Model model) {
        List<Booking> confirmedBookings = bookingService.getConfirmedBookings();
        List<Booking> pendingBookings = bookingService.getPendingBookings();
        
        int totalConfirmed = confirmedBookings.size();
        int totalPending = pendingBookings.size();
        int totalBookings = totalConfirmed + totalPending;
        
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalConfirmed", totalConfirmed);
        model.addAttribute("totalPending", totalPending);
        return "admin/booking/bookings";
    }
    
    @GetMapping("/statistical")
    public String showStatistical(Model model) {
        List<Booking> confirmedBookings = bookingService.getConfirmedBookings();
        List<Booking> pendingBookings = bookingService.getPendingBookings();

        int totalConfirmed = confirmedBookings.size();
        int totalPending = pendingBookings.size();
        int totalBookings = totalConfirmed + totalPending;
        BigDecimal totalRevenue = confirmedBookings.stream()
                .map(this::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalConfirmed", totalConfirmed);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("totalRevenue", totalRevenue);
        
        return "admin/statistical/statisticals";
    }

    private BigDecimal calculateTotalPrice(Booking booking) {
        Room room = booking.getRoom(); // Lấy Room từ Booking
        BigDecimal price = room.getPrice(); // Lấy giá từ Room
        long days = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
        return price.multiply(BigDecimal.valueOf(days));
    }
    
    @GetMapping("/roomtype")
    public String showRoomtype(Model model) {
        List<RoomType> roomTypes = roomTypeService.getAllRoomTypes();
        List<Amenities> amenities = amenityService.getAllAmenities();
        
        model.addAttribute("roomTypes", roomTypes);  // Thêm roomTypes vào model
        model.addAttribute("allAmenities", amenities);  // Thêm allAmenities vào model
        model.addAttribute("roomType", new RoomType());
        
        return "admin/roomtype/roomtypes";
    }
    
    @GetMapping("/amenities")
    public String showAmenities(Model model) {
        List<Amenities> amenities = amenityService.getAllAmenities();
        model.addAttribute("allAmenities", amenities);
        
        return "admin/amenities/amenity";
    }
    
    @GetMapping("/news")
    public String showNews(Model model) {
    	List<News> news = newsService.getAllNews();
    	model.addAttribute("allNews", news);
    	
    	return "admin/news/news1";
    }
}