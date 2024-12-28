package com.example.hotelmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.hotelmanager.model.RoomType;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
	Optional<RoomType> findByName(String name);
	
	@Query("SELECT r FROM RoomType r JOIN FETCH r.amenities WHERE r.id = :id")
    Optional<RoomType> findByIdWithAmenities(@Param("id") Long id);
}
