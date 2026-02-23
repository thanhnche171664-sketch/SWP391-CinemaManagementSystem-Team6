-- ==========================================================
-- 1. XÓA DỮ LIỆU CŨ (Thứ tự: Bảng con trước -> Bảng cha sau)
-- ==========================================================
DELETE FROM NOTIFICATIONS;
DELETE FROM REVIEWS;
DELETE FROM BOOKINGS;
DELETE FROM SHOWTIMES;
DELETE FROM ROOMS;
DELETE FROM USERS;             -- USERS tham chiếu CINEMA_BRANCHES nên xóa trước
DELETE FROM CINEMA_BRANCHES;
DELETE FROM MOVIES;

-- ==========================================================
-- 2. CHÈN BẢNG CHA (Các bảng không phụ thuộc ai)
-- ==========================================================

-- Chèn Chi nhánh trước để USERS và ROOMS có ID tham chiếu
INSERT INTO CINEMA_BRANCHES (BRANCH_ID, BRANCH_NAME, ADDRESS) VALUES
    (1, 'Cinema Center - Ha Noi', 'Cau Giay, Ha Noi');

-- Chèn Phim
INSERT INTO MOVIES (MOVIE_ID, TITLE, GENRE, DURATION) VALUES
                                                          (101, 'Lat Mat 7', 'Family', 138),
                                                          (102, 'Mai', 'Drama', 131);

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