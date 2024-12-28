package com.example.hotelmanager.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "RoomType", schema = "dbo")
public class RoomType {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String name;
	
	@Column
	private String description;
	
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
	    name = "RoomTypeAmenities",
	    joinColumns = @JoinColumn(name = "room_type_id"),
	    inverseJoinColumns = @JoinColumn(name = "amenity_id")
	)
	private Set<Amenities> amenities;
	
	@OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<RoomTypeAmenities> roomTypeAmenities = new ArrayList<>();

}
