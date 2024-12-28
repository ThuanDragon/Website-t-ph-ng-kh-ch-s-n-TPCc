package com.example.hotelmanager.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hotelmanager.model.Amenities;
import com.example.hotelmanager.model.RoomType;
import com.example.hotelmanager.model.RoomTypeAmenities;
import com.example.hotelmanager.repository.AmenityRepository;
import com.example.hotelmanager.repository.RoomTypeAmenityRepository;
import com.example.hotelmanager.repository.RoomTypeRepository;

import jakarta.transaction.Transactional;

import com.example.hotelmanager.exception.ResourceNotFoundException;

@Service
@Transactional
public class RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;
    private final RoomTypeAmenityRepository roomTypeAmenityRepository;
    private final AmenityRepository amenityRepository;

    public RoomTypeService(
        RoomTypeRepository roomTypeRepository, 
        RoomTypeAmenityRepository roomTypeAmenityRepository, 
        AmenityRepository amenityRepository
    ) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomTypeAmenityRepository = roomTypeAmenityRepository;
        this.amenityRepository = amenityRepository;
    }

    public Optional<RoomType> findById(Long id) {
        return roomTypeRepository.findById(id);
    }

    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    public RoomType createRoomType(RoomType roomType, List<Long> amenityIds) {
        // Kiểm tra trùng lặp tên
        if (roomTypeRepository.findByName(roomType.getName()).isPresent()) {
            throw new DuplicateResourceException("Room type with name '" + roomType.getName() + "' already exists");
        }

        // Lưu room type
        RoomType savedRoomType = roomTypeRepository.save(roomType);

        // Lưu amenities
        saveRoomTypeAmenities(savedRoomType, amenityIds);

        return savedRoomType;
    }

    public RoomType updateRoomType(Long id, RoomType roomTypeDetails, List<Long> amenityIds) {
        RoomType existingRoomType = roomTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + id));

        // Cập nhật thông tin
        existingRoomType.setName(roomTypeDetails.getName());
        existingRoomType.setDescription(roomTypeDetails.getDescription());

        // Lưu room type
        RoomType updatedRoomType = roomTypeRepository.save(existingRoomType);

        // Cập nhật amenities
        updateRoomTypeAmenities(updatedRoomType, amenityIds);

        return updatedRoomType;
    }

    private void saveRoomTypeAmenities(RoomType roomType, List<Long> amenityIds) {
        if (amenityIds != null && !amenityIds.isEmpty()) {
            for (Long amenityId : amenityIds) {
                Amenities amenity = amenityRepository.findById(amenityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Amenity not found: " + amenityId));

                RoomTypeAmenities roomTypeAmenity = new RoomTypeAmenities();
                roomTypeAmenity.setRoomType(roomType);
                roomTypeAmenity.setAmenity(amenity);

                roomTypeAmenityRepository.save(roomTypeAmenity);
            }
        }
    }

    private void updateRoomTypeAmenities(RoomType roomType, List<Long> amenityIds) {
        // Xóa các amenities cũ
        roomTypeAmenityRepository.deleteByRoomTypeId(roomType.getId());
        roomTypeAmenityRepository.flush();
        
        // Thêm amenities mới
saveRoomTypeAmenities(roomType, amenityIds);
    }
    public void deleteRoomType(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + id));

        // Xóa các liên kết amenities trước
        roomTypeAmenityRepository.deleteByRoomTypeId(id);

        // Sau đó xóa room type
        roomTypeRepository.delete(roomType);
    }
    
    @Transactional
    public void saveRoomTypeWithAmenities(final RoomType roomType, List<Long> amenityIds) {
    	roomTypeAmenityRepository.deleteByRoomTypeId(roomType.getId());
        roomTypeAmenityRepository.flush();
    	
    	// Sử dụng final để biến không thay đổi
        RoomType savedRoomType = roomTypeRepository.saveAndFlush(roomType);

        List<RoomTypeAmenities> roomTypeAmenities = amenityIds.stream()
            .map(amenityId -> {
                Amenities amenity = amenityRepository.findById(amenityId)
                    .orElseThrow(() -> new RuntimeException("Amenity not found: " + amenityId));
                
                RoomTypeAmenities roomTypeAmenity = new RoomTypeAmenities();
                roomTypeAmenity.setRoomType(savedRoomType); 
                roomTypeAmenity.setAmenity(amenity);
                
                return roomTypeAmenity;
            })
            .collect(Collectors.toList());

        roomTypeAmenityRepository.saveAll(roomTypeAmenities);
    }
    	
    // Custom exception để xử lý trường hợp trùng lặp
    public class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String message) {
            super(message);
        }
    }
    
}