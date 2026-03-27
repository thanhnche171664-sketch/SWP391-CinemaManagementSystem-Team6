-- ============================================================
--  SQL TEST SCRIPT: Booking Seat Concurrency Flow
--  Cinema Management System - Team 6
-- ============================================================
--  Mục đích: Test luồng đặt vé (Pessimistic Locking + Pending Booking)
--  Hướng dẫn: Chạy toàn bộ script 1 lần, theo thứ tự từ trên xuống
-- ============================================================

USE cinema_db;

-- ============================================================
--  B1: XOÁ HẾT DỮ LIỆU CŨ
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE booking_seats;
TRUNCATE TABLE bookings;
TRUNCATE TABLE payments;
TRUNCATE TABLE showtimes;
TRUNCATE TABLE seats;
TRUNCATE TABLE rooms;
TRUNCATE TABLE movies;
TRUNCATE TABLE branch_movies;
TRUNCATE TABLE promotions;
TRUNCATE TABLE notifications;
TRUNCATE TABLE reviews;
TRUNCATE TABLE news;
TRUNCATE TABLE movie_genres;
TRUNCATE TABLE genres;
TRUNCATE TABLE pricing;
TRUNCATE TABLE users;
TRUNCATE TABLE cinema_branches;

TRUNCATE TABLE otp_codes;
TRUNCATE TABLE verification_tokens;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
--  B2: SETUP DỮ LIỆU TEST
-- ============================================================

-- 2.1: Users
INSERT INTO users (user_id, full_name, email, password_hash, phone, role, branch_id, status) VALUES
(1,  'Admin System',      'admin@cinema.com',    '$2a$10$dummyhash00000000000000000000000000', '090000001', 'ADMIN',    NULL, 'active'),
(2,  'Manager Q1',         'manager.q1@cinema.com','$2a$10$dummyhash00000000000000000000000000', '090000002', 'MANAGER',  1,   'active'),
(3,  'Staff Q1',           'staff1.q1@cinema.com', '$2a$10$dummyhash00000000000000000000000000', '090000003', 'STAFF',    1,   'active'),
(4,  'Test User A',        'usera@test.com',       '$2a$10$dummyhash00000000000000000000000000', '090000100', 'CUSTOMER', NULL, 'active'),
(5,  'Test User B',        'userb@test.com',       '$2a$10$dummyhash00000000000000000000000000', '090000101', 'CUSTOMER', NULL, 'active'),
(6,  'Test User C',        'userc@test.com',       '$2a$10$dummyhash00000000000000000000000000', '090000102', 'CUSTOMER', NULL, 'active');

-- 2.2: Cinema Branches
INSERT INTO cinema_branches (branch_id, branch_name, city, address, status) VALUES
(1, 'FPT Cinema Quận 1',   'TP.HCM', '123 Đường Nguyễn Huệ, Quận 1',   'active'),
(2, 'FPT Cinema Quận 7',    'TP.HCM', '456 Đường Nguyễn Văn Linh, Q7',   'active');

-- 2.3: Rooms
INSERT INTO rooms (room_id, branch_id, room_name, total_seats, status) VALUES
(1, 1, 'Room 1 - VIP',   10, 'active'),
(2, 1, 'Room 2 - Normal', 10, 'active'),
(3, 2, 'Room 1 - VIP',    10, 'active');

-- 2.4: Seats (3 rooms x 10 seats = 30 seats)
-- Room 1 (seat_id 1-10): A1-A2 NORMAL, B1-B2 VIP, C1-C2 NORMAL, D1-D2 VIP, E1-E2 COUPLE
-- Room 2 (seat_id 11-20): same layout
-- Room 3 (seat_id 21-30): same layout
INSERT INTO seats (seat_id, room_id, seat_row, seat_number, seat_type) VALUES
-- Room 1
(1,  1, 'A', 1, 'NORMAL'), (2,  1, 'A', 2, 'NORMAL'),
(3,  1, 'B', 1, 'VIP'),    (4,  1, 'B', 2, 'VIP'),
(5,  1, 'C', 1, 'NORMAL'), (6,  1, 'C', 2, 'NORMAL'),
(7,  1, 'D', 1, 'VIP'),    (8,  1, 'D', 2, 'VIP'),
(9,  1, 'E', 1, 'COUPLE'), (10, 1, 'E', 2, 'COUPLE'),
-- Room 2
(11, 2, 'A', 1, 'NORMAL'), (12, 2, 'A', 2, 'NORMAL'),
(13, 2, 'B', 1, 'VIP'),   (14, 2, 'B', 2, 'VIP'),
(15, 2, 'C', 1, 'NORMAL'),(16, 2, 'C', 2, 'NORMAL'),
(17, 2, 'D', 1, 'VIP'),   (18, 2, 'D', 2, 'VIP'),
(19, 2, 'E', 1, 'COUPLE'),(20, 2, 'E', 2, 'COUPLE'),
-- Room 3
(21, 3, 'A', 1, 'NORMAL'),(22, 3, 'A', 2, 'NORMAL'),
(23, 3, 'B', 1, 'VIP'),   (24, 3, 'B', 2, 'VIP'),
(25, 3, 'C', 1, 'NORMAL'),(26, 3, 'C', 2, 'NORMAL'),
(27, 3, 'D', 1, 'VIP'),   (28, 3, 'D', 2, 'VIP'),
(29, 3, 'E', 1, 'COUPLE'),(30, 3, 'E', 2, 'COUPLE');

-- 2.5: Genres
INSERT INTO genres (genre_id, genre_name) VALUES
(1, 'Action'), (2, 'Comedy'), (3, 'Horror'),
(4, 'Sci-Fi'), (5, 'Romance'), (6, 'Drama'),
(7, 'Animation'), (8, 'Thriller');

-- 2.6: Movies
INSERT INTO movies (movie_id, title, duration, age_rating, description, poster_url, release_date, status, is_hidden) VALUES
(1, 'Avengers: Endgame',   181, '13', 'Superhero blockbuster',         'https://via.placeholder.com/300x450?text=Avengers',   '2026-03-01', 'now_showing', 0),
(2, 'Batman: The Dark Knight', 152, '16', 'DC superhero masterpiece',   'https://via.placeholder.com/300x450?text=Batman',   '2026-03-05', 'now_showing', 0),
(3, 'Inception',           148, '13', 'Mind-bending sci-fi thriller',    'https://via.placeholder.com/300x450?text=Inception', '2026-03-10', 'now_showing', 0),
(4, 'Avatar: The Way of Water', 192, '13', 'Sci-fi epic sequel',         'https://via.placeholder.com/300x450?text=Avatar',    '2026-04-01', 'upcoming',     0);

-- 2.7: Movie Genres
INSERT INTO movie_genres (movie_id, genre_id) VALUES
(1, 1), (1, 4),
(2, 1), (2, 6),
(3, 4), (3, 8),
(4, 4), (4, 7);

-- 2.8: Branch Movies (movie chiếu ở branch nào)
INSERT INTO branch_movies (branch_id, movie_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 3), (2, 4);

-- 2.9: Showtimes (2 suất chiếu trong tương lai)
INSERT INTO showtimes (showtime_id, movie_id, room_id, start_time, end_time, status) VALUES
-- Suất 1: Ngày mai 14:00-17:00 (Avengers, Room 1, Branch 1)
(1, 1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 14 HOUR,
             DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 17 HOUR, 'open'),
-- Suất 2: Ngày mai 19:00-22:00 (Batman, Room 1, Branch 1)
(2, 2, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 19 HOUR,
             DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 22 HOUR, 'open'),
-- Suất 3: Ngày kia 14:00-16:30 (Inception, Room 3, Branch 2)
(3, 3, 3, DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 14 HOUR,
             DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 16 HOUR, 'open');

-- 2.10: Pricing
INSERT INTO pricing (branch_id, seat_type, time_range, price) VALUES
(1, 'NORMAL', 'all_day', 70000),
(1, 'VIP',     'all_day', 120000),
(1, 'COUPLE',  'all_day', 200000),
(2, 'NORMAL', 'all_day', 65000),
(2, 'VIP',     'all_day', 110000),
(2, 'COUPLE',  'all_day', 180000);

-- 2.11: Promotions
INSERT INTO promotions (promotion_id, promo_code, description, discount_type, discount_value, min_booking_amount, start_date, end_date, branch_id, usage_limit, used_count, status) VALUES
(1, 'NEWCUSTOMER', 'Giảm 50k cho đơn từ 200k', 'amount', 50000, 200000, '2026-01-01', '2026-12-31', NULL, 100, 0, 'active'),
(2, 'VIP20',       'Giảm 20% cho khách VIP',    'percent', 20, 100000, '2026-01-01', '2026-12-31', NULL, 50,  0, 'active');

-- ============================================================
--  B3: XÁC NHẬN DỮ LIỆU ĐÃ SETUP
-- ============================================================
SELECT '=== B3.1: Users ===' AS '';
SELECT user_id, full_name, email, role FROM users;

SELECT '=== B3.2: Showtimes (dữ liệu test chính) ===' AS '';
SELECT s.showtime_id, m.title, r.room_name, b.branch_name, s.start_time, s.end_time, s.status
FROM showtimes s
JOIN movies m ON s.movie_id = m.movie_id
JOIN rooms r ON s.room_id = r.room_id
JOIN cinema_branches b ON r.branch_id = b.branch_id
ORDER BY s.showtime_id;

SELECT '=== B3.3: 30 ghế trong hệ thống ===' AS '';
SELECT s.seat_id, r.room_name, CONCAT(s.seat_row, s.seat_number) AS seat_label, s.seat_type
FROM seats s JOIN rooms r ON s.room_id = r.room_id
ORDER BY s.room_id, s.seat_row, s.seat_number;

SELECT '=== B3.4: Giá vé Branch 1 ===' AS '';
SELECT seat_type, price FROM pricing WHERE branch_id = 1;

-- ============================================================
--  B4: SHOWTIME 1 - TẤT CẢ GHẾ ĐỀU TRỐNG
-- ============================================================
SELECT '=== B4: Showtime 1 - Tất cả 10 ghế TRỐNG ===' AS '';
SELECT
    s.seat_id,
    CONCAT(s.seat_row, s.seat_number) AS seat_label,
    s.seat_type,
    IFNULL(b.booking_id, '---')   AS booking_id,
    IFNULL(b.status, 'CON_TRONG') AS trang_thai
FROM seats s
LEFT JOIN booking_seats bs ON bs.seat_id = s.seat_id
    AND bs.booking_id IN (
        SELECT booking_id FROM bookings
        WHERE showtime_id = 1 AND status IN ('pending', 'paid')
    )
LEFT JOIN bookings b ON bs.booking_id = b.booking_id
WHERE s.room_id = 1
ORDER BY s.seat_row, s.seat_number;

-- ============================================================
--  B5: KỊCH BẢN 1 - User A đặt ghế B1, B2 (PENDING)
-- ============================================================
INSERT INTO bookings (booking_id, user_id, showtime_id, booking_type, status, total_amount, booking_time)
VALUES (100, 4, 1, 'online', 'pending', 240000, NOW());

INSERT INTO booking_seats (booking_id, seat_id) VALUES
(100, 3),  -- B1 (VIP, 120k)
(100, 4);  -- B2 (VIP, 120k)

INSERT INTO payments (booking_id, method, amount, payment_status, payment_time)
VALUES (100, 'online', 240000, 'pending', NOW());

SELECT '=== B5.1: User A tạo PENDING booking thành công ===' AS '';
SELECT b.booking_id, u.full_name, b.status, b.total_amount, b.booking_time
FROM bookings b JOIN users u ON b.user_id = u.user_id
WHERE b.booking_id = 100;

SELECT '=== B5.2: Ghế B1, B2 đã bị giữ (PENDING) ===' AS '';
SELECT
    CONCAT(s.seat_row, s.seat_number) AS seat_label,
    s.seat_type,
    b.booking_id,
    b.status,
    u.full_name
FROM seats s
JOIN booking_seats bs ON bs.seat_id = s.seat_id
JOIN bookings b ON bs.booking_id = b.booking_id
JOIN users u ON b.user_id = u.user_id
WHERE s.room_id = 1 AND b.showtime_id = 1 AND b.status IN ('pending', 'paid')
ORDER BY s.seat_row, s.seat_number;

-- ============================================================
--  B6: KỊCH BẢN 2 - User B thử đặt ghế B1 (đã bị A giữ)
-- ============================================================
SELECT '=== B6.1: Xác nhận ghế B1 đang bị PENDING giữ ===' AS '';
SELECT
    CONCAT(s.seat_row, s.seat_number) AS seat_label,
    b.status       AS booking_status,
    u.full_name    AS nguoi_dat
FROM seats s
JOIN booking_seats bs ON bs.seat_id = s.seat_id
JOIN bookings b ON bs.booking_id = b.booking_id
JOIN users u ON b.user_id = u.user_id
WHERE s.seat_id = 3 AND b.showtime_id = 1 AND b.status = 'pending';

SELECT '=== B6.2: User B không thể đặt ghế B1 (sẽ báo lỗi nếu thử) ===' AS '';
SELECT
    CASE
        WHEN EXISTS (
            SELECT 1 FROM booking_seats bs
            JOIN bookings b ON bs.booking_id = b.booking_id
            WHERE bs.seat_id = 3 AND b.showtime_id = 1 AND b.status IN ('pending', 'paid')
        ) THEN 'KHOA - Ghe B1 dang bi chon boi User A (khong the dat)'
        ELSE 'MO - Ghe B1 con trong (co the dat)'
    END AS ghe_B1_status;

-- User B đặt ghế khác (C1 - còn trống)
INSERT INTO bookings (booking_id, user_id, showtime_id, booking_type, status, total_amount, booking_time)
VALUES (101, 5, 1, 'online', 'pending', 70000, NOW());
INSERT INTO booking_seats (booking_id, seat_id) VALUES (101, 5);  -- C1 (NORMAL)
INSERT INTO payments (booking_id, method, amount, payment_status, payment_time)
VALUES (101, 'online', 70000, 'pending', NOW());

SELECT '=== B6.3: User B đặt ghế C1 (còn trống) thành công ===' AS '';
SELECT b.booking_id, u.full_name, b.status, b.total_amount
FROM bookings b JOIN users u ON b.user_id = u.user_id WHERE b.booking_id = 101;

-- ============================================================
--  B7: KỊCH BẢN 3 - User A thanh toán THÀNH CÔNG (PAID)
-- ============================================================
UPDATE bookings SET status = 'paid' WHERE booking_id = 100;
UPDATE payments SET payment_status = 'success' WHERE booking_id = 100;

SELECT '=== B7.1: User A booking chuyển sang PAID ===' AS '';
SELECT b.booking_id, b.status AS booking_status, p.payment_status
FROM bookings b JOIN payments p ON p.booking_id = b.booking_id
WHERE b.booking_id = 100;

SELECT '=== B7.2: Ghế B1, B2 LOCK VĨNH VIỄN (paid) ===' AS '';
SELECT
    CONCAT(s.seat_row, s.seat_number) AS seat_label,
    b.booking_id,
    b.status AS booking_status,
    u.full_name
FROM seats s
JOIN booking_seats bs ON bs.seat_id = s.seat_id
JOIN bookings b ON bs.booking_id = b.booking_id
JOIN users u ON b.user_id = u.user_id
WHERE s.room_id = 1 AND b.showtime_id = 1 AND b.status IN ('pending', 'paid')
ORDER BY s.seat_row, s.seat_number;

-- ============================================================
--  B8: KỊCH BẢN 4 - User B hủy booking (chưa thanh toán)
-- ============================================================
UPDATE bookings SET status = 'cancelled' WHERE booking_id = 101;
UPDATE payments SET payment_status = 'cancelled' WHERE booking_id = 101;

SELECT '=== B8.1: User B booking bị CANCELLED ===' AS '';
SELECT b.booking_id, b.status, p.payment_status
FROM bookings b JOIN payments p ON p.booking_id = b.booking_id
WHERE b.booking_id = 101;

SELECT '=== B8.2: Ghế C1 đã TRỞ LẠI TRỐNG ===' AS '';
SELECT
    CONCAT(s.seat_row, s.seat_number) AS seat_label,
    IFNULL(b.booking_id, 'CON_TRONG') AS booking_id,
    IFNULL(b.status, 'CON_TRONG')     AS trang_thai
FROM seats s
LEFT JOIN booking_seats bs ON bs.seat_id = s.seat_id
    AND bs.booking_id IN (
        SELECT booking_id FROM bookings WHERE showtime_id = 1 AND status IN ('pending', 'paid')
    )
LEFT JOIN bookings b ON bs.booking_id = b.booking_id
WHERE s.seat_id = 5;

-- ============================================================
--  B9: KỊCH BẢN 5 - User C đặt ghế COUPLE (E1)
-- ============================================================
INSERT INTO bookings (booking_id, user_id, showtime_id, booking_type, status, total_amount, booking_time)
VALUES (102, 6, 1, 'online', 'pending', 200000, NOW());
INSERT INTO booking_seats (booking_id, seat_id) VALUES (102, 9);  -- E1 (COUPLE)
INSERT INTO payments (booking_id, method, amount, payment_status, payment_time)
VALUES (102, 'online', 200000, 'pending', NOW());
UPDATE bookings SET status = 'paid' WHERE booking_id = 102;
UPDATE payments SET payment_status = 'success' WHERE booking_id = 102;

SELECT '=== B9: Ghế E1 (COUPLE) đã LOCK vĩnh viễn ===' AS '';
SELECT
    CONCAT(s.seat_row, s.seat_number) AS seat_label,
    s.seat_type,
    b.booking_id,
    b.status
FROM seats s
JOIN booking_seats bs ON bs.seat_id = s.seat_id
JOIN bookings b ON bs.booking_id = b.booking_id
WHERE s.seat_id = 9;

-- ============================================================
--  B10: TỔNG KẾT TRẠNG THÁI SHOWTIME 1
-- ============================================================
SELECT '========================================' AS '';
SELECT '=== B10: TONG KET Showtime 1 ===' AS '';
SELECT '========================================' AS '';

SELECT
    CONCAT(s.seat_row, s.seat_number)                                             AS ghe,
    s.seat_type,
    CASE
        WHEN b.status = 'paid'     THEN 'DA_BAN (lock vinh vien)'
        WHEN b.status = 'pending'  THEN 'DANG_CHON (cho thanh toan)'
        WHEN b.status = 'cancelled' THEN 'TU_DONG_MO_LAI (booking bi huy)'
        ELSE                             'CON_TRONG'
    END AS trang_thai,
    CASE WHEN b.booking_id IS NOT NULL THEN u.full_name ELSE '---' END AS nguoi_dat,
    IFNULL(b.booking_id, '---') AS booking_id
FROM seats s
LEFT JOIN booking_seats bs ON bs.seat_id = s.seat_id
    AND bs.booking_id = (
        SELECT MAX(b2.booking_id)
        FROM booking_seats bs2
        JOIN bookings b2 ON bs2.booking_id = b2.booking_id
        WHERE bs2.seat_id = s.seat_id
          AND b2.showtime_id = 1
          AND b2.status IN ('pending', 'paid')
    )
LEFT JOIN bookings b ON bs.booking_id = b.booking_id
LEFT JOIN users u ON b.user_id = u.user_id
WHERE s.room_id = 1
ORDER BY s.seat_row, s.seat_number;

-- ============================================================
--  B11: KIỂM TRA getOccupiedSeatIdsForShowtime() trả về đúng seat_id
-- ============================================================
SELECT '=== B11: occupied seat IDs (B1,B2,E1) ===' AS '';
SELECT DISTINCT s.seat_id, CONCAT(s.seat_row, s.seat_number) AS seat_label
FROM seats s
WHERE s.room_id = 1
  AND CONCAT(s.seat_row, ':', s.seat_number) IN (
    SELECT DISTINCT CONCAT(seat.seat_row, ':', seat.seat_number)
    FROM booking_seats bs
    JOIN bookings b ON bs.booking_id = b.booking_id
    JOIN seats seat ON bs.seat_id = seat.seat_id
    WHERE b.showtime_id = 1 AND b.status IN ('pending', 'paid')
  )
ORDER BY s.seat_id;

-- ============================================================
--  HƯỚNG DẪN TEST THỦ CÔNG TRÊN GIAO DIỆN
-- ============================================================
/*
 Sau khi chạy script, truy cập: http://localhost:8080/booking/1

 Tài khoản test:
   usera@test.com    - User A
   userb@test.com    - User B
   userc@test.com    - User C

 TEST 1: User A đặt ghế trước
   - Đăng nhập usera@test.com
   - Truy cập /booking/1
   - Chọn ghế B1, B2 -> bấm "Thanh toán PayOS"
   - Quản lý DB: booking 100 = 'pending'
   - User B trên trình duyệt khác: sau ~10s polling thấy B1, B2 đổi màu occupied

 TEST 2: User A hủy thanh toán (chưa trả tiền)
   - Trên PayOS: bấm Hủy
   - Backend: booking 101 chuyển 'cancelled'
   - Trên DB: C1 trở về trống
   - User B polling thấy C1 trống lại

 TEST 3: User A thanh toán thành công
   - Trên PayOS: thanh toán thành công
   - Backend webhook: booking 100 = 'paid'
   - Ghế B1, B2 lock vĩnh viễn

 KẾT QUẢ MONG ĐỢI:
   ✓ Không xảy ra double-book cùng 1 ghế
   ✓ Pessimistic Lock ngăn race condition ở tầng DB
   ✓ Frontend polling cập nhật real-time trạng thái ghế
*/
