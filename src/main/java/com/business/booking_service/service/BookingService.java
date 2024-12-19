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

    List<Booking> findActiveBookingsByUserId(Integer userId);

    List<BookingResponseDTO> getAllBookings();

    Integer getUserIdByBookingId(Integer bookingId);

    List<Booking> searchBookings(String fullName, String phone, String status);

    boolean updateBookingStatus(Integer id, String status, LocalDateTime bookingTime);

    List<Booking> findByStatus(String status);

Page<BookingResponseDTO> getUserBookingHistory(Integer userId, Pageable pageable);

    int getOrdersToday(LocalDate date);

    Optional<Booking> getBookingById(Integer id);

    boolean checkAllTablesArePaying(Integer bookingId);

    boolean checkAllTablesAreEmpty(Integer bookingId);
    boolean checkAllTablesArePaymentProcessing(Integer bookingId);

    void updateBookingStatus(Integer bookingId, String status);

    boolean isTableBooked(Integer tableId);

    boolean checkAnyTableIsPlaying(Integer bookingId);
}
