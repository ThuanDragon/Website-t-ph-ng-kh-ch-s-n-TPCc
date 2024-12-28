package com.example.hotelmanager.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hotelmanager.model.Amenities;
import com.example.hotelmanager.repository.AmenityRepository;

import jakarta.persistence.EntityNotFoundException;

import com.example.hotelmanager.exception.ResourceNotFoundException; // Ngoại lệ tùy chỉnh

@Service
public class AmenityService {

    private final AmenityRepository amenityRepository;

    @Autowired
    public AmenityService(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    public List<Amenities> getAllAmenities() {
        return amenityRepository.findAll();
    }
    
    public Amenities getById(Long id) {
        return amenityRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tiện ích với ID: " + id));
    }

    
    public Amenities getAmenityById(Long id) {
        return amenityRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tiện ích"));
    }
    public List<Amenities> getAmenitiesByIds(List<Long> ids) {
        return amenityRepository.findAllById(ids);
    }


    public List<Amenities> searchAmenitiesByName(String name) {
        return amenityRepository.findByNameContainingIgnoreCase(name);
    }

    public Amenities createAmenity(Amenities amenity) {
        if (amenityRepository.existsByNameIgnoreCase(amenity.getName())) {
            throw new IllegalArgumentException("Amenity with this name already exists");
        }
        return amenityRepository.save(amenity);
    }

    public Amenities updateAmenity(Amenities amenity) {
        if (!amenityRepository.existsById(amenity.getId())) {
            throw new ResourceNotFoundException("Amenity not found with id: " + amenity.getId());
        }
        return amenityRepository.save(amenity);
    }

    public void deleteAmenity(Long id) {
        if (!amenityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Amenity not found with id: " + id);
        }
        amenityRepository.deleteById(id);
    }

    public boolean amenityExists(String name) {
        return amenityRepository.existsByNameIgnoreCase(name);
    }
}
