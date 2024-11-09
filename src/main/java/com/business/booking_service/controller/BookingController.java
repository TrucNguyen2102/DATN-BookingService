package com.business.booking_service.controller;

import com.business.booking_service.dto.*;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.entity.BookingTableId;
import com.business.booking_service.service.BookingService;
import com.business.booking_service.service.BookingTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;



    @PostMapping("/add")
    public ResponseEntity<String> createBooking(@RequestBody BookingRequest bookingRequest) {
        try {
            bookingService.createBooking(bookingRequest);
            return new ResponseEntity<>("Đặt bàn thành công!", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Có lỗi xảy ra khi đặt bàn.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/all")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        try {
            List<BookingResponseDTO> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(bookings);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    @PutMapping("/update/{id}/status")
    public ResponseEntity<String> updateBookingStatus(@PathVariable Integer id, @RequestBody BookingStatusUpdateRequest request) {
        // Lấy thông tin booking hiện tại
        Optional<Booking> optionalCurrentBooking = bookingService.getBookingById(id);
        if (!optionalCurrentBooking.isPresent()) {
            return ResponseEntity.badRequest().body("Đơn đặt không tồn tại.");
        }

        Booking currentBooking = optionalCurrentBooking.get(); // Extract the Booking object

        boolean isUpdated = bookingService.updateBookingStatus(id, request.getStatus(), currentBooking.getBookingTime());
        if (isUpdated) {
            return ResponseEntity.ok("Trạng thái đơn đặt đã được cập nhật thành công.");
        } else {
            return ResponseEntity.badRequest().body("Cập nhật trạng thái không thành công.");
        }
    }

    @GetMapping("/history/{userId}")
    public List<BookingResponseDTO> getUserBookingHistory(@PathVariable Integer userId) {
        return bookingService.getUserBookingHistory(userId);
    }

    // API lấy số đơn trong ngày
    @GetMapping("/orders/today")
    public ResponseEntity<Map<String, Integer>> getOrdersToday(@RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        int count = bookingService.getOrdersToday(date);
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

//    @PutMapping("/update/{id}/status")
//    public ResponseEntity<String> updateBookingAndTableStatus(@PathVariable Integer id, @RequestBody TableStatusUpdateRequest request) {
//        boolean isUpdated = bookingService.updateTableStatusThroughBooking(id, request.getStatus());
//        if (isUpdated) {
//            return ResponseEntity.ok("Trạng thái bàn đã được cập nhật thành công.");
//        } else {
//            return ResponseEntity.badRequest().body("Cập nhật trạng thái bàn không thành công.");
//        }
//    }




}