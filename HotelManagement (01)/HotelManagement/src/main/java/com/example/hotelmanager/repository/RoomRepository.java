package com.example.hotelmanager.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.hotelmanager.model.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
	List<Room> findByStatus(String status);
	List<Room> findByRoomType_Id(Long roomTypeId);	
    List<Room> findByCapacityGreaterThanEqual(Integer capacity);

	//bo loc
	@Query("SELECT DISTINCT r.location FROM Room r")
	List<String> findDistinctLocations();

	@Query("SELECT DISTINCT r.capacity FROM Room r")
	List<Integer> findDistinctCapacities();

	@Query("SELECT DISTINCT r.status FROM Room r")
	List<String> findDistinctStatuses();

	
    @Query("SELECT DISTINCT r FROM Room r " +
           "JOIN r.roomType rt " +
           "JOIN rt.amenities a " +
           "WHERE (:location IS NULL OR r.location = :location) " +
           "AND (:capacity IS NULL OR r.capacity = :capacity) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:amenityName IS NULL OR a.name = :amenityName)")
    List<Room> filterRooms(@Param("location") String location,
                           @Param("capacity") Integer capacity,
                           @Param("status") String status,
                           @Param("amenityName") String amenityName);
	
    
    //loc theo ngay
    @Query("SELECT r FROM Room r WHERE r NOT IN " +
            "(SELECT DISTINCT b.room FROM Booking b WHERE " +
            "b.status != 'CANCELLED' AND " +
            "((b.checkIn <= :checkOut AND b.checkOut >= :checkIn) OR " +
            "(b.checkIn >= :checkIn AND b.checkIn < :checkOut)))")
     List<Room> findAvailableRooms(@Param("checkIn") LocalDate checkIn,
                                   @Param("checkOut") LocalDate checkOut);

     @Query("SELECT r FROM Room r WHERE r.roomType.id = :roomTypeId AND r NOT IN " +
            "(SELECT DISTINCT b.room FROM Booking b WHERE " +
            "b.status != 'CANCELLED' AND " +
            "((b.checkIn <= :checkOut AND b.checkOut >= :checkIn) OR " +
            "(b.checkIn >= :checkIn AND b.checkIn < :checkOut)))")
     List<Room> findAvailableRoomsByType(@Param("roomTypeId") Long roomTypeId,
                                         @Param("checkIn") LocalDate checkIn,
                                         @Param("checkOut") LocalDate checkOut);
     
     @Query("SELECT r FROM Room r JOIN FETCH r.roomType rt JOIN FETCH rt.amenities WHERE r.id = :id")
     Optional<Room> findByIdWithAmenities(@Param("id") Long id);


     List<Room> findByPriceLessThan(double price);



     Optional<Room> findByRoomNumber(int roomNumber);

}
