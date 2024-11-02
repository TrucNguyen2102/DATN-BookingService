package com.business.booking_service.service;

import com.business.booking_service.dto.BookingDTO;
import com.business.booking_service.dto.BookingRequest;
import com.business.booking_service.dto.BookingResponseDTO;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;

import java.util.List;

public interface BookingService {


    void createBooking(BookingRequest bookingRequest);

    List<BookingResponseDTO> getAllBookings();

    boolean updateBookingStatus(Integer id, String status);

    List<Booking> findByStatus(String status);
}
