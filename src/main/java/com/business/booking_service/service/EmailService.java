package com.business.booking_service.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
