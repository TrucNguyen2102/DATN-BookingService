package com.business.booking_service.task;//package com.entertainment.booking_service.task;

import com.business.booking_service.entity.Booking;
import com.business.booking_service.repository.BookingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingCleanupTask {
    @Autowired
    private BookingRepo bookingRepo;

    @Scheduled(cron = "0 * * * * *") // Chạy mỗi phút
    public void cleanUpExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepo.findByStatus("Đã Xác Nhận");

        for (Booking booking : bookings) {
            LocalDateTime expiryTime = booking.getBookingTime().plusMinutes(15);
            if (now.isAfter(expiryTime)) {
                booking.setStatus("Đã Hủy");
                bookingRepo.save(booking);

            }
        }
    }
}
