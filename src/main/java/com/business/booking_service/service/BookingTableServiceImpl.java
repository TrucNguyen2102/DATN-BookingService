package com.business.booking_service.service;

import com.business.booking_service.dto.TablePlayDTO;
import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.repository.BookingTableRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingTableServiceImpl implements BookingTableService{
    @Autowired
    private BookingTableRepo bookingTableRepo;
    public List<BookingTable> getTablesByBookingId(Integer bookingId) {
        return bookingTableRepo.findByBookingId(bookingId);
    }

    public List<Object[]> getMostBookedTables() {
        return bookingTableRepo.findMostBookedTables();
    }
}
