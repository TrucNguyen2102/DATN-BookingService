package com.business.booking_service.controller;

import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.service.BookingTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingTableController {
    @Autowired
    private BookingTableService bookingTableService;

    @GetMapping("/booking_table/{bookingId}")
    public ResponseEntity<List<BookingTable>> getTablesByBookingId(@PathVariable Integer bookingId) {
        System.out.println("Received bookingId: " + bookingId);
        List<BookingTable> tables = bookingTableService.getTablesByBookingId(bookingId);
        System.out.println("Returned tables: " + tables);
        System.out.println("Tables found: " + tables.size()); // Ghi log số lượng bàn chơi
        return new ResponseEntity<>(tables, HttpStatus.OK);
    }

    // Endpoint để lấy số bàn được đặt nhiều nhất
    @GetMapping("/booking_table/most-booked-tables")
    public ResponseEntity<List<Object[]>> getMostBookedTables() {
        try {
            List<Object[]> mostBookedTables = bookingTableService.getMostBookedTables();
            System.out.println("Most booked tables: " + Arrays.deepToString(mostBookedTables.toArray()));
            return ResponseEntity.ok(mostBookedTables);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }
}
