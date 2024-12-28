package com.example.hotelmanager.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "booking")
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "room_id", nullable = false)
	private Room room;
	
	@ManyToOne
	@JoinColumn(name = "room_type_id", nullable = false)
	private RoomType roomType;
	
	@Column(nullable = false)
	private LocalDate checkIn;
	
	@Column(nullable = false)
	private LocalDate checkOut;
	
	@Column(nullable = false)
	private String status;
	
   @Column(nullable = false)
	    private int guests;  // Số lượng khách
}
