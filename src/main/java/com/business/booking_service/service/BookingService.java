package com.business.booking_service.service;

import com.business.booking_service.dto.BookingDTO;
import com.business.booking_service.dto.BookingRequest;
import com.business.booking_service.dto.BookingResponseDTO;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingService {

    void createBooking(BookingRequest bookingRequest);

    List<BookingResponseDTO> getAllBookings();

    boolean updateBookingStatus(Integer id, String status, LocalDateTime bookingTime);

    List<Booking> findByStatus(String status);

Page<BookingResponseDTO> getUserBookingHistory(Integer userId, Pageable pageable);

    int getOrdersToday(LocalDate date);

    Optional<Booking> getBookingById(Integer id);
}
