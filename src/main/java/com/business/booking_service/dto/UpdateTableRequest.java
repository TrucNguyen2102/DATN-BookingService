package com.business.booking_service.dto;

public class UpdateTableRequest {
    private Integer bookingId;

    private Integer tableId;

    private String status;

    public UpdateTableRequest() {

    }



        public UpdateTableRequest(Integer bookingId, Integer tableId, String status) {
        this.bookingId = bookingId;
        this.tableId = tableId;
        this.status = status;
    }

    public UpdateTableRequest(Integer tableId, String status) {
        this.tableId = tableId;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
