-- 1. Xóa dữ liệu cũ theo thứ tự (Showtimes -> Rooms -> Branches -> Users)
DELETE FROM SHOWTIMES;
DELETE FROM ROOMS;
DELETE FROM MOVIES;
DELETE FROM CINEMA_BRANCHES;
DELETE FROM USERS;

-- 2. Chèn USERS (Bỏ USER_ID để Database tự tăng)
-- Lưu ý: Status phải là 'active' (viết thường) giống hệt Enum UserStatus
INSERT INTO USERS (FULL_NAME, EMAIL, PASSWORD_HASH, PHONE, ROLE, STATUS, CREATED_AT) VALUES
                                                                                         ('Nguyen Quang Anh', 'admin@cinema.com', '123456', '0912345678', 'ADMIN', 'active', CURRENT_TIMESTAMP),
                                                                                         ('Staff Member', 'staff@cinema.com', '123456', '0987654321', 'STAFF', 'active', CURRENT_TIMESTAMP);

-- 3. Chèn CINEMA_BRANCHES
INSERT INTO CINEMA_BRANCHES (BRANCH_ID, BRANCH_NAME, ADDRESS) VALUES
    (1, 'Cinema Center - Ha Noi', 'Cau Giay, Ha Noi');

-- 4. Chèn MOVIES
INSERT INTO MOVIES (MOVIE_ID, TITLE, GENRE, DURATION) VALUES
                                                          (101, 'Lat Mat 7', 'Family', 138),
                                                          (102, 'Mai', 'Drama', 131);