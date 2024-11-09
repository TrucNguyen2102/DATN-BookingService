package com.business.booking_service.service;

import com.business.booking_service.dto.BookingRequest;
import com.business.booking_service.dto.BookingResponseDTO;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.entity.BookingTableId;
import com.business.booking_service.repository.BookingRepo;
import com.business.booking_service.repository.BookingTableRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService{
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private BookingTableRepo bookingTableRepo;

    @Autowired
    private RestTemplate restTemplate; // Dùng để gửi HTTP request đến Table Service




//    public void createBooking(BookingRequest bookingRequest) {
//        // Tạo một booking mới
//        Booking booking = new Booking();
//        LocalDateTime bookingTime = bookingRequest.getBookingTime();
//        booking.setBookingTime(bookingTime);
//
//        // Đặt expiryTime là null khi tạo mới
//        booking.setExpiryTime(null); // Đặt expiryTime là null
//
//        booking.setUserId(bookingRequest.getUserId());
//        booking.setStatus("Chờ Xác Nhận"); // Gán giá trị cho status
//
//        booking = bookingRepo.save(booking); // Lưu booking vào DB
//
//        // Lưu thông tin vào bảng booking_table
//        List<Integer> tableIds = bookingRequest.getTableIds();
//        if (tableIds == null || tableIds.isEmpty()) {
//            throw new IllegalArgumentException("No table IDs provided");
//        }
//
//        // Tạo danh sách BookingTable để gán cho Booking
//        List<BookingTable> bookingTables = new ArrayList<>();
//
//        for (Integer tableId : tableIds) {
//            BookingTable bookingTable = new BookingTable();
//            BookingTableId bookingTableId = new BookingTableId();
//            bookingTableId.setBookingId(booking.getId()); // Gán Booking ID
//            bookingTableId.setTableId(tableId); // Gán Table ID
//            bookingTable.setId(bookingTableId);
//
//            // Gán booking cho bookingTable
//            bookingTable.setBooking(booking); // Thiết lập mối quan hệ giữa BookingTable và Booking
//
//            // Log thông tin trước khi lưu
//            System.out.println("Saving BookingTable with Booking ID: " + booking.getId() + ", Table ID: " + tableId);
//
//            // Thêm bookingTable vào danh sách
//            bookingTables.add(bookingTable);
//        }
//
//        // Lưu tất cả các BookingTable cùng lúc
//        try {
//            bookingTableRepo.saveAll(bookingTables); // Lưu vào bảng booking_table
//        } catch (Exception e) {
//            System.err.println("Error saving booking tables. Error: " + e.getMessage());
//        }
//    }

    public void createBooking(BookingRequest bookingRequest) {
        LocalDateTime bookingTime = bookingRequest.getBookingTime();
        List<Integer> tableIds = bookingRequest.getTableIds();

        for (Integer tableId : tableIds) {
            // Kiểm tra xem bàn đã có đơn nào chờ xác nhận chưa
            boolean isPending = bookingTableRepo.existsByTableIdAndBooking_Status(tableId, "Chờ Xác Nhận");

            if (isPending) {
                throw new IllegalArgumentException(
                        "Bàn " + tableId + " hiện đang có khách đặt và chờ xác nhận. Vui lòng chọn bàn hoặc thời gian khác."
                );
            }
        }

        // Tạo một booking mới khi không có xung đột
        Booking booking = new Booking();
        booking.setBookingTime(bookingTime);
        booking.setExpiryTime(null); // Đặt expiryTime là null khi tạo mới
        booking.setUserId(bookingRequest.getUserId());
        booking.setStatus("Chờ Xác Nhận"); // Gán giá trị cho status

        booking = bookingRepo.save(booking); // Lưu booking vào DB

        // Tạo danh sách BookingTable để gán cho Booking
        List<BookingTable> bookingTables = new ArrayList<>();

        for (Integer tableId : tableIds) {
            BookingTable bookingTable = new BookingTable();
            BookingTableId bookingTableId = new BookingTableId();
            bookingTableId.setBookingId(booking.getId());
            bookingTableId.setTableId(tableId);
            bookingTable.setId(bookingTableId);
            bookingTable.setBooking(booking);

            // Thêm bookingTable vào danh sách
            bookingTables.add(bookingTable);
        }

        // Lưu tất cả các BookingTable cùng lúc
        bookingTableRepo.saveAll(bookingTables);
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

//    public boolean updateBookingStatus(Integer id, String status, LocalDateTime bookingTime) {
//        // Tính toán thời gian hết hạn (expiryTime)
//        LocalDateTime expiryTime = bookingTime.plusMinutes(15);
//
//        // Tìm kiếm booking theo id
//        Optional<Booking> optionalBooking = bookingRepo.findById(id);
//        if (optionalBooking.isPresent()) {
//            Booking booking = optionalBooking.get();
//            booking.setExpiryTime(expiryTime); //cập nhật thời gian hết hạn
//            booking.setStatus(status); // Cập nhật trạng thái
//            bookingRepo.save(booking); // Lưu thay đổi
//
//            // Lấy danh sách table_id liên quan đến booking
//            List<Integer> tableIds = bookingTableRepo.findTableIdsByBookingId(id);
//
//            // Gửi yêu cầu HTTP đến Table Service để cập nhật trạng thái bàn
//            updateTableStatusInTableService(tableIds, status);
//
//            return true;
//        }
//        return false; // Nếu không tìm thấy booking
//    }

    public boolean updateBookingStatus(Integer id, String status, LocalDateTime bookingTime) {
        // Tính toán thời gian hết hạn (expiryTime)
        LocalDateTime expiryTime = bookingTime.plusMinutes(15);

        // Tìm kiếm booking theo id
        Optional<Booking> optionalBooking = bookingRepo.findById(id);
//        if (!optionalBooking.isEmpty()) {
        if (optionalBooking.isEmpty()) { // Kiểm tra khi không tìm thấy booking
            System.out.println("Không tìm thấy booking với id: " + id);
            return false; // Nếu không tìm thấy booking
        }

        Booking booking = optionalBooking.get();
        booking.setExpiryTime(expiryTime); // Cập nhật thời gian hết hạn
        booking.setStatus(status); // Cập nhật trạng thái
        bookingRepo.save(booking); // Lưu thay đổi

        // Lấy danh sách table_id liên quan đến booking
        List<Integer> tableIds = bookingTableRepo.findTableIdsByBookingId(id);

        // Gửi yêu cầu HTTP đến Table Service để cập nhật trạng thái bàn
        return updateTableStatusInTableService(tableIds, status);


    }


    private boolean updateTableStatusInTableService(List<Integer> tableIds, String status) {
        String url = "http://localhost:9092/api/tables/update-status"; // URL của Table Service
        for (Integer tableId : tableIds) {
            // Tạo payload cho yêu cầu HTTP
            Map<String, Object> request = new HashMap<>();
            request.put("tableId", tableId);
            request.put("status", status);

            // Cấu hình headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            // Đóng gói dữ liệu vào HttpEntity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            try {
                // Gửi yêu cầu PUT và kiểm tra phản hồi
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    // Log hoặc xử lý lỗi nếu trạng thái không phải là OK
                    System.out.println("Lỗi cập nhật trạng thái bàn: " + response.getStatusCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Log hoặc xử lý lỗi khi không thể kết nối
                System.out.println("Lỗi kết nối với Table Service: " + e.getMessage());
                return false;
            }
        }
        return true;
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
