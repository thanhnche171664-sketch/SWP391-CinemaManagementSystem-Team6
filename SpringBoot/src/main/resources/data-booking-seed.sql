-- ============================================================
-- SEED DATA CHO LUỒNG ĐẶT VÉ XEM PHIM - CinemaHub
-- ============================================================
-- Cách chạy:
-- 1. Chạy ứng dụng Spring Boot một lần để Hibernate tạo/cập nhật bảng (ddl-auto: update).
-- 2. Chạy file SQL này:
--    - Cách A: Trong application.yml thêm (tạm thời):
--        spring.sql.init.mode: always
--        spring.sql.init.data-locations: classpath:data-booking-seed.sql
--      Rồi khởi động lại ứng dụng.
--    - Cách B: Chạy thủ công trong MySQL:
--        mysql -u root -p swp391 < src/main/resources/data-booking-seed.sql
--      Hoặc mở file trong MySQL Workbench / DBeaver và Execute.
-- 3. Bảng được đặt tên lowercase (cinema_branches, movies, ...). Nếu DB dùng tên khác, đổi lại cho khớp.
-- 4. QUAN TRỌNG: Chạy toàn bộ file từ đầu theo đúng thứ tự. Không chạy từng đoạn riêng lẻ
--    (users cần cinema_branches có sẵn; rooms cần branch_id; v.v.).
-- ============================================================

-- 1. XÓA DỮ LIỆU CŨ (bảng con trước, bảng cha sau)
-- ============================================================
DELETE FROM notifications;
DELETE FROM reviews;
DELETE FROM booking_seats;
DELETE FROM payments;
DELETE FROM bookings;
DELETE FROM showtimes;
DELETE FROM seats;
DELETE FROM rooms;
DELETE FROM pricing;
DELETE FROM users;
DELETE FROM branch_movies;   -- phải xóa trước cinema_branches và movies (FK)
DELETE FROM cinema_branches;
DELETE FROM movies;

-- 2. CHI NHÁNH RẠP (chèn rõ branch_id 1,2 để users/rooms/pricing tham chiếu đúng)
-- ============================================================
INSERT INTO cinema_branches (branch_id, branch_name, city, address, status) VALUES
(1, 'Cinema Center - Cầu Giấy', 'Hà Nội', 'Cầu Giấy, Hà Nội', 'active'),
(2, 'Cinema Center - Đống Đa', 'Hà Nội', 'Đống Đa, Hà Nội', 'active');

-- 3. PHIM (chèn rõ movie_id 1,2,3; status = now_showing để hiện ở trang Phim)
-- ============================================================
INSERT INTO movies (movie_id, title, genre, duration, description, status) VALUES
(1, 'Lật Mặt 7', 'Family', 138, 'Một bộ phim gia đình đầy cảm xúc.', 'now_showing'),
(2, 'Mai', 'Drama', 131, 'Hành trình tìm lại bản thân.', 'now_showing'),
(3, 'Làm Giàu Với Ma', 'Comedy', 110, 'Comedy đình đám.', 'now_showing');

-- 4. NGƯỜI DÙNG (branch_id NULL = khách hàng; email_verified = 1 để đăng nhập được)
-- ============================================================
INSERT INTO users (full_name, email, password_hash, phone, role, branch_id, status, created_at, email_verified) VALUES
-- Admin & nhân viên (gắn chi nhánh 1)
('Nguyễn Quang Anh', 'admin@cinema.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', '0912345678', 'ADMIN', 1, 'active', CURRENT_TIMESTAMP, 1),
('Nhân viên Rạp', 'staff@cinema.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', '0987654321', 'STAFF', 1, 'active', CURRENT_TIMESTAMP, 1),
-- Khách hàng (branch_id NULL, email_verified = 1)
('Lê Khách A', 'customerA@gmail.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', '0911223344', 'CUSTOMER', NULL, 'active', CURRENT_TIMESTAMP, 1),
('Phạm Khách B', 'customerB@gmail.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', '0922334455', 'CUSTOMER', NULL, 'active', CURRENT_TIMESTAMP, 1);
-- Mật khẩu mặc định cho tất cả: 123456 (bcrypt)

-- 5. PHÒNG CHIẾU (chèn rõ room_id 1,2 để seats và showtimes tham chiếu đúng)
-- ============================================================
INSERT INTO rooms (room_id, branch_id, room_name, total_seats, status) VALUES
(1, 1, 'Phòng 1', 24, 'active'),
(2, 1, 'Phòng 2', 24, 'active');

-- 6. GHẾ (phòng 1 & 2: 4 hàng x 6 ghế; hàng A,B = NORMAL, C = VIP)
-- ============================================================
-- Phòng 1: room_id = 1
INSERT INTO seats (room_id, seat_row, seat_number, seat_type) VALUES
(1, 'A', 1, 'NORMAL'), (1, 'A', 2, 'NORMAL'), (1, 'A', 3, 'NORMAL'), (1, 'A', 4, 'NORMAL'), (1, 'A', 5, 'NORMAL'), (1, 'A', 6, 'NORMAL'),
(1, 'B', 1, 'NORMAL'), (1, 'B', 2, 'NORMAL'), (1, 'B', 3, 'NORMAL'), (1, 'B', 4, 'NORMAL'), (1, 'B', 5, 'NORMAL'), (1, 'B', 6, 'NORMAL'),
(1, 'C', 1, 'VIP'),   (1, 'C', 2, 'VIP'),   (1, 'C', 3, 'VIP'),   (1, 'C', 4, 'VIP'),   (1, 'C', 5, 'VIP'),   (1, 'C', 6, 'VIP'),
(1, 'D', 1, 'NORMAL'), (1, 'D', 2, 'NORMAL'), (1, 'D', 3, 'NORMAL'), (1, 'D', 4, 'NORMAL'), (1, 'D', 5, 'NORMAL'), (1, 'D', 6, 'NORMAL');
-- Phòng 2: room_id = 2
INSERT INTO seats (room_id, seat_row, seat_number, seat_type) VALUES
(2, 'A', 1, 'NORMAL'), (2, 'A', 2, 'NORMAL'), (2, 'A', 3, 'NORMAL'), (2, 'A', 4, 'NORMAL'), (2, 'A', 5, 'NORMAL'), (2, 'A', 6, 'NORMAL'),
(2, 'B', 1, 'NORMAL'), (2, 'B', 2, 'NORMAL'), (2, 'B', 3, 'NORMAL'), (2, 'B', 4, 'NORMAL'), (2, 'B', 5, 'NORMAL'), (2, 'B', 6, 'NORMAL'),
(2, 'C', 1, 'VIP'),   (2, 'C', 2, 'VIP'),   (2, 'C', 3, 'VIP'),   (2, 'C', 4, 'VIP'),   (2, 'C', 5, 'VIP'),   (2, 'C', 6, 'VIP'),
(2, 'D', 1, 'NORMAL'), (2, 'D', 2, 'NORMAL'), (2, 'D', 3, 'NORMAL'), (2, 'D', 4, 'NORMAL'), (2, 'D', 5, 'NORMAL'), (2, 'D', 6, 'NORMAL');

-- 7. BẢNG GIÁ (chi nhánh 1: NORMAL, VIP; time_range có thể null hoặc 'weekday'/'weekend')
-- ============================================================
INSERT INTO pricing (branch_id, seat_type, time_range, price) VALUES
(1, 'NORMAL', 'weekday', 65000.00),
(1, 'NORMAL', 'weekend', 75000.00),
(1, 'VIP', 'weekday', 85000.00),
(1, 'VIP', 'weekend', 95000.00),
(2, 'NORMAL', 'weekday', 65000.00),
(2, 'VIP', 'weekday', 85000.00);

-- 8. SUẤT CHIẾU (phim 1 = Lật Mặt 7, phim 2 = Mai; thời gian tương lai; status = open)
-- ============================================================
-- Lật Mặt 7 (movie_id=1, 138 phút) - Phòng 1 & 2
INSERT INTO showtimes (movie_id, room_id, start_time, end_time, status) VALUES
(1, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), INTERVAL 138 MINUTE), 'open'),
(1, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_ADD(DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 138 MINUTE), 'open'),
(1, 2, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), INTERVAL 138 MINUTE), 'open');
-- Mai (movie_id=2, 131 phút)
INSERT INTO showtimes (movie_id, room_id, start_time, end_time, status) VALUES
(2, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), INTERVAL 131 MINUTE), 'open'),
(2, 2, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY), DATE_ADD(DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY), INTERVAL 131 MINUTE), 'open');

-- ============================================================
-- LUỒNG TEST ĐẶT VÉ
-- 1. Vào /movies -> chọn "Lật Mặt 7" -> /movies/1
-- 2. Chọn suất chiếu -> "Chọn ghế" -> /booking/select-seats?showtimeId=...
-- 3. Đăng nhập: customerA@gmail.com / 123456 (hoặc admin@cinema.com / 123456)
-- 4. Chọn ghế -> tạo booking -> thanh toán
-- ============================================================
