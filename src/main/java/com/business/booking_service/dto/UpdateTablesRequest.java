package com.business.booking_service.dto;

import java.util.List;

public class UpdateTablesRequest {
    private Integer bookingId;

    private List<Integer> oldTableIds;
    private List<Integer> newTableIds; // Danh sách các bàn mới

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public List<Integer> getOldTableIds() {
        return oldTableIds;
    }

    public void setOldTableIds(List<Integer> oldTableIds) {
        this.oldTableIds = oldTableIds;
    }

    public List<Integer> getNewTableIds() {
        return newTableIds;
    }

    public void setNewTableIds(List<Integer> newTableIds) {
        this.newTableIds = newTableIds;
    }
}
