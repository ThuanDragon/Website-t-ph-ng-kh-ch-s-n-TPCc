package com.example.hotelmanager.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.hotelmanager.model.Booking;
import com.example.hotelmanager.model.Room;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Tìm các booking trùng lịch cho một phòng cụ thể
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND " +
           "b.status NOT IN ('CANCELLED', 'REJECTED') AND " +
           "((b.checkIn <= :checkOut AND b.checkOut >= :checkIn) OR " +
           "(b.checkIn >= :checkIn AND b.checkIn < :checkOut))")
    List<Booking> findOverlappingBookings(
        @Param("roomId") Long roomId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    // Tìm các phòng đã được đặt trong khoảng thời gian
    @Query("SELECT DISTINCT b.room FROM Booking b WHERE " +
           "b.status NOT IN ('CANCELLED', 'REJECTED') AND " +
           "((b.checkIn <= :checkOut AND b.checkOut >= :checkIn) OR " +
           "(b.checkIn >= :checkIn AND b.checkIn < :checkOut))")
    List<Room> findBookedRooms(
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    // Tìm các phòng còn trống trong khoảng thời gian
    @Query("SELECT r FROM Room r WHERE r.id NOT IN " +
           "(SELECT DISTINCT b.room.id FROM Booking b WHERE " +
           "b.status NOT IN ('CANCELLED', 'REJECTED') AND " +
           "((b.checkIn <= :checkOut AND b.checkOut >= :checkIn) OR " +
           "(b.checkIn >= :checkIn AND b.checkIn < :checkOut)))")
    List<Room> findAvailableRooms(
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    // Tìm các phòng còn trống theo loại phòng và số người
    @Query("SELECT r FROM Room r WHERE r.roomType.id = :roomTypeId " +
           "AND r.capacity >= :guests " +
           "AND r.id NOT IN " +
           "(SELECT DISTINCT b.room.id FROM Booking b WHERE " +
           "b.status NOT IN ('CANCELLED', 'REJECTED') AND " +
           "((b.checkIn <= :checkOut AND b.checkOut >= :checkIn) OR " +
           "(b.checkIn >= :checkIn AND b.checkIn < :checkOut)))")
    List<Room> findAvailableRoomsByTypeAndCapacity(
        @Param("roomTypeId") Long roomTypeId,
        @Param("guests") Integer guests,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    // Các method cơ bản
    List<Booking> findByUser_Id(Long userId);
    List<Booking> findByRoom_Id(Long roomId);
    List<Booking> findByCheckInBetween(LocalDate startDate, LocalDate endDate);
    
    // Thêm các method hữu ích
    List<Booking> findByStatus(String status);
    List<Booking> findByUser_IdAndStatus(Long userId, String status);
    
    // Đếm số booking theo trạng thái
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    Long countByStatus(@Param("status") String status);
    
    // Thống kê booking theo tháng
    @Query("SELECT new map(FUNCTION('MONTH', b.checkIn) as month, COUNT(b) as count) " +
           "FROM Booking b " +
           "WHERE FUNCTION('YEAR', b.checkIn) = :year " +
           "GROUP BY FUNCTION('MONTH', b.checkIn)")
    List<Map<String, Object>> countBookingsByMonth(@Param("year") int year);

    // Kiểm tra xem user có booking nào đang active không
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.user.id = :userId AND " +
           "b.status NOT IN ('CANCELLED', 'COMPLETED') AND " +
           "b.checkOut >= CURRENT_DATE")
    boolean hasActiveBookings(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
            "AND ((b.checkIn <= :checkOut AND b.checkOut >= :checkIn))")
     List<Booking> findConflictingBookings(
         @Param("roomId") Long roomId, 
         @Param("checkIn") LocalDate checkIn, 
         @Param("checkOut") LocalDate checkOut
     );
}