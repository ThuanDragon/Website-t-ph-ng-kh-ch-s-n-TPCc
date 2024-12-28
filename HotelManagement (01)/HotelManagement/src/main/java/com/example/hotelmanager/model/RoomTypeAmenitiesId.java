package com.example.hotelmanager.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class RoomTypeAmenitiesId implements Serializable {

    @Column(name = "room_type_id")
    private Long roomTypeId;

    @Column(name = "amenity_id")
    private Long amenityId;

    // Constructor không tham số
    public RoomTypeAmenitiesId() {}

    // Constructor đầy đủ
    public RoomTypeAmenitiesId(Long roomTypeId, Long amenityId) {
        this.roomTypeId = roomTypeId;
        this.amenityId = amenityId;
    }

    // Ghi đè phương thức equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomTypeAmenitiesId that = (RoomTypeAmenitiesId) o;
        return Objects.equals(roomTypeId, that.roomTypeId) && 
               Objects.equals(amenityId, that.amenityId);
    }

    // Ghi đè phương thức hashCode
    @Override
    public int hashCode() {
        return Objects.hash(roomTypeId, amenityId);
    }
}
