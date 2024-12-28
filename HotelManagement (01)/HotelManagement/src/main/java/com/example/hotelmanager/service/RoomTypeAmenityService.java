package com.example.hotelmanager.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hotelmanager.model.RoomTypeAmenities;
import com.example.hotelmanager.repository.RoomTypeAmenityRepository;

import jakarta.transaction.Transactional;


@Service
public class RoomTypeAmenityService {

    private final RoomTypeAmenityRepository roomTypeAmenityRepository;

    @Autowired
    public RoomTypeAmenityService(RoomTypeAmenityRepository roomTypeAmenityRepository) {
        this.roomTypeAmenityRepository = roomTypeAmenityRepository;
    }

    public List<RoomTypeAmenities> getAllRoomTypeAmenities() {
        return roomTypeAmenityRepository.findAll();
    }

    public Optional<RoomTypeAmenities> getRoomTypeAmenityById(Long id) {
        return roomTypeAmenityRepository.findById(id);
    }

    public List<RoomTypeAmenities> getRoomTypeAmenitiesByRoomTypeId(Long roomTypeId) {
        return roomTypeAmenityRepository.findByRoomTypeId(roomTypeId);
    }

    public List<RoomTypeAmenities> getRoomTypeAmenitiesByAmenityId(Long amenityId) {
        return roomTypeAmenityRepository.findByAmenityId(amenityId);
    }

    public RoomTypeAmenities saveRoomTypeAmenity(RoomTypeAmenities roomTypeAmenity) {
        return roomTypeAmenityRepository.save(roomTypeAmenity);
    }

    @Transactional
    public void deleteRoomTypeAmenity(Long amenityId, Long roomTypeId) {
        roomTypeAmenityRepository.deleteRoomTypeAmenities(amenityId, roomTypeId);
    }

    @Transactional
    public void deleteRoomTypeAmenitiesByRoomTypeId(Long roomTypeId) {
        roomTypeAmenityRepository.deleteByRoomTypeId(roomTypeId);
    }
}
