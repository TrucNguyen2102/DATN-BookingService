package com.business.booking_service.service;

import com.business.booking_service.dto.BookingDTO;
import com.business.booking_service.dto.BookingRequest;
import com.business.booking_service.dto.BookingResponseDTO;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingService {


    void createBooking(BookingRequest bookingRequest);

    List<BookingResponseDTO> getAllBookings();

    boolean updateBookingStatus(Integer id, String status, LocalDateTime bookingTime);

    List<Booking> findByStatus(String status);

    List<BookingResponseDTO> getUserBookingHistory(Integer userId);

    int getOrdersToday(LocalDate date);

    Optional<Booking> getBookingById(Integer id);
}
