package com.business.booking_service.entity;

import jakarta.persistence.*;

import java.io.Serializable;
@Entity
@Table(name = "booking_table")
public class BookingTable implements Serializable {
    @ManyToOne
    @MapsId("bookingId")
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @MapsId("tableId")
    @JoinColumn(name = "table_id")
    private Integer tableId; //khóa ngoại của table từ table-service

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
