package com.business.booking_service.service;

import com.business.booking_service.repository.BookingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingTableServiceImpl implements BookingTableService{
    @Autowired
    private BookingRepo bookingRepo;
}
