-- Seed data for booking seat flow
-- Run once on empty DB. Enable: spring.sql.init.mode: always (then set to never to avoid re-running).
-- Seed customer: email = customer@test.com, password = password (BCrypt hash below)

-- Genres
INSERT IGNORE INTO genres (genre_name, description, status) VALUES
('Action', 'Action movies', 'active'),
('Comedy', 'Comedy movies', 'active'),
('Drama', 'Drama movies', 'active');

-- Cinema branches
INSERT INTO cinema_branches (branch_name, city, address, status) VALUES
('Cinema HCM', 'Ho Chi Minh', '123 Nguyen Hue, Q1', 'active'),
('Cinema Hanoi', 'Hanoi', '45 Tran Hung Dao, Hoan Kiem', 'active');

-- Users (customer for booking; BCrypt for "password")
INSERT INTO users (full_name, email, password_hash, phone, role, branch_id, status, is_verify, created_at) VALUES
('Test Customer', 'customer@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901234567', 'CUSTOMER', NULL, 'active', 1, NOW())
ON DUPLICATE KEY UPDATE email = email;

-- Movies
INSERT INTO movies (title, duration, age_rating, description, poster_url, trailer_url, status, is_hidden) VALUES
('The Sample Movie', 120, 'P', 'A sample movie for testing booking flow.', '/images/poster1.jpg', NULL, 'now_showing', 0),
('Another Show', 95, 'T', 'Another movie for showtimes.', NULL, NULL, 'now_showing', 0);

-- Movie-Genre links
INSERT IGNORE INTO movie_genres (movie_id, genre_id) VALUES (1, 1), (1, 2), (2, 2);

-- Branch-Movie (movie available at branch)
INSERT IGNORE INTO branch_movies (branch_id, movie_id) VALUES (1, 1), (1, 2), (2, 1);

-- Rooms (branch 1)
INSERT INTO rooms (branch_id, room_name, total_seats, status) VALUES
(1, 'Room A', 48, 'active'),
(1, 'Room B', 36, 'active');

-- Seats for Room A: 6 rows x 8 seats (A1-A8 VIP, B-E NORMAL, F1-F8 COUPLE)
INSERT INTO seats (room_id, seat_row, seat_number, seat_type) VALUES
(1,'A',1,'VIP'),(1,'A',2,'VIP'),(1,'A',3,'VIP'),(1,'A',4,'VIP'),(1,'A',5,'VIP'),(1,'A',6,'VIP'),(1,'A',7,'VIP'),(1,'A',8,'VIP'),
(1,'B',1,'NORMAL'),(1,'B',2,'NORMAL'),(1,'B',3,'NORMAL'),(1,'B',4,'NORMAL'),(1,'B',5,'NORMAL'),(1,'B',6,'NORMAL'),(1,'B',7,'NORMAL'),(1,'B',8,'NORMAL'),
(1,'C',1,'NORMAL'),(1,'C',2,'NORMAL'),(1,'C',3,'NORMAL'),(1,'C',4,'NORMAL'),(1,'C',5,'NORMAL'),(1,'C',6,'NORMAL'),(1,'C',7,'NORMAL'),(1,'C',8,'NORMAL'),
(1,'D',1,'NORMAL'),(1,'D',2,'NORMAL'),(1,'D',3,'NORMAL'),(1,'D',4,'NORMAL'),(1,'D',5,'NORMAL'),(1,'D',6,'NORMAL'),(1,'D',7,'NORMAL'),(1,'D',8,'NORMAL'),
(1,'E',1,'NORMAL'),(1,'E',2,'NORMAL'),(1,'E',3,'NORMAL'),(1,'E',4,'NORMAL'),(1,'E',5,'NORMAL'),(1,'E',6,'NORMAL'),(1,'E',7,'NORMAL'),(1,'E',8,'NORMAL'),
(1,'F',1,'COUPLE'),(1,'F',2,'COUPLE'),(1,'F',3,'COUPLE'),(1,'F',4,'COUPLE'),(1,'F',5,'COUPLE'),(1,'F',6,'COUPLE'),(1,'F',7,'COUPLE'),(1,'F',8,'COUPLE');

-- Pricing (branch 1: weekday / weekend by seat type)
INSERT INTO pricing (branch_id, seat_type, time_range, price) VALUES
(1, 'NORMAL', 'weekday', 65000.00),
(1, 'NORMAL', 'weekend', 85000.00),
(1, 'VIP', 'weekday', 95000.00),
(1, 'VIP', 'weekend', 120000.00),
(1, 'COUPLE', 'weekday', 120000.00),
(1, 'COUPLE', 'weekend', 150000.00);

-- Showtimes (movie 1 in room 1: today 14:00 & 19:00, tomorrow 14:00)
INSERT INTO showtimes (movie_id, room_id, start_time, end_time, status) VALUES
(1, 1, DATE_ADD(CURDATE(), INTERVAL 14 HOUR), DATE_ADD(CURDATE(), INTERVAL 16 HOUR), 'open'),
(1, 1, DATE_ADD(CURDATE(), INTERVAL 19 HOUR), DATE_ADD(CURDATE(), INTERVAL 21 HOUR), 'open'),
(1, 1, DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL 14 HOUR), DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL 16 HOUR), 'open');
