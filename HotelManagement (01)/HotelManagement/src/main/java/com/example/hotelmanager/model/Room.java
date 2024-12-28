package com.example.hotelmanager.model;

import java.math.BigDecimal;

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
@Table(name = "Room")
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, unique = true)
	private Integer roomNumber;
	
	@ManyToOne 
	@JoinColumn(name = "room_type_id", nullable = false)
	private RoomType roomType;
	
	@Column(nullable = false)
	private String status;

    @Column(nullable = false)
    private int capacity;  

    @Column(nullable = false)
    private String location;  
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "image_url_2")
    private String imageUrl2;

    @Column(name = "image_url_3")
    private String imageUrl3;

    @Column(name = "image_url_4")
    private String imageUrl4;
    
    private String imagePath;
    
	@Column(nullable = false)
	private BigDecimal price;
}
