package com.business.booking_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BookingTableId implements Serializable {
    @Column(name = "booking_id")
    private Integer bookingId;

    @Column(name = "table_id")
    private Integer tableId;

    public BookingTableId() {

    }

    public BookingTableId(Integer bookingId, Integer tableId) {
        this.bookingId = bookingId;
        this.tableId = tableId;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    // Override equals và hashCode để so sánh các khóa chính
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingTableId that = (BookingTableId) o;
        return Objects.equals(bookingId, that.bookingId) && Objects.equals(tableId, that.tableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, tableId);
    }
}
