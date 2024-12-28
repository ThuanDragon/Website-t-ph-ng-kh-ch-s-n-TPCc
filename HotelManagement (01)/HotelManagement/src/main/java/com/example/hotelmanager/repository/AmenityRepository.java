package com.example.hotelmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hotelmanager.model.Amenities;

@Repository
public interface AmenityRepository extends JpaRepository<Amenities, Long> {
	List<Amenities> findByNameContainingIgnoreCase(String name);
	boolean existsByNameIgnoreCase(String name);
}
