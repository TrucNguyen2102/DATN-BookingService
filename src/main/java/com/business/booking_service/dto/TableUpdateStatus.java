package com.business.booking_service.dto;

public class TableUpdateStatus {
    private String tableStatus;

    public TableUpdateStatus(String tableStatus) {
        this.tableStatus = tableStatus;
    }

    public String getTableStatus() {
        return tableStatus;
    }

    public void setTableStatus(String tableStatus) {
        this.tableStatus = tableStatus;
    }
}
