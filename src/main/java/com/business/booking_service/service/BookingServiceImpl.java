package com.business.booking_service.service;

import com.business.booking_service.dto.BookingDTO;
import com.business.booking_service.dto.BookingRequest;
import com.business.booking_service.dto.BookingResponseDTO;
import com.business.booking_service.dto.TablePlayDTO;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.entity.BookingTableId;
import com.business.booking_service.repository.BookingRepo;
import com.business.booking_service.repository.BookingTableRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService{
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private BookingTableRepo bookingTableRepo;


    public void createBooking(BookingRequest bookingRequest) {
        // Tạo một booking mới
        Booking booking = new Booking();
        LocalDateTime bookingTime = bookingRequest.getBookingTime();
        booking.setBookingTime(bookingTime);

//        // Tính toán expiryTime
//        LocalDateTime expiryTime = bookingTime.plusMinutes(15);
//        booking.setExpiryTime(expiryTime); // Giả sử bạn đã có thuộc tính expiryTime trong thực thể Booking

        // Đặt expiryTime là null khi tạo mới
        booking.setExpiryTime(null); // Đặt expiryTime là null

        booking.setUserId(bookingRequest.getUserId());
        booking.setStatus("Chờ Xác Nhận"); // Gán giá trị cho status

        booking = bookingRepo.save(booking); // Lưu booking vào DB

        // Lưu thông tin vào bảng booking_table
        List<Integer> tableIds = bookingRequest.getTableIds();
        if (tableIds == null || tableIds.isEmpty()) {
            throw new IllegalArgumentException("No table IDs provided");
        }

        // Tạo danh sách BookingTable để gán cho Booking
        List<BookingTable> bookingTables = new ArrayList<>();

        for (Integer tableId : tableIds) {
            BookingTable bookingTable = new BookingTable();
            BookingTableId bookingTableId = new BookingTableId();
            bookingTableId.setBookingId(booking.getId()); // Gán Booking ID
            bookingTableId.setTableId(tableId); // Gán Table ID
            bookingTable.setId(bookingTableId);

            // Gán booking cho bookingTable
            bookingTable.setBooking(booking); // Thiết lập mối quan hệ giữa BookingTable và Booking

            // Log thông tin trước khi lưu
            System.out.println("Saving BookingTable with Booking ID: " + booking.getId() + ", Table ID: " + tableId);

            // Thêm bookingTable vào danh sách
            bookingTables.add(bookingTable);
        }

        // Lưu tất cả các BookingTable cùng lúc
        try {
            bookingTableRepo.saveAll(bookingTables); // Lưu vào bảng booking_table
        } catch (Exception e) {
            System.err.println("Error saving booking tables. Error: " + e.getMessage());
        }
    }

    public List<BookingResponseDTO> getAllBookings() {
        List<Booking> bookings = bookingRepo.findAll();
        return bookings.stream().map(booking -> {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setId(booking.getId());
            dto.setBookingTime(booking.getBookingTime());
            dto.setExpiryTime(booking.getExpiryTime());
            dto.setStatus(booking.getStatus());
            dto.setUserId(booking.getUserId());

            // Lấy danh sách ID bàn từ bookingTables
            List<Integer> tableIds = booking.getBookingTables().stream()
                    .map(table -> table.getId().getTableId()) // Thay đổi tùy thuộc vào cách bạn cấu trúc ID
                    .collect(Collectors.toList());
            dto.setTableIds(tableIds);

            return dto;
        }).collect(Collectors.toList());
    }

    public boolean updateBookingStatus(Integer id, String status, LocalDateTime bookingTime) {
        // Tính toán thời gian hết hạn (expiryTime)
        LocalDateTime expiryTime = bookingTime.plusMinutes(15);

        // Tìm kiếm booking theo id
        Optional<Booking> optionalBooking = bookingRepo.findById(id);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            booking.setExpiryTime(expiryTime); //cập nhật thời gian hết hạn
            booking.setStatus(status); // Cập nhật trạng thái
            bookingRepo.save(booking); // Lưu thay đổi
            return true;
        }
        return false; // Nếu không tìm thấy booking
    }

    public List<Booking> findByStatus(String status) {
        return bookingRepo.findByStatus(status); // Tìm các booking theo trạng thái
    }

    public List<BookingResponseDTO> getUserBookingHistory(Integer userId) {
        List<Booking> bookings = bookingRepo.findByUserId(userId);
        return bookings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private BookingResponseDTO convertToDto(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setBookingTime(booking.getBookingTime());
        dto.setExpiryTime(booking.getExpiryTime());
        dto.setStatus(booking.getStatus());
        dto.setUserId(booking.getUserId());
        dto.setTableIdsFromBookingTables(booking.getBookingTables()); // Lấy danh sách ID bàn
        return dto;
    }

    // Lấy số đơn trong ngày
    public int getOrdersToday(LocalDate date) {
        return bookingRepo.countOrdersToday(date);
    }

    public Optional<Booking> getBookingById(Integer id) {
        return bookingRepo.findById(id);
    }

}
