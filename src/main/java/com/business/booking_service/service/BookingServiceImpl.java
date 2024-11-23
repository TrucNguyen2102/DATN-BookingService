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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Value("${tablePlayService_url}")
    private String TABLE_SERVICE_URL;

    public BookingServiceImpl(BookingRepo bookingRepo, BookingTableRepo bookingTableRepo, RestTemplate restTemplate, @Value("${tablePlayService_url}") String TABLE_SERVICE_URL) {
        this.bookingRepo = bookingRepo;
        this.bookingTableRepo = bookingTableRepo;
        this.restTemplate = restTemplate;
        this.TABLE_SERVICE_URL = TABLE_SERVICE_URL;
    }

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


        // Chỉ cập nhật trạng thái của bàn thành "Trống" nếu trạng thái booking là "Đã Thanh Toán"
        if ("Đã Thanh Toán".equals(status)) {
            updateTableStatusInTableService(tableIds, "Trống");
        }
        return true;


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


    public Page<BookingResponseDTO> getUserBookingHistory(Integer userId, Pageable pageable) {
        return bookingRepo.findByUserId(userId, pageable)
                .map(this::convertToDto); // Chuyển đổi mỗi Booking thành BookingResponseDTO
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


    // Kiểm tra tất cả bàn thuộc bookingId có trạng thái "Trống" không
    // Kiểm tra tất cả bàn thuộc bookingId có trạng thái "Trống" không
    public boolean checkAllTablesAreEmpty(Integer bookingId) {
        // Lấy tất cả các BookingTable thuộc bookingId
        List<BookingTable> bookingTables = bookingTableRepo.findByBookingId(bookingId);

        // Kiểm tra trạng thái bàn "Trống" qua API của Table Service
        for (BookingTable bookingTable : bookingTables) {
            Integer tableId = bookingTable.getTableId();  // Lấy tableId từ BookingTable
            String url = TABLE_SERVICE_URL + "/" + tableId + "/status";  // Gọi API của Table Service
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            // Nếu bàn có trạng thái khác "Trống", trả về false
            if (!"Trống".equals(response.getBody())) {
                return false;
            }
        }
        return true;  // Nếu tất cả bàn đều "Trống", trả về true
    }

    public boolean checkAllTablesArePaymentProcessing(Integer bookingId) {
        // Lấy tất cả các BookingTable thuộc bookingId
        List<BookingTable> bookingTables = bookingTableRepo.findByBookingId(bookingId);

        // Kiểm tra trạng thái bàn "Trống" qua API của Table Service
        for (BookingTable bookingTable : bookingTables) {
            Integer tableId = bookingTable.getTableId();  // Lấy tableId từ BookingTable
            String url = TABLE_SERVICE_URL + "/" + tableId + "/status";  // Gọi API của Table Service
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            System.out.println("Bàn ID: " + tableId + " - Trạng thái từ API: " + response.getBody());
            // Nếu bàn có trạng thái khác "Đang Xử Lý Thanh Toán", trả về false
            if (!"Đang Xử Lý Thanh Toán".equals(response.getBody())) {
                return false;
            }
        }
        return true;  // Nếu tất cả bàn đều "Trống", trả về true
    }

    // Cập nhật trạng thái booking thành "Đã Thanh Toán"
    public void updateBookingStatus(Integer bookingId, String status) {
        Optional<Booking> bookingOptional = bookingRepo.findById(bookingId);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            System.out.println("Trạng thái trước khi cập nhật: " + booking.getStatus());
            booking.setStatus(status);
            bookingRepo.save(booking);
            System.out.println("Đã cập nhật trạng thái bookingId " + bookingId + " thành: " + status);
        }
    }



}
