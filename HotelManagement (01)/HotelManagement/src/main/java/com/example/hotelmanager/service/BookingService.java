package com.example.hotelmanager.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hotelmanager.model.Booking;
import com.example.hotelmanager.model.Room;
import com.example.hotelmanager.repository.BookingRepository;
import com.example.hotelmanager.repository.RoomRepository;
import com.example.hotelmanager.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import com.example.hotelmanager.exception.RoomAlreadyBookedException;
import com.example.hotelmanager.exception.ResourceNotFoundException;
//main
@Service
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                         RoomRepository roomRepository,
                         UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }
    
    public List<Booking> getFilteredBookings(LocalDate fromDate, LocalDate toDate) {
        return bookingRepository.findByCheckInBetween(fromDate, toDate);
    }
    
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    public List<Booking> getBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoom_Id(roomId);
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUser_Id(userId);
    }

    public List<Booking> getBookingsBetweenDates(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return bookingRepository.findByCheckInBetween(startDate, endDate);
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        validateBooking(booking);
        checkRoomAvailability(booking);
        
        // Set initial status
        booking.setStatus("Pending");
        
        // Save the booking
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Created new booking with ID: {}", savedBooking.getId());
        
        return savedBooking;
    }

    @Transactional
    public Booking updateBooking(Long id, Booking booking) {
        Booking existingBooking = getBookingById(id);
        
        // Chỉ cho phép update nếu booking chưa hoàn thành hoặc hủy
        if ("Completed".equals(existingBooking.getStatus()) || 
            "Cancelled".equals(existingBooking.getStatus())) {
            throw new IllegalStateException("Cannot update completed or cancelled booking");
        }

        validateBooking(booking);
        
        // Nếu thay đổi phòng hoặc ngày, kiểm tra availability
        if (!existingBooking.getRoom().getId().equals(booking.getRoom().getId()) ||
            !existingBooking.getCheckIn().equals(booking.getCheckIn()) ||
            !existingBooking.getCheckOut().equals(booking.getCheckOut())) {
            checkRoomAvailability(booking);
        }

        // Update the booking
        existingBooking.setCheckIn(booking.getCheckIn());
        existingBooking.setCheckOut(booking.getCheckOut());
        existingBooking.setGuests(booking.getGuests());
        existingBooking.setRoom(booking.getRoom());
        existingBooking.setStatus(booking.getStatus());

        Booking updatedBooking = bookingRepository.save(existingBooking);
        log.info("Updated booking with ID: {}", updatedBooking.getId());
        
        return updatedBooking;
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = getBookingById(id);
        
        if ("Completed".equals(booking.getStatus())) {
            throw new IllegalStateException("Cannot cancel completed booking");
        }
        
        booking.setStatus("Cancelled");
        bookingRepository.save(booking);
        log.info("Cancelled booking with ID: {}", id);
    }

    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = getBookingById(id);
        
        // Chỉ cho phép xóa booking đã hủy
        if (!"Cancelled".equals(booking.getStatus())) {
            throw new IllegalStateException("Can only delete cancelled bookings");
        }
        
        bookingRepository.deleteById(id);
        log.info("Deleted booking with ID: {}", id);
    }

    private void checkRoomAvailability(Booking newBooking) {
        List<Booking> existingBookings = bookingRepository.findOverlappingBookings(
            newBooking.getRoom().getId(),
            newBooking.getCheckIn(),
            newBooking.getCheckOut()
        );

        if (!existingBookings.isEmpty()) {
            throw new RoomAlreadyBookedException("Phòng đã được đặt trong khoảng thời gian này.");
        }
    }

    private void validateBooking(Booking booking) {
        if (booking.getCheckIn() == null || booking.getCheckOut() == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required");
        }

        if (booking.getCheckIn().isAfter(booking.getCheckOut())) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }

        if (booking.getCheckIn().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        if (booking.getGuests() <= 0) {
            throw new IllegalArgumentException("Number of guests must be positive");
        }

        Room room = roomRepository.findById(booking.getRoom().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (booking.getGuests() > room.getCapacity()) {
            throw new IllegalArgumentException(
                "Sức chứa của phòng này hiện tại là " + room.getCapacity() + ". Vui lòng nhập lại số lượng khách !"
            );
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }
    
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, Integer guests, Long roomTypeId) {
        if (roomTypeId != null) {
            return bookingRepository.findAvailableRoomsByTypeAndCapacity(
                roomTypeId, guests, checkIn, checkOut
            );
        } else {
            return bookingRepository.findAvailableRooms(checkIn, checkOut)
                .stream()
                .filter(room -> room.getCapacity() >= guests)
                .collect(Collectors.toList());
        }
    }

    public boolean canUserBook(Long userId) {
        return !bookingRepository.hasActiveBookings(userId);
    }

    public Map<String, Long> getBookingStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", bookingRepository.countByStatus("PENDING"));
        stats.put("confirmed", bookingRepository.countByStatus("CONFIRMED"));
        stats.put("completed", bookingRepository.countByStatus("COMPLETED"));
        stats.put("cancelled", bookingRepository.countByStatus("CANCELLED"));
        return stats;
    }
    public Booking updateBookingStatus(Long id, String status)	{
    	Booking booking = bookingRepository.findById(id)
    			.orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    	booking.setStatus(status);
    	return bookingRepository.save(booking);
    }
    public List<Booking> getConfirmedBookings()	{
    	return bookingRepository.findByStatus("Confirmed");
    }
    
    public List<Booking> getPendingBookings()	{
    	return bookingRepository.findByStatus("Pending");
    }
    
    public void acceptBooking(Long id) {
    	Booking booking = getBookingById(id);
    	booking.setStatus("Confirmed");
    	bookingRepository.save(booking);
    }
}