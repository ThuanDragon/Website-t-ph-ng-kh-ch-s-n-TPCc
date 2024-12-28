package com.example.hotelmanager.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hotelmanager.model.Booking;
import com.example.hotelmanager.model.Room;
import com.example.hotelmanager.repository.BookingRepository;
import com.example.hotelmanager.repository.RoomRepository;

@Service
public class RoomAvailabilityService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut);
        return overlappingBookings.isEmpty();
    }

    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRooms(checkIn, checkOut);
    }

    public List<Room> getAvailableRoomsByType(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRoomsByType(roomTypeId, checkIn, checkOut);
    }
}