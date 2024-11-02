package com.business.booking_service.dto;

public class TablePlayDTO {
    private Integer id;
    private String tableNum; // Số bàn
    private String tableStatus; // Trạng thái bàn
    private String type;

    public TablePlayDTO() {

    }

    public TablePlayDTO(Integer id, String tableNum, String tableStatus, String type) {
        this.id = id;
        this.tableNum = tableNum;
        this.tableStatus = tableStatus;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTableNum() {
        return tableNum;
    }

    public void setTableNum(String tableNum) {
        this.tableNum = tableNum;
    }

    public String getTableStatus() {
        return tableStatus;
    }

    public void setTableStatus(String tableStatus) {
        this.tableStatus = tableStatus;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
