package com.example.hotelmanager.Controller;

import com.example.hotelmanager.model.Amenities;
import com.example.hotelmanager.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/amenities")
public class AdminAmenityController {

    private final AmenityService amenityService;

    @Autowired
    public AdminAmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }

    // Hiển thị tất cả tiện ích
    @GetMapping
    public String listAmenities(Model model) {
        model.addAttribute("allAmenities", amenityService.getAllAmenities());
        return "admin/amenities/amenity"; // Trang hiển thị danh sách tiện ích
    }

    // Hiển thị form tạo hoặc chỉnh sửa tiện ích
    @GetMapping({"/create", "/edit/{id}"})
    public String showForm(@PathVariable(required = false) Long id, Model model) {
        Amenities amenity = (id == null) ? new Amenities() : amenityService.getAmenityById(id);
        model.addAttribute("amenity", amenity);
        return "admin/amenities/amenity_form"; // Trang form dùng chung
    }

    // Xử lý lưu tiện ích (tạo hoặc cập nhật)
    @PostMapping("/save")
    public String saveAmenity(@ModelAttribute Amenities amenity, RedirectAttributes redirectAttributes) {
        try {
            if (amenity.getId() == null) {
                // Tạo mới tiện ích
                amenityService.createAmenity(amenity);
            } else {
                // Cập nhật tiện ích
                amenityService.updateAmenity(amenity);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Lưu tiện ích thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lưu tiện ích không thành công!");
        }
        return "redirect:/admin/amenities";
    }


    // Xóa tiện ích
    @PostMapping("/delete/{id}")
    public String deleteAmenity(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            amenityService.deleteAmenity(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa tiện ích thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa tiện ích không thành công, tiện ích này đang được gán.");
        }
        return "redirect:/admin/amenities";
    }
}
