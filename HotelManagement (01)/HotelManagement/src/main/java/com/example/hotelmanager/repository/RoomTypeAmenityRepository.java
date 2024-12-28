package com.example.hotelmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.hotelmanager.model.RoomTypeAmenities;
import com.example.hotelmanager.model.RoomTypeAmenitiesId;

import jakarta.transaction.Transactional;

@Repository
public interface RoomTypeAmenityRepository extends JpaRepository<RoomTypeAmenities, Long> {
    List<RoomTypeAmenities> findByRoomTypeId(Long roomTypeId);
    List<RoomTypeAmenities> findByAmenityId(Long amenityId);

    @Transactional
    @Modifying
    @Query("DELETE FROM RoomTypeAmenities rta WHERE rta.roomType.id = :roomTypeId")
    void deleteByRoomTypeId(@Param("roomTypeId") Long roomTypeId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM RoomTypeAmenities rta WHERE rta.amenity.id = :amenityId AND rta.roomType.id = :roomTypeId")
    void deleteRoomTypeAmenities(@Param("amenityId") Long amenityId, @Param("roomTypeId") Long roomTypeId);
}
