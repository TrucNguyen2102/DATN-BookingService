package com.business.booking_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "user_id")
    private Integer userId; //Khóa ngoại của User trong user-service


    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<BookingTable> bookingTables; // Danh sách các bàn đặt

    public Booking() {

    }

    public Booking(Integer id) {
        this.id = id;

    }

        public Booking(Integer id, LocalDateTime bookingTime, LocalDateTime expiryTime, String status, Integer userId, List<BookingTable> bookingTables) {
        this.id = id;
        this.bookingTime = bookingTime;
        this.expiryTime = expiryTime;
        this.status = status;
        this.userId = userId;
        this.bookingTables = bookingTables;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<BookingTable> getBookingTables() {
        return bookingTables;
    }

    public void setBookingTables(List<BookingTable> bookingTables) {
        this.bookingTables = bookingTables;
    }
}
