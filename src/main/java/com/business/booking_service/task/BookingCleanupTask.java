//package com.business.booking_service.task;//package com.entertainment.booking_service.task;
//
//import com.business.booking_service.dto.NotificationDTO;
//import com.business.booking_service.dto.TableUpdateStatus;
//import com.business.booking_service.dto.UpdateTableRequest;
//import com.business.booking_service.entity.Booking;
//import com.business.booking_service.entity.BookingTable;
//import com.business.booking_service.repository.BookingRepo;
//import com.business.booking_service.repository.BookingTableRepo;
//import com.business.booking_service.service.BookingService;
//import com.business.booking_service.service.EmailService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class BookingCleanupTask {
//    @Autowired
//    private BookingRepo bookingRepo;
//
//    @Autowired
//    private BookingService bookingService;
//    @Autowired
//    private BookingTableRepo bookingTableRepo;
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    @Autowired
//    @Value("${tablePlayService_url}")  // Lấy URL từ application.properties
//    private String tablePlayServiceUrl; // Địa chỉ URL của TablePlay Service
//
//    @Autowired
//    @Value("${userService_url}")
//    private String userServiceUrl;
//
//    @Value("${notificationService_url}")
//    private String NOTIFICATION_SERVICE_URL;
//
//    @Autowired
//    private EmailService emailService;
//
//    public BookingCleanupTask() {
//
//    }
//
//    public BookingCleanupTask(BookingRepo bookingRepo, BookingService bookingService, BookingTableRepo bookingTableRepo, RestTemplate restTemplate, @Value("${tablePlayService_url}") String tablePlayServiceUrl, @Value("${userService_url}") String userServiceUrl,  @Value("${notificationService_url}") String NOTIFICATION_SERVICE_URL, EmailService emailService) {
//        this.bookingRepo = bookingRepo;
//        this.bookingService = bookingService;
//        this.bookingTableRepo = bookingTableRepo;
//        this.restTemplate = restTemplate;
//        this.tablePlayServiceUrl = tablePlayServiceUrl;
//        this.userServiceUrl = userServiceUrl;
//        this.NOTIFICATION_SERVICE_URL = NOTIFICATION_SERVICE_URL;
//        this.emailService = emailService;
//    }
//
//
//
//    @Scheduled(cron = "0 * * * * *") // Chạy mỗi phút
//    public void cleanUpExpiredBookings() {
//        LocalDateTime now = LocalDateTime.now();
//        List<Booking> bookings = bookingRepo.findByStatus("Đã Xác Nhận");
//
//
//        //đã xác nhận nhưng hết tg đ
//        for (Booking booking : bookings) {
//            LocalDateTime expiryTime = booking.getBookingTime().plusMinutes(15);
//            if (now.isAfter(expiryTime)) {
//                booking.setStatus("Đã Hủy");
//                bookingRepo.save(booking);
//
//                // Lấy danh sách các BookingTable liên kết với booking để tìm bàn liên quan
//                List<BookingTable> bookingTables = bookingTableRepo.findByBooking(booking);
//
//                for (BookingTable bookingTable : bookingTables) {
//                    Integer tableId = bookingTable.getTableId();
//
//                    // Gửi yêu cầu đến Service Table để cập nhật trạng thái bàn
//                    String url = tablePlayServiceUrl + "/" + tableId + "/status";
//                    try {
//                        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(new TableUpdateStatus("Trống")), Void.class);
//                    } catch (RestClientException e) {
//                        // Xử lý lỗi khi không thể gửi yêu cầu đến Service Table
//                        e.printStackTrace();
//                    }
//                }
//
//                sendCancellationEmail(booking);
//
//            }
//
//        }
//
//
//    }
//
//    // Hàm gửi email thông báo hủy đơn
//    private void sendCancellationEmail(Booking booking) {
//
//        NotificationDTO notificationDTO = new NotificationDTO();
//        notificationDTO.setContent(
//                "Hệ thống tự động hủy đơn đặt bàn do đã hết thời gian giữ bàn.");
//        notificationDTO.setNotificationType("AUTO_CANCELLATION");
//        notificationDTO.setSendAt(LocalDateTime.now());
//        notificationDTO.setBookingId(booking.getId());
//
//        Integer userId = bookingService.getUserIdByBookingId(booking.getId());
//
//
//
//        if (userId != null) {
//            // Lấy email từ User Service
//            String user_url = userServiceUrl + "/" + userId + "/email";
//            String email = restTemplate.getForObject(user_url, String.class);
//
//            if (email != null) {
//
//                String subject = "TỰ ĐỘNG HỦY";
//                String body = "Hệ thống tự động hủy đơn đặt bàn do đã hết thời gian giữ bàn.\n\n" +
//                        "Nếu bạn muốn đặt bàn lại, vui lòng thực hiện đặt lại trên trang web của chúng tôi.\n\n" +
//                        "Xin lỗi vì sự bất tiện này và cảm ơn bạn đã hiểu.";
//
//
//
//                emailService.sendEmail(email, subject, body); // Gửi email
//            }
//        }
//
//        // Gửi yêu cầu tới Notification Service
//        try {
//            ResponseEntity<Void> response = restTemplate.postForEntity(NOTIFICATION_SERVICE_URL, notificationDTO, Void.class);
//            if (response.getStatusCode().is2xxSuccessful()) {
//                System.out.println("Notification sent successfully");
//            } else {
//                System.out.println("Failed to send notification");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Error sending notification");
//        }
//    }
//
//    @Scheduled(cron = "0 * * * * *") // Chạy mỗi phút
//    public void sendBookingReminders() {
//        LocalDateTime now = LocalDateTime.now();
//        List<Booking> bookings = bookingRepo.findByStatus("Đã Xác Nhận");
//
//        for (Booking booking : bookings) {
//            LocalDateTime reminderTime = booking.getBookingTime().minusMinutes(15);
//            if (now.isAfter(reminderTime) && now.isBefore(booking.getBookingTime())) {
//                sendReminderNotification(booking);
//            }
//        }
//    }
//
//    private void sendReminderNotification(Booking booking) {
//        NotificationDTO notificationDTO = new NotificationDTO();
//        notificationDTO.setContent(String.format(
//                "Nhắc nhở: Bạn có một đơn đặt bàn sắp tới. \n\n" +
//                        "Thời gian đặt bàn: %s\n" +
//                        "Bạn vui lòng chú ý thời gian.",
//                booking.getBookingTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
//        ));
//        notificationDTO.setNotificationType("BOOKING_REMINDER");
//        notificationDTO.setSendAt(LocalDateTime.now());
//        notificationDTO.setBookingId(booking.getId());
//
//        Integer userId = bookingService.getUserIdByBookingId(booking.getId());
//        if (userId != null) {
//            // Lấy email từ User Service
//            String userUrl = userServiceUrl + "/" + userId + "/email";
//            String email = restTemplate.getForObject(userUrl, String.class);
//
//            if (email != null) {
//                String subject = "Nhắc nhở đặt bàn";
//                String body = String.format(
//                        "Bạn có một đơn đặt bàn vào lúc: %s.\n" +
//                                "Hãy kiểm tra để đảm bảo không bỏ lỡ!\n\n" +
//                                "Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi.",
//                        booking.getBookingTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
//                );
//
//                emailService.sendEmail(email, subject, body); // Gửi email
//            }
//        }
//
//        // Gửi yêu cầu tới Notification Service
//        try {
//            ResponseEntity<Void> response = restTemplate.postForEntity(NOTIFICATION_SERVICE_URL, notificationDTO, Void.class);
//            if (response.getStatusCode().is2xxSuccessful()) {
//                System.out.println("Reminder notification sent successfully");
//            } else {
//                System.out.println("Failed to send reminder notification");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Error sending reminder notification");
//        }
//    }
//
//
//}


package com.business.booking_service.task;

import com.business.booking_service.dto.NotificationDTO;
import com.business.booking_service.dto.TableUpdateStatus;
import com.business.booking_service.entity.Booking;
import com.business.booking_service.entity.BookingTable;
import com.business.booking_service.repository.BookingRepo;
import com.business.booking_service.repository.BookingTableRepo;
import com.business.booking_service.service.BookingService;
import com.business.booking_service.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BookingCleanupTask {

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingTableRepo bookingTableRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Value("${tablePlayService_url}")
    private String tablePlayServiceUrl; // Địa chỉ URL của TablePlay Service

    @Autowired
    @Value("${userService_url}")
    private String userServiceUrl;

    @Value("${notificationService_url}")
    private String NOTIFICATION_SERVICE_URL;

    @Autowired
    private EmailService emailService;

    private Map<Integer, Boolean> notificationSentMap = new HashMap<>(); // Map để lưu trạng thái thông báo đã gửi

    // Đặt số lần hủy tối đa là 3
    private static final int MAX_CANCEL_LIMIT = 3;

    public BookingCleanupTask() {

    }
    public BookingCleanupTask(BookingRepo bookingRepo, BookingService bookingService, BookingTableRepo bookingTableRepo, RestTemplate restTemplate, String tablePlayServiceUrl, String userServiceUrl, String NOTIFICATION_SERVICE_URL, EmailService emailService, Map<Integer, Boolean> notificationSentMap) {
        this.bookingRepo = bookingRepo;
        this.bookingService = bookingService;
        this.bookingTableRepo = bookingTableRepo;
        this.restTemplate = restTemplate;
        this.tablePlayServiceUrl = tablePlayServiceUrl;
        this.userServiceUrl = userServiceUrl;
        this.NOTIFICATION_SERVICE_URL = NOTIFICATION_SERVICE_URL;
        this.emailService = emailService;
        this.notificationSentMap = notificationSentMap;
    }

    @Scheduled(cron = "0 * * * * *") // Chạy mỗi phút
    public void autoConfirmBookings() {
        LocalDateTime now = LocalDateTime.now();

        // Lấy danh sách các booking chưa xác nhận từ database
        List<Booking> bookings = bookingRepo.findByStatus("Chờ Xác Nhận");

        // Tự động cập nhật trạng thái thành "Đã Xác Nhận" nếu còn 5 phút trước giờ đặt
        for (Booking booking : bookings) {
            LocalDateTime autoConfirmTime = booking.getBookingTime().minusMinutes(5);
            LocalDateTime expiryTime = booking.getBookingTime().plusMinutes(15);
            if (now.isAfter(autoConfirmTime) && now.isBefore(booking.getBookingTime()) && !booking.getStatus().equals("Đã Xác Nhận")) {
                booking.setStatus("Đã Xác Nhận");
                booking.setExpiryTime(expiryTime);
                bookingRepo.save(booking); // Lưu trạng thái mới vào DB
                System.out.println("Đơn đặt bàn ID " + booking.getId() + " đã được tự động xác nhận.");

                // Lấy danh sách các BookingTable liên kết với booking để tìm bàn liên quan
                List<BookingTable> bookingTables = bookingTableRepo.findByBooking(booking);

                for (BookingTable bookingTable : bookingTables) {
                    Integer tableId = bookingTable.getTableId();

                    // Gửi yêu cầu đến Service Table để cập nhật trạng thái bàn
                    String url = tablePlayServiceUrl + "/" + tableId + "/status";
                    try {
                        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(new TableUpdateStatus("Đã Đặt")), Void.class);
                    } catch (RestClientException e) {
                        // Xử lý lỗi khi không thể gửi yêu cầu đến Service Table
                        e.printStackTrace();
                    }
                }

                // Gửi thông báo nhắc nhở hoặc cập nhật trạng thái
                sendAutoConfirmationNotification(booking);
            }
        }
    }


    private void sendAutoConfirmationNotification(Booking booking) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setContent("Chào bạn, đơn đặt bàn của bạn đã được xác nhận.");
        notificationDTO.setNotificationType("BOOKING_CONFIRMATION");
        notificationDTO.setSendAt(LocalDateTime.now());
        notificationDTO.setBookingId(booking.getId());

        Integer userId = bookingService.getUserIdByBookingId(booking.getId());
        if (userId != null) {
            // Lấy email từ User Service
            String userUrl = userServiceUrl + "/" + userId + "/email";
            try {
                String email = restTemplate.getForObject(userUrl, String.class);

                if (email != null) {
                    String subject = "XÁC NHẬN ĐẶT BÀN";
                    String body = String.format(
                            "Chào bạn, đơn đặt bàn của bạn đã được xác nhận. \n\n" +
                                    "Thời gian xác nhận: %s\n" +
                                    "Thời gian đặt bàn: %s\n" +
                                    "Chúng tôi sẽ giữ bàn trong 15 phút kể từ thời gian đặt. Nếu quá thời gian, đơn đặt sẽ bị hủy. \n\n" +
                                    "Bạn vui lòng chú ý thời gian.",
                            booking.getBookingTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                    );

                    emailService.sendEmail(email, subject, body);
                    System.out.println("Đã gửi email tự động xác nhận cho: " + email);
                }
            } catch (Exception e) {
                System.out.println("Không thể gửi email xác nhận: " + e.getMessage());
            }
        }

        // Gửi thông báo tới Notification Service
        try {
            restTemplate.postForEntity(NOTIFICATION_SERVICE_URL, notificationDTO, Void.class);
            System.out.println("Đã gửi thông báo xác nhận thành công.");
        } catch (Exception e) {
            System.out.println("Lỗi khi gửi thông báo xác nhận: " + e.getMessage());
        }
    }




    @Scheduled(cron = "0 * * * * *") // Chạy mỗi phút
    public void cleanUpExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepo.findByStatus("Đã Xác Nhận");

        // Đã xác nhận nhưng hết thời gian đến nhận bàn thì bị hủy
        for (Booking booking : bookings) {
            LocalDateTime expiryTime = booking.getBookingTime().plusMinutes(15);
            if (now.isAfter(expiryTime)) {
                booking.setStatus("Đã Hủy");
                bookingRepo.save(booking);

                // Lấy danh sách các BookingTable liên kết với booking để tìm bàn liên quan
                List<BookingTable> bookingTables = bookingTableRepo.findByBooking(booking);

                for (BookingTable bookingTable : bookingTables) {
                    Integer tableId = bookingTable.getTableId();

                    // Gửi yêu cầu đến Service Table để cập nhật trạng thái bàn
                    String url = tablePlayServiceUrl + "/" + tableId + "/status";
                    try {
                        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(new TableUpdateStatus("Trống")), Void.class);
                    } catch (RestClientException e) {
                        // Xử lý lỗi khi không thể gửi yêu cầu đến Service Table
                        e.printStackTrace();
                    }
                }



                sendCancellationEmail(booking);

                Integer userId = bookingService.getUserIdByBookingId(booking.getId());
                if (userId != null) {
                    // Kiểm tra xem người dùng đã hủy vượt quá số lần cho phép chưa
                    if (hasExceededCancelLimit(userId)) {
                        sendPolicyViolationNotification(userId, booking);
//                        lockUserAccount(userId); // Tạm khóa tài khoản nếu vượt quá số lần hủy
//
//                        // Gọi API mở khóa tài khoản sau 3 ngày (nếu bị khóa)
//                        unlockUserAccountIfExpired(userId);
                    }
                }
            }
        }

        // Gửi nhắc nhở cho các đơn đặt bàn trong khoảng thời gian 15 phút trước giờ đặt bàn
        for (Booking booking : bookings) {
            LocalDateTime reminderTime = booking.getBookingTime().minusMinutes(15);
            if (now.isAfter(reminderTime) && now.isBefore(booking.getBookingTime()) && !notificationSentMap.containsKey(booking.getId())) {
                sendReminderNotification(booking);
                notificationSentMap.put(booking.getId(), true); // Đánh dấu thông báo đã gửi
            }
        }
    }

    // Phương thức gửi thông báo vi phạm chính sách
    private void sendPolicyViolationNotification(Integer userId, Booking booking) {
        // Tạo DTO cho Notification Service
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setContent("Tài khoản của bạn đã bị tạm khóa do vi phạm chính sách đặt bàn.");
        notificationDTO.setNotificationType("POLICY_VIOLATION");  // Loại thông báo
        notificationDTO.setSendAt(LocalDateTime.now());
        notificationDTO.setBookingId(booking.getId());

        System.out.println("Sending notification to Notification Service with data: " + notificationDTO);

        // Gửi yêu cầu tới Notification Service
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(NOTIFICATION_SERVICE_URL, notificationDTO, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Notification sent successfully");
            } else {
                System.out.println("Failed to send notification. Response: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending notification: " + e.getMessage());
        }

        // Thêm chức năng gửi email
        if (userId != null) {
            try {
                // Lấy email từ User Service
                String userUrl = userServiceUrl + "/" + userId + "/email";
                String email = restTemplate.getForObject(userUrl, String.class);

                if (email != null) {
                    // Chuẩn bị nội dung email
                    String subject = "VI PHẠM CHÍNH SÁCH ĐẶT BÀN";
                    String body = "Chào bạn, tài khoản của bạn bị khóa do vi phạm chính sách đặt bàn của chúng tôi. \n\n" +
                            "Bạn có thể tiếp tục sử dụng dịch vụ sau 3 ngày. \n\n" +
                            "Xin lỗi và cảm ơn bạn đã hiểu.";

                    // Gửi email
                    emailService.sendEmail(email, subject, body);
                    System.out.println("Email sent successfully to: " + email);
                } else {
                    System.out.println("No email found for user ID: " + userId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error fetching email or sending email: " + e.getMessage());
            }
        }

        lockUserAccount(userId);
        unlockUserAccountIfExpired(userId);
    }


    // Kiểm tra số lần hủy của người dùng
    private boolean hasExceededCancelLimit(Integer userId) {
        List<Booking> canceledBookings = bookingRepo.findByUserIdAndStatus(userId, "Đã Hủy");
        return canceledBookings.size() > MAX_CANCEL_LIMIT;
    }

    private void lockUserAccount(Integer userId) {
        // Gửi yêu cầu tới User Service để khóa tài khoản
        String url =  userServiceUrl + "/" + userId + "/lock";
        try {
            restTemplate.put(url, null);
            System.out.println("Tài khoản đã bị khóa do vi phạm chính sách.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Không thể khóa tài khoản.");
        }
    }

    private void unlockUserAccountIfExpired(Integer userId) {
        // Gửi yêu cầu tới User Service để mở khóa tài khoản nếu đã hết 3 ngày
        String url = userServiceUrl + "/" + userId + "/unlock";
        try {
            restTemplate.put(url, null);
            System.out.println("Tài khoản đã được mở lại.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Không thể mở lại tài khoản.");
        }
    }




    private void sendCancellationEmail(Booking booking) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setContent("Hệ thống tự động hủy đơn đặt bàn do đã hết thời gian giữ bàn.");
        notificationDTO.setNotificationType("AUTO_CANCELLATION");
        notificationDTO.setSendAt(LocalDateTime.now());
        notificationDTO.setBookingId(booking.getId());

        Integer userId = bookingService.getUserIdByBookingId(booking.getId());
        if (userId != null) {
            // Lấy email từ User Service
            String user_url = userServiceUrl + "/" + userId + "/email";
            String email = restTemplate.getForObject(user_url, String.class);

            if (email != null) {
                String subject = "TỰ ĐỘNG HỦY";
                String body = "Hệ thống tự động hủy đơn đặt bàn do đã hết thời gian giữ bàn.\n\n" +
                        "Nếu bạn muốn đặt bàn lại, vui lòng thực hiện đặt lại trên trang web của chúng tôi.\n\n" +
                        "Xin lỗi vì sự bất tiện này và cảm ơn bạn đã hiểu.";

                emailService.sendEmail(email, subject, body); // Gửi email
            }
        }

        // Gửi yêu cầu tới Notification Service
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(NOTIFICATION_SERVICE_URL, notificationDTO, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Notification sent successfully");
            } else {
                System.out.println("Failed to send notification");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending notification");
        }
    }

    private void sendReminderNotification(Booking booking) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setContent("Nhắc nhở: Bạn đang có một đơn đặt bàn sắp tới.");
        notificationDTO.setNotificationType("BOOKING_REMINDER");
        notificationDTO.setSendAt(LocalDateTime.now());
        notificationDTO.setBookingId(booking.getId());

        Integer userId = bookingService.getUserIdByBookingId(booking.getId());
        if (userId != null) {
            // Lấy email từ User Service
            String userUrl = userServiceUrl + "/" + userId + "/email";
            String email = restTemplate.getForObject(userUrl, String.class);

            if (email != null) {
                String subject = "NHẮC NHỞ ĐẶT BÀN";
                String body = String.format(
                        "Bạn có một đơn đặt bàn vào lúc: %s.\n" +
                                "Hãy kiểm tra để đảm bảo không bỏ lỡ!\n\n" +
                                "Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi.",
                        booking.getBookingTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                );

                emailService.sendEmail(email, subject, body); // Gửi email
            }
        }

        // Gửi yêu cầu tới Notification Service
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(NOTIFICATION_SERVICE_URL, notificationDTO, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Reminder notification sent successfully");
            } else {
                System.out.println("Failed to send reminder notification");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending reminder notification");
        }
    }
}

