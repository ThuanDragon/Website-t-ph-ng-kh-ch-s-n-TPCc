package com.example.hotelmanager.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hotelmanager.exception.ResourceNotFoundException;
import com.example.hotelmanager.model.RoomType;
import com.example.hotelmanager.repository.RoomTypeRepository;
import com.example.hotelmanager.service.AmenityService;
import com.example.hotelmanager.service.RoomTypeService;

import java.util.List;

@Controller
@RequestMapping("/admin/roomtypes")
public class RoomTypeController {
	@Autowired
	private RoomTypeRepository roomTypeRepository;
	
    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private AmenityService amenityService;

    @GetMapping
    public String listRoomTypes(Model model) {
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        return "admin/roomtype/roomtypes";  // Correct path for room types list view
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("roomType", new RoomType());
        model.addAttribute("allAmenities", amenityService.getAllAmenities());
        return "admin/roomtype/roomtype_form";  // Correct view name for create form
    }

    @PostMapping("/save")
    public String saveRoomType(
        @Validated @ModelAttribute("roomType") RoomType roomType,
        BindingResult result,
        @RequestParam(required = false) List<Long> amenityIds,
        RedirectAttributes redirectAttributes
    ) {
        // Kiểm tra lỗi trong form
        if (result.hasErrors()) {
            return "admin/roomtype/roomtype_form"; // Trả về trang chỉnh sửa hoặc tạo mới
        }

        // Kiểm tra tiện ích không được để trống
        if (amenityIds == null || amenityIds.isEmpty()) {
            // Thêm thông báo lỗi và chuyển hướng về trang chỉnh sửa
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một tiện nghi!");
            return "redirect:/admin/roomtypes/edit/" + roomType.getId(); // Chuyển hướng về trang chỉnh sửa của loại phòng hiện tại
        }

        try {
            roomTypeService.saveRoomTypeWithAmenities(roomType, amenityIds);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu loại phòng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            e.printStackTrace(); // Để debug
        }

        return "redirect:/admin/roomtypes";
    }




    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        RoomType roomType = roomTypeService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room type not found for id " + id));
        model.addAttribute("roomType", roomType);
        model.addAttribute("allAmenities", amenityService.getAllAmenities());
        return "admin/roomtype/roomtype_form";  // Use the same form for both create and edit
    }

    @PostMapping("/delete/{id}")
    public String deleteRoomType(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        roomTypeService.deleteRoomType(id);
        redirectAttributes.addFlashAttribute("successMessage", "Room type deleted successfully!");
        return "redirect:/admin/roomtypes";
    }
}