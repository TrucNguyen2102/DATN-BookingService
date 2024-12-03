package com.business.booking_service.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private String content;
    private String notificationType;
    private LocalDateTime sendAt;
    private Integer bookingId;

    public NotificationDTO() {

    }

    public NotificationDTO(String content, String notificationType, LocalDateTime sendAt, Integer bookingId) {
        this.content = content;
        this.notificationType = notificationType;
        this.sendAt = sendAt;
        this.bookingId = bookingId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public LocalDateTime getSendAt() {
        return sendAt;
    }

    public void setSendAt(LocalDateTime sendAt) {
        this.sendAt = sendAt;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }
}
