package com.business.booking_service.repository;

import com.business.booking_service.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Integer> {
    Optional<Booking> findById(Integer id);

    List<Booking> findAll();

    List<Booking> findByStatus(String status);

//    List<Booking> findByUserId(Integer userId);
    Page<Booking> findByUserId(Integer userId, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Booking b WHERE DATE(b.bookingTime) = :date")
    int countOrdersToday(@Param("date") LocalDate date);

//    @Query("SELECT COUNT(b) FROM Booking b WHERE DATE(b.bookingTime) = :date")
//    int countTablesByDate(@Param("date") LocalDate date);
    @Query("SELECT COUNT(bt) FROM BookingTable bt JOIN bt.booking b WHERE DATE(b.bookingTime) = :date")
    int countTablesByDate(@Param("date") LocalDate date);

    // Tìm các Booking theo danh sách userId và trạng thái
    List<Booking> findByUserIdInAndStatus(List<Integer> userIds, String status);


    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status IN ('Chờ Xác Nhận', 'Đã Xác Nhận', 'Đã Nhận Bàn', 'Chưa Thanh Toán', 'Chờ Thanh Toán')")
    List<Booking> findActiveBookingsByUserId(@Param("userId") Integer userId);

    // Kiểm tra xem người dùng đã có đơn đặt nào chưa kết thúc (trong danh sách các trạng thái)
    boolean existsByUserIdAndStatusIn(Integer userId, List<String> statuses);
}
