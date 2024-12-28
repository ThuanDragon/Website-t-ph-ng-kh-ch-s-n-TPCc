
package com.example.hotelmanager.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.example.hotelmanager.model.Amenities;
import com.example.hotelmanager.model.Room;
import com.example.hotelmanager.model.RoomType;
import com.example.hotelmanager.repository.RoomTypeRepository;
import com.example.hotelmanager.service.RoomService;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/rooms")
public class TestController {
    private final RoomService roomService;
    private final RoomTypeRepository roomTypeRepository;
    @Autowired
    public TestController(RoomService roomService, RoomTypeRepository roomTypeRepository) {
        this.roomService = roomService;
        this.roomTypeRepository = roomTypeRepository;
    }

    @GetMapping
    public String getAllRooms(Model model) {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomType> roomTypes = roomService.getAllRoomTypes();
        model.addAttribute("rooms", rooms);

        // Lấy dữ liệu
        List<String> locations = roomService.getDistinctLocations();
        List<Integer> capacities = roomService.getDistinctCapacities();
        List<String> statuses = roomService.getDistinctStatuses();
        List<Amenities> amenities = roomService.getAllAmenities(); 
        model.addAttribute("locations", locations);
        model.addAttribute("capacities", capacities);
        model.addAttribute("statuses", statuses);
        model.addAttribute("amenities", amenities);
        model.addAttribute("roomTypes", roomTypes);

        return "layout/main"; 
    }

    @GetMapping("/filter")
    public String filterRooms(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String amenity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Long roomTypeId,
            Model model) {
        List<Room> rooms = roomService.filterRooms(location, capacity, status, amenity, checkIn, checkOut, roomTypeId);
        model.addAttribute("rooms", rooms);

        // Đưa dữ liệu cần thiết lên view
        model.addAttribute("locations", roomService.getDistinctLocations());
        model.addAttribute("capacities", roomService.getDistinctCapacities());
        model.addAttribute("statuses", roomService.getDistinctStatuses());
        model.addAttribute("roomTypes", roomTypeRepository.findAll()); // Thêm danh sách loại phòng
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("amenity", amenity);
        model.addAttribute("roomTypeId", roomTypeId);

        return "layout/main";
    }


   //detail
    @GetMapping("/room/{id}")
    public String getRoomDetail(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
        Room room = roomService.getRoomById(id);
        model.addAttribute("room", room);

        Map<String, ?> flashAttributes = RequestContextUtils.getInputFlashMap(request);
        if (flashAttributes != null) {
            Object errorMessage = flashAttributes.get("errorMessage");
            if (errorMessage != null) {
                model.addAttribute("errorMessage", errorMessage);
            }
        }

        // Lấy các tiện ích từ loại phòng
        if (room.getRoomType() != null) {
            model.addAttribute("amenities", room.getRoomType().getAmenities());
        } else {
            model.addAttribute("amenities", null); 
        }

        return "room/detail";
    }
}
