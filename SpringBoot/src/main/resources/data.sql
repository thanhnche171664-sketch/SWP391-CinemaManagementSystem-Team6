-- ==========================================================
-- 1. XÓA DỮ LIỆU CŨ (Thứ tự: Bảng con trước -> Bảng cha sau)
-- ==========================================================
DELETE FROM NOTIFICATIONS;
DELETE FROM REVIEWS;
DELETE FROM BOOKING_SEATS;
DELETE FROM BOOKINGS;
DELETE FROM SHOWTIMES;
DELETE FROM ROOMS;
DELETE FROM USERS;             -- USERS tham chiếu CINEMA_BRANCHES nên xóa trước
DELETE FROM BRANCH_MOVIES;     -- phải xóa trước CINEMA_BRANCHES và MOVIES (FK)
DELETE FROM CINEMA_BRANCHES;
DELETE FROM MOVIES;

-- ==========================================================
-- 2. CHÈN BẢNG CHA (Các bảng không phụ thuộc ai)
-- ==========================================================

-- Chèn Chi nhánh trước để USERS và ROOMS có ID tham chiếu
INSERT INTO CINEMA_BRANCHES (BRANCH_ID, BRANCH_NAME, ADDRESS) VALUES
    (1, 'Cinema Center - Ha Noi', 'Cau Giay, Ha Noi');

-- Chèn Phim (dùng ID 1, 2 để /movies/1 hiển thị Lật Mặt 7)
INSERT INTO MOVIES (MOVIE_ID, TITLE, GENRE, DURATION, DESCRIPTION) VALUES
(1, 'Lật Mặt 7', 'Family', 138, 'Một bộ phim gia đình đầy cảm xúc.'),
(2, 'Mai', 'Drama', 131, NULL);

-- ==========================================================
-- 3. CHÈN BẢNG CON (Các bảng phụ thuộc)
-- ==========================================================

-- Chèn USERS (Lúc này BRANCH_ID = 1 đã tồn tại trong DB)
INSERT INTO USERS (FULL_NAME, EMAIL, PASSWORD_HASH, PHONE, ROLE, BRANCH_ID, STATUS, CREATED_AT) VALUES
-- Đội ngũ quản trị và nhân viên (có gắn chi nhánh)
('Nguyen Quang Anh', 'admin@cinema.com', '123456', '0912345678', 'ADMIN', 1, 'active', CURRENT_TIMESTAMP),
('Staff Member', 'staff@cinema.com', '123456', '0987654321', 'STAFF', 1, 'active', CURRENT_TIMESTAMP),

-- Khách hàng (BRANCH_ID là NULL)
('Le Khach Hang A', 'customerA@gmail.com', '123456', '0911223344', 'CUSTOMER', NULL, 'active', CURRENT_TIMESTAMP),
('Pham Khach Hang B', 'customerB@gmail.com', '123456', '0922334455', 'CUSTOMER', NULL, 'active', CURRENT_TIMESTAMP),
('Hoang Bi Khoa', 'blocked@gmail.com', '123456', '0933445566', 'CUSTOMER', NULL, 'inactive', CURRENT_TIMESTAMP);

-- Phòng chiếu (gắn chi nhánh 1)
INSERT INTO ROOMS (BRANCH_ID, ROOM_NAME, TOTAL_SEATS, STATUS) VALUES
(1, 'Phòng 1', 50, 'active'),
(1, 'Phòng 2', 60, 'active');

-- Suất chiếu (phim 1 = Lật Mặt 7, phòng 1 và 2; thời gian tương lai, status = open)
-- MySQL: END_TIME = START_TIME + 138 phút
INSERT INTO SHOWTIMES (MOVIE_ID, ROOM_ID, START_TIME, END_TIME, STATUS) VALUES
(1, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), INTERVAL 138 MINUTE), 'open'),
(1, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_ADD(DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 DAY), INTERVAL 138 MINUTE), 'open'),
(1, 2, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), INTERVAL 138 MINUTE), 'open');