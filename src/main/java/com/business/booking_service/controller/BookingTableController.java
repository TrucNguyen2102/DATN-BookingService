package com.business.booking_service.controller;

import com.business.booking_service.service.BookingTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/booking_table")
public class BookingTableController {
    @Autowired
    private BookingTableService bookingTableService;
}
