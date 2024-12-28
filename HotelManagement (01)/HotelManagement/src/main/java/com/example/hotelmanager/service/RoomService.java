package com.example.hotelmanager.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hotelmanager.model.Amenities;
import com.example.hotelmanager.model.Booking;
import com.example.hotelmanager.model.Room;
import com.example.hotelmanager.model.RoomType;
import com.example.hotelmanager.repository.AmenityRepository;
import com.example.hotelmanager.repository.BookingRepository;
import com.example.hotelmanager.repository.RoomRepository;
import com.example.hotelmanager.repository.RoomTypeRepository;

import jakarta.transaction.Transactional;

import com.example.hotelmanager.exception.ResourceNotFoundException; // Custom Exception

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
	private BookingRepository bookingRepository;
	private AmenityRepository amenityRepository;
	
	public List<Room> filterRooms(String location, Integer capacity, String status, String amenity, LocalDate checkIn, LocalDate checkOut, Long roomTypeId) {
	    List<Room> rooms = roomRepository.findAll();

	    // Lọc theo location
	    if (location != null && !location.isEmpty()) {
	        rooms = rooms.stream()
	                     .filter(room -> room.getLocation().equalsIgnoreCase(location))
	                     .collect(Collectors.toList());
	    }

	    // Lọc theo capacity
	    if (capacity != null) {
	        rooms = rooms.stream()
	                     .filter(room -> room.getCapacity() == capacity)
	                     .collect(Collectors.toList());
	    }

	    // Lọc theo status
	    if (status != null && !status.isEmpty()) {
	        rooms = rooms.stream()
	                     .filter(room -> room.getStatus().equalsIgnoreCase(status))
	                     .collect(Collectors.toList());
	    }

	    // Lọc theo loại phòng
	    if (roomTypeId != null) {
	        rooms = rooms.stream()
	                     .filter(room -> room.getRoomType() != null && room.getRoomType().getId().equals(roomTypeId))
	                     .collect(Collectors.toList());
	    }

	    // Lọc theo tiện nghi
	    if (amenity != null && !amenity.isEmpty()) {
	        rooms = rooms.stream()
	                     .filter(room -> room.getRoomType().getAmenities().stream()
	                         .anyMatch(a -> a.getName().equalsIgnoreCase(amenity)))
	                     .collect(Collectors.toList());
	    }

	    // Lọc theo ngày nhận và trả phòng
	    if (checkIn != null && checkOut != null) {
	        rooms = rooms.stream()
	                     .filter(room -> isRoomAvailable(room.getId(), checkIn, checkOut))
	                     .collect(Collectors.toList());
	    }

	    return rooms;
	}

    
    private boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut);
        return overlappingBookings.isEmpty();
    }
    
    @Autowired
    public RoomService(RoomRepository roomRepository, RoomTypeRepository roomTypeRepository, BookingRepository bookingRepository, AmenityRepository amenityRepository) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.bookingRepository = bookingRepository;
        this.amenityRepository = amenityRepository;
    }
    
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    
    public List<Room> getRoomsByStatus(String status) {
        return roomRepository.findByStatus(status);
    }
    
    public List<Room> getRoomsByTypeId(Long roomTypeId) {
        return roomRepository.findByRoomType_Id(roomTypeId);
    }
    
    public List<Amenities> getAllAmenities() {
        return amenityRepository.findAll();
    }

    
    public Room createRoom(Room room) {       
        if (room.getRoomType() == null || room.getRoomType().getId() == null) {
            throw new IllegalArgumentException("Room type must be specified.");
        }
    	RoomType roomType = roomTypeRepository.findById(room.getRoomType().getId())
                .orElseThrow(() -> new ResourceNotFoundException("RoomType not found"));
        room.setRoomType(roomType);
        

        return roomRepository.save(room);
    }
    
    public Room updateRoom(Room room) {
    	if (room.getId() == null || !roomRepository.existsById(room.getId())) {
            throw new ResourceNotFoundException("Room not found");
        }
        
        RoomType roomType = roomTypeRepository.findById(room.getRoomType().getId())
                .orElseThrow(() -> new ResourceNotFoundException("RoomType not found"));
        room.setRoomType(roomType);

        return roomRepository.save(room);
    }
    
    @Transactional
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
    
    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }
    
    public List<String> getDistinctLocations() {
        return roomRepository.findDistinctLocations();
    }

    public List<Integer> getDistinctCapacities() {
        return roomRepository.findDistinctCapacities();
    }

    public List<String> getDistinctStatuses() {
        return roomRepository.findDistinctStatuses();
    }
    
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, Integer guests) {
        List<Room> allRooms = roomRepository.findByCapacityGreaterThanEqual(guests);
        return allRooms.stream()
            .filter(room -> isRoomAvailable(room.getId(), checkIn, checkOut))
            .collect(Collectors.toList());
    }
    
    public List<Room> findRoomsByPriceLessThan(double price) {
        return roomRepository.findByPriceLessThan(price);
    }
    public Optional<Room> findRoomByRoomNumber(int roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber);
    }
}
