//package com.business.booking_service.controller;
//
//import com.business.booking_service.dto.UpdateTableRequest;
//import com.business.booking_service.dto.UpdateTablesRequest;
//import com.business.booking_service.entity.Booking;
//import com.business.booking_service.entity.BookingTable;
//import com.business.booking_service.entity.BookingTableId;
//import com.business.booking_service.exception.ResourceNotFoundException;
//import com.business.booking_service.repository.BookingRepo;
//import com.business.booking_service.repository.BookingTableRepo;
//import com.business.booking_service.service.BookingTableService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/bookings")
//public class BookingTableController {
//    @Autowired
//    private BookingTableService bookingTableService;
//    @Autowired
//    private BookingTableRepo bookingTableRepo;
//
////    @Autowired
////    private BookingRepo bookingRepo;
////
////    @Autowired
////    private RestTemplate restTemplate; // Inject RestTemplate
////
////    @Autowired
////    @Value("${tablePlayService_url}")  // Lấy URL từ application.properties
////    private String tablePlayServiceUrl; // Địa chỉ URL của TablePlay Service
//
//
//
////    public BookingTableController(BookingTableService bookingTableService, BookingTableRepo bookingTableRepo, BookingRepo bookingRepo, RestTemplate restTemplate, @Value("${tablePlayService_url}")  String tablePlayServiceUrl) {
////        this.bookingTableService = bookingTableService;
////        this.bookingTableRepo = bookingTableRepo;
////        this.bookingRepo = bookingRepo;
////        this.restTemplate = restTemplate;
////        this.tablePlayServiceUrl = tablePlayServiceUrl;
////    }
//
//    @GetMapping("/booking_table/{bookingId}")
//    public ResponseEntity<List<BookingTable>> getTablesByBookingId(@PathVariable Integer bookingId) {
//        System.out.println("Received bookingId: " + bookingId);
//        List<BookingTable> tables = bookingTableService.getTablesByBookingId(bookingId);
//        System.out.println("Returned tables: " + tables);
//        System.out.println("Tables found: " + tables.size()); // Ghi log số lượng bàn chơi
//        return new ResponseEntity<>(tables, HttpStatus.OK);
//    }
//
//
//    // Endpoint để lấy số bàn được đặt nhiều nhất
//    @GetMapping("/booking_table/most-booked-tables")
//    public ResponseEntity<List<Object[]>> getMostBookedTables() {
//        try {
//            List<Object[]> mostBookedTables = bookingTableService.getMostBookedTables();
//            System.out.println("Most booked tables: " + Arrays.deepToString(mostBookedTables.toArray()));
//            return ResponseEntity.ok(mostBookedTables);
//        }catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//
//    }
//
//    @DeleteMapping("/booking_table/delete")
//    public ResponseEntity<String> deleteBookingTable(@RequestParam Integer bookingId, @RequestParam Integer tableId) {
//        try {
//            // Tạo BookingTableId từ bookingId và tableId
//            BookingTableId bookingTableId = new BookingTableId(bookingId, tableId);
//
//            // Kiểm tra xem bản ghi có tồn tại không
//            Optional<BookingTable> bookingTableOptional = bookingTableRepo.findById(bookingTableId);
//            if (bookingTableOptional.isPresent()) {
//                // Nếu bản ghi tồn tại, tiến hành xóa
//                bookingTableRepo.deleteById(bookingTableId);
//                return ResponseEntity.ok("Bàn đã được xóa khỏi đơn đặt bàn.");
//            } else {
//                // Nếu không tìm thấy bản ghi
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy bản ghi để xóa.");
//            }
//        } catch (Exception e) {
//            // Xử lý lỗi
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa bản ghi.");
//        }
//    }
//
//
//    @PutMapping("/booking_table/update-table-id")
//    public ResponseEntity<String> updateBookingTable(@RequestBody UpdateTableRequest request) {
//        try {
//            return bookingTableService.updateBookingTable(request);
//        }catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//
//    }
//
//    @PutMapping("/booking_table/update-tables")
//    public ResponseEntity<String> updateBookingTables(@RequestBody UpdateTablesRequest request) {
//        try {
//            return bookingTableService.updateBookingTables(request);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật thông tin booking.");
//        }
//    }
//
//    //lấy ds bàn thuộc booking
////    @GetMapping("/booking_table/{bookingId}/tables")
////    public ResponseEntity<?> getTableIdsByBookingId(@PathVariable Integer bookingId) {
////        try {
////            List<Integer> tableIds = bookingTableRepo.findTableIdsByBookingId(bookingId);
////            if (tableIds.isEmpty()) {
////                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tables found for bookingId: " + bookingId);
////            }
////            return ResponseEntity.ok(tableIds);
////        } catch (Exception e) {
////            e.printStackTrace();
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
////        }
////    }
//
////    @GetMapping("/booking_table/{bookingId}/tables")
////    public ResponseEntity<List<Integer>> getTableIdsByBookingId(@PathVariable Integer bookingId) {
////        try {
////            List<Integer> tableIds = bookingTableService.getTableIdsByBookingId(bookingId);
////            return ResponseEntity.ok(tableIds);
////        }catch (Exception e) {
////            e.printStackTrace();
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
////        }
////
////    }
//
//
//
//    @GetMapping("/{bookingId}/tables")
//    public ResponseEntity<List<Integer>> getTableIdsByBookingId(@PathVariable Integer bookingId) {
//        try {
//            System.out.println("Received bookingId: " + bookingId);
//            List<Integer> tableIds = bookingTableService.getTableIdsByBookingId(bookingId);
//            return ResponseEntity.ok(tableIds);
//        } catch (Exception e) {
//            e.printStackTrace();  // In ra chi tiết lỗi
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//
//
//
//
//
//
//
//}
