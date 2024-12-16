package com.business.booking_service.dto;

import java.util.List;

public class GuestBookingRequest {
    private String fullName;
    private String phone;

    private List<Integer> tableIds;  // Danh sách các tableId được chọn



    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Integer> getTableIds() {
        return tableIds;
    }

    public void setTableIds(List<Integer> tableIds) {
        this.tableIds = tableIds;
    }
}
