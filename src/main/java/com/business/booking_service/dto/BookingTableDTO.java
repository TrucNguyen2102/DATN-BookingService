package com.business.booking_service.dto;

import java.util.List;

public class BookingTableDTO {
    private Integer bookingId;
    private List<Integer> tableIds;

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public List<Integer> getTableIds() {
        return tableIds;
    }

    public void setTableIds(List<Integer> tableIds) {
        this.tableIds = tableIds;
    }
}
