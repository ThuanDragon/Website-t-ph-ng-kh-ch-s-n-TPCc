package com.example.hotelmanager.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hotelmanager.model.Room;
import com.example.hotelmanager.model.RoomType;
import com.example.hotelmanager.service.AmenityService;
import com.example.hotelmanager.service.FileStorageService;
import com.example.hotelmanager.service.RoomService;
import com.example.hotelmanager.service.RoomTypeService;

@Controller
@RequestMapping("/admin/rooms")
public class RoomController {
	
	private final RoomService roomService;
	private final RoomTypeService roomTypeService;
	private final AmenityService amenityService;
	private final FileStorageService fileStorageService;
	
	@Autowired
	public RoomController(RoomService roomService, RoomTypeService roomTypeService, AmenityService amenityService, FileStorageService fileStorageService)	{
		this.roomService = roomService;
		this.roomTypeService = roomTypeService;
		this.amenityService = amenityService;
		this.fileStorageService = fileStorageService;
	}
	
	@GetMapping
	public String listRooms(Model model)	{
		model.addAttribute("room", new Room());
		model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
		model.addAttribute("status", List.of("Available", "Occupied", "Maintenance"));
		model.addAttribute("rooms", roomService.getAllRooms());
		return "admin/room/list";
	}
	
	@GetMapping("/get/{id}")
	@ResponseBody
	public ResponseEntity<Room> getRoomById(@PathVariable Long id)	{
		try {
			Room room = roomService.getRoomById(id);
			return ResponseEntity.ok(room);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
	
	@GetMapping("/add")
	public String showAddRoomForm(Model model) {
		model.addAttribute("room", new Room());
		model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
		model.addAttribute("status", List.of("Available", "Unavailable"));
		return "admin/room/form";
	}
	
	@PostMapping("/save")
	@ResponseBody
	public ResponseEntity<?> saveRoom(
	        @ModelAttribute Room room,
	        @RequestParam("imageFiles") MultipartFile[] imageFiles) {
	    try {
	        // Lưu tệp và cập nhật URL
	        int index = 0; // Chỉ số để theo dõi URL
	        for (MultipartFile file : imageFiles) {
	            if (!file.isEmpty()) {
	                String imageUrl = fileStorageService.storeFile(file);
	                switch (index) {
	                    case 0 -> room.setImageUrl(imageUrl);
	                    case 1 -> room.setImageUrl2(imageUrl);
	                    case 2 -> room.setImageUrl3(imageUrl);
	                    case 3 -> room.setImageUrl4(imageUrl);
	                }
	                index++;
	                if (index >= 4) break; // Chỉ lưu tối đa 4 URL
	            }
	        }

	        if (room.getId() == null) {
	            roomService.createRoom(room);
	        } else {
	            roomService.updateRoom(room);
	        }
	        return ResponseEntity.ok(Map.of(
	            "success", true,
	            "message", "Phòng đã được lưu thành công!"
	        ));
	    } catch (Exception e) {
	        return ResponseEntity.badRequest().body(Map.of(
	            "success", false,
	            "message", "Lỗi khi lưu phòng: " + e.getMessage()
	        ));
	    }
	}


	
	@GetMapping("/edit/{id}")
	public String showEditRoomForm(@PathVariable Long id, Model model) {
		Room room = roomService.getRoomById(id);
		model.addAttribute("room", room);
		model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
		model.addAttribute("status", List.of("Available", "Occupied", "Maintenance"));
		model.addAttribute("amenities", amenityService.getAllAmenities());
		return "admin/room/form";
	}
	
	@PostMapping("/update")
	public String saveRoom(@ModelAttribute Room room,
			@RequestParam("imageFiles") MultipartFile[] imageFiles,
			RedirectAttributes redirectAttributes) {
	    try {
	        int index = 0;
	        for (MultipartFile file : imageFiles) {
	            if (!file.isEmpty()) {
	                String imageUrl = fileStorageService.storeFile(file);
	                switch (index) {
	                    case 0 -> room.setImageUrl(imageUrl);
	                    case 1 -> room.setImageUrl2(imageUrl);
	                    case 2 -> room.setImageUrl3(imageUrl);
	                    case 3 -> room.setImageUrl4(imageUrl);
	                }
	                index++;
	                if (index >= 4) break;
	            }
	        }
	        
	        //Cập nhật thông tin phòng
            roomService.updateRoom(room);

	        // Thêm thông báo thành công vào redirect attributes
	        redirectAttributes.addFlashAttribute("successMessage", "Phòng đã được lưu thành công!");

	        return "redirect:/admin/rooms";
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu phòng: " + e.getMessage());
	        return "redirect:/admin/rooms";
	    }
	}

	
	@DeleteMapping("/delete/{id}")
	@ResponseBody
	public ResponseEntity<?> deleteRoomAjax(@PathVariable Long id)	{
		try {
			roomService.deleteRoom(id);
			return ResponseEntity.ok(Map.of(
					"success", true,
					"message", "Phòng đã được xóa thành công"
					));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of(
					"success", false,
					"message", "Lỗi khi xóa phòng: " +e.getMessage()
					));
		}
	}
	
    @GetMapping("/types")
    @ResponseBody
    public ResponseEntity<List<RoomType>> getRoomTypes() {
        return ResponseEntity.ok(roomTypeService.getAllRoomTypes());
    }
	
	@PostMapping("/validate")
	@ResponseBody
	public ResponseEntity<?> validateRoom(@RequestBody Room room)	{
		Map<String, String> errors = new HashMap<>();
		
		if (room.getRoomNumber() <= 0)	{
			errors.put("roomNumber", "Số phòng phải lớn hơn 0");
		}
		if (room.getCapacity() <= 0)	{
			errors.put("capacity", "Sức chứa phải lớn hơn 0");
		}
		
		if (errors.isEmpty())	{
			return ResponseEntity.ok(Map.of("valid", true));
		} else {
			return ResponseEntity.badRequest().body(errors);
		}
	}
}
