package com.example.hotelmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hotelmanager.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	
	List<Payment> findByUser_Id(Long userId);
	List<Payment> findByBooking_Id(Long bookingId);
	List<Payment> findByStatus(String status);
}
