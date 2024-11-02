package com.business.booking_service.entity;

import jakarta.persistence.*;

import java.io.Serializable;
@Entity
@Table(name = "booking_table")
public class BookingTable implements Serializable {

    @EmbeddedId
    private BookingTableId id;
    @ManyToOne
    @MapsId("bookingId")
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "table_id", insertable = false, updatable = false)
    private Integer tableId; // Khóa ngoại của table từ table-service


    public BookingTableId getId() {
        return id;
    }

    public void setId(BookingTableId id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }


}
