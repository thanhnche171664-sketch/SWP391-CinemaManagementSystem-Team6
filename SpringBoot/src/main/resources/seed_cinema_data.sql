-- Seed Data for Cinema, Room, Seat, Pricing, and Showtime
-- Note: Replace movie_id with actual available movie_ids if needed.
-- It is assumed that movies with ID 1 and 2 already exist in the database.

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE booking_seats;  -- optional fallback cleanup
TRUNCATE TABLE bookings;       -- optional fallback cleanup
TRUNCATE TABLE showtimes;
TRUNCATE TABLE pricing;
TRUNCATE TABLE seats;
TRUNCATE TABLE rooms;
TRUNCATE TABLE branch_movies;  -- optional fallback cleanup
TRUNCATE TABLE cinema_branches;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Cinema Branches
INSERT IGNORE INTO cinema_branches (branch_id, branch_name, city, address, status) VALUES
(1, 'Cinema HCM', 'Ho Chi Minh', '123 Nguyen Hue, Q1', 'active'),
(2, 'Cinema Hanoi', 'Hanoi', '45 Tran Hung Dao, Hoan Kiem', 'active'),
(3, 'Cinema Da Nang', 'Da Nang', '999 Nguyen Van Linh', 'active');

-- 2. Rooms
INSERT IGNORE INTO rooms (room_id, branch_id, room_name, total_seats, status) VALUES
(1, 1, 'Room 1 - Standard', 48, 'active'),
(2, 1, 'Room 2 - IMAX', 48, 'active'),
(3, 2, 'Room 1 - Premium', 48, 'active'),
(4, 3, 'Room 1 - Standard', 48, 'active');

-- 3. Seats (For Room 1, 2, 3, 4 - Each has 48 seats: 6 rows x 8 columns)
-- Normal: Rows A-D, VIP: Row E, Couple: Row F
INSERT IGNORE INTO seats (room_id, seat_row, seat_number, seat_type) VALUES
-- Room 1
(1,'A',1,'NORMAL'),(1,'A',2,'NORMAL'),(1,'A',3,'NORMAL'),(1,'A',4,'NORMAL'),(1,'A',5,'NORMAL'),(1,'A',6,'NORMAL'),(1,'A',7,'NORMAL'),(1,'A',8,'NORMAL'),
(1,'B',1,'NORMAL'),(1,'B',2,'NORMAL'),(1,'B',3,'NORMAL'),(1,'B',4,'NORMAL'),(1,'B',5,'NORMAL'),(1,'B',6,'NORMAL'),(1,'B',7,'NORMAL'),(1,'B',8,'NORMAL'),
(1,'C',1,'NORMAL'),(1,'C',2,'NORMAL'),(1,'C',3,'NORMAL'),(1,'C',4,'NORMAL'),(1,'C',5,'NORMAL'),(1,'C',6,'NORMAL'),(1,'C',7,'NORMAL'),(1,'C',8,'NORMAL'),
(1,'D',1,'NORMAL'),(1,'D',2,'NORMAL'),(1,'D',3,'NORMAL'),(1,'D',4,'NORMAL'),(1,'D',5,'NORMAL'),(1,'D',6,'NORMAL'),(1,'D',7,'NORMAL'),(1,'D',8,'NORMAL'),
(1,'E',1,'VIP'),(1,'E',2,'VIP'),(1,'E',3,'VIP'),(1,'E',4,'VIP'),(1,'E',5,'VIP'),(1,'E',6,'VIP'),(1,'E',7,'VIP'),(1,'E',8,'VIP'),
(1,'F',1,'COUPLE'),(1,'F',2,'COUPLE'),(1,'F',3,'COUPLE'),(1,'F',4,'COUPLE'),(1,'F',5,'COUPLE'),(1,'F',6,'COUPLE'),(1,'F',7,'COUPLE'),(1,'F',8,'COUPLE'),

-- Room 2
(2,'A',1,'NORMAL'),(2,'A',2,'NORMAL'),(2,'A',3,'NORMAL'),(2,'A',4,'NORMAL'),(2,'A',5,'NORMAL'),(2,'A',6,'NORMAL'),(2,'A',7,'NORMAL'),(2,'A',8,'NORMAL'),
(2,'B',1,'NORMAL'),(2,'B',2,'NORMAL'),(2,'B',3,'NORMAL'),(2,'B',4,'NORMAL'),(2,'B',5,'NORMAL'),(2,'B',6,'NORMAL'),(2,'B',7,'NORMAL'),(2,'B',8,'NORMAL'),
(2,'C',1,'NORMAL'),(2,'C',2,'NORMAL'),(2,'C',3,'NORMAL'),(2,'C',4,'NORMAL'),(2,'C',5,'NORMAL'),(2,'C',6,'NORMAL'),(2,'C',7,'NORMAL'),(2,'C',8,'NORMAL'),
(2,'D',1,'NORMAL'),(2,'D',2,'NORMAL'),(2,'D',3,'NORMAL'),(2,'D',4,'NORMAL'),(2,'D',5,'NORMAL'),(2,'D',6,'NORMAL'),(2,'D',7,'NORMAL'),(2,'D',8,'NORMAL'),
(2,'E',1,'VIP'),(2,'E',2,'VIP'),(2,'E',3,'VIP'),(2,'E',4,'VIP'),(2,'E',5,'VIP'),(2,'E',6,'VIP'),(2,'E',7,'VIP'),(2,'E',8,'VIP'),
(2,'F',1,'COUPLE'),(2,'F',2,'COUPLE'),(2,'F',3,'COUPLE'),(2,'F',4,'COUPLE'),(2,'F',5,'COUPLE'),(2,'F',6,'COUPLE'),(2,'F',7,'COUPLE'),(2,'F',8,'COUPLE'),

-- Room 3
(3,'A',1,'NORMAL'),(3,'A',2,'NORMAL'),(3,'A',3,'NORMAL'),(3,'A',4,'NORMAL'),(3,'A',5,'NORMAL'),(3,'A',6,'NORMAL'),(3,'A',7,'NORMAL'),(3,'A',8,'NORMAL'),
(3,'B',1,'NORMAL'),(3,'B',2,'NORMAL'),(3,'B',3,'NORMAL'),(3,'B',4,'NORMAL'),(3,'B',5,'NORMAL'),(3,'B',6,'NORMAL'),(3,'B',7,'NORMAL'),(3,'B',8,'NORMAL'),
(3,'C',1,'NORMAL'),(3,'C',2,'NORMAL'),(3,'C',3,'NORMAL'),(3,'C',4,'NORMAL'),(3,'C',5,'NORMAL'),(3,'C',6,'NORMAL'),(3,'C',7,'NORMAL'),(3,'C',8,'NORMAL'),
(3,'D',1,'NORMAL'),(3,'D',2,'NORMAL'),(3,'D',3,'NORMAL'),(3,'D',4,'NORMAL'),(3,'D',5,'NORMAL'),(3,'D',6,'NORMAL'),(3,'D',7,'NORMAL'),(3,'D',8,'NORMAL'),
(3,'E',1,'VIP'),(3,'E',2,'VIP'),(3,'E',3,'VIP'),(3,'E',4,'VIP'),(3,'E',5,'VIP'),(3,'E',6,'VIP'),(3,'E',7,'VIP'),(3,'E',8,'VIP'),
(3,'F',1,'COUPLE'),(3,'F',2,'COUPLE'),(3,'F',3,'COUPLE'),(3,'F',4,'COUPLE'),(3,'F',5,'COUPLE'),(3,'F',6,'COUPLE'),(3,'F',7,'COUPLE'),(3,'F',8,'COUPLE'),

-- Room 4
(4,'A',1,'NORMAL'),(4,'A',2,'NORMAL'),(4,'A',3,'NORMAL'),(4,'A',4,'NORMAL'),(4,'A',5,'NORMAL'),(4,'A',6,'NORMAL'),(4,'A',7,'NORMAL'),(4,'A',8,'NORMAL'),
(4,'B',1,'NORMAL'),(4,'B',2,'NORMAL'),(4,'B',3,'NORMAL'),(4,'B',4,'NORMAL'),(4,'B',5,'NORMAL'),(4,'B',6,'NORMAL'),(4,'B',7,'NORMAL'),(4,'B',8,'NORMAL'),
(4,'C',1,'NORMAL'),(4,'C',2,'NORMAL'),(4,'C',3,'NORMAL'),(4,'C',4,'NORMAL'),(4,'C',5,'NORMAL'),(4,'C',6,'NORMAL'),(4,'C',7,'NORMAL'),(4,'C',8,'NORMAL'),
(4,'D',1,'NORMAL'),(4,'D',2,'NORMAL'),(4,'D',3,'NORMAL'),(4,'D',4,'NORMAL'),(4,'D',5,'NORMAL'),(4,'D',6,'NORMAL'),(4,'D',7,'NORMAL'),(4,'D',8,'NORMAL'),
(4,'E',1,'VIP'),(4,'E',2,'VIP'),(4,'E',3,'VIP'),(4,'E',4,'VIP'),(4,'E',5,'VIP'),(4,'E',6,'VIP'),(4,'E',7,'VIP'),(4,'E',8,'VIP'),
(4,'F',1,'COUPLE'),(4,'F',2,'COUPLE'),(4,'F',3,'COUPLE'),(4,'F',4,'COUPLE'),(4,'F',5,'COUPLE'),(4,'F',6,'COUPLE'),(4,'F',7,'COUPLE'),(4,'F',8,'COUPLE');

-- 4. Pricing (for branches 1, 2, 3)
INSERT IGNORE INTO pricing (branch_id, seat_type, time_range, price) VALUES
-- Branch 1
(1, 'NORMAL', 'weekday', 70000.00),
(1, 'NORMAL', 'weekend', 90000.00),
(1, 'VIP', 'weekday', 100000.00),
(1, 'VIP', 'weekend', 120000.00),
(1, 'COUPLE', 'weekday', 140000.00),
(1, 'COUPLE', 'weekend', 160000.00),
-- Branch 2
(2, 'NORMAL', 'weekday', 75000.00),
(2, 'NORMAL', 'weekend', 95000.00),
(2, 'VIP', 'weekday', 105000.00),
(2, 'VIP', 'weekend', 125000.00),
(2, 'COUPLE', 'weekday', 145000.00),
(2, 'COUPLE', 'weekend', 165000.00),
-- Branch 3
(3, 'NORMAL', 'weekday', 65000.00),
(3, 'NORMAL', 'weekend', 85000.00),
(3, 'VIP', 'weekday', 90000.00),
(3, 'VIP', 'weekend', 110000.00),
(3, 'COUPLE', 'weekday', 130000.00),
(3, 'COUPLE', 'weekend', 150000.00);

-- 5. Showtimes (For Movie ID 1, 2 assuming they exist in movies table)
INSERT IGNORE INTO showtimes (movie_id, room_id, start_time, end_time, status) VALUES
-- Movie 1 in Room 1 (Today & Tomorrow)
(1, 1, DATE_ADD(CURDATE(), INTERVAL '10:00' HOUR_MINUTE), DATE_ADD(CURDATE(), INTERVAL '12:00' HOUR_MINUTE), 'open'),
(1, 1, DATE_ADD(CURDATE(), INTERVAL '14:30' HOUR_MINUTE), DATE_ADD(CURDATE(), INTERVAL '16:30' HOUR_MINUTE), 'open'),
(1, 1, DATE_ADD(CURDATE(), INTERVAL '19:00' HOUR_MINUTE), DATE_ADD(CURDATE(), INTERVAL '21:00' HOUR_MINUTE), 'open'),
(1, 1, DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL '10:00' HOUR_MINUTE), DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL '12:00' HOUR_MINUTE), 'open'),
(1, 1, DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL '19:00' HOUR_MINUTE), DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL '21:00' HOUR_MINUTE), 'open'),

-- Movie 2 in Room 2 (Today & Tomorrow)
(2, 2, DATE_ADD(CURDATE(), INTERVAL '09:00' HOUR_MINUTE), DATE_ADD(CURDATE(), INTERVAL '11:00' HOUR_MINUTE), 'open'),
(2, 2, DATE_ADD(CURDATE(), INTERVAL '13:00' HOUR_MINUTE), DATE_ADD(CURDATE(), INTERVAL '15:00' HOUR_MINUTE), 'open'),
(2, 2, DATE_ADD(CURDATE(), INTERVAL '20:00' HOUR_MINUTE), DATE_ADD(CURDATE(), INTERVAL '22:00' HOUR_MINUTE), 'open'),
(2, 2, DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL '20:00' HOUR_MINUTE), DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL '22:00' HOUR_MINUTE), 'open'),

-- Movie 1 in Room 3 (Premium)
(1, 3, DATE_ADD(CURDATE(), INTERVAL '18:00' HOUR_MINUTE), DATE_ADD(CURDATE(), INTERVAL '20:00' HOUR_MINUTE), 'open'),
(1, 3, DATE_ADD(CURDATE(), INTERVAL '20:30' HOUR_MINUTE), DATE_ADD(CURDATE(), INTERVAL '22:30' HOUR_MINUTE), 'open'),

-- Movie 2 in Room 4
(2, 4, DATE_ADD(CURDATE(), INTERVAL '17:00' HOUR_MINUTE), DATE_ADD(CURDATE(), INTERVAL '19:00' HOUR_MINUTE), 'open'),
(2, 4, DATE_ADD(CURDATE(), INTERVAL '21:00' HOUR_MINUTE), DATE_ADD(CURDATE(), INTERVAL '23:00' HOUR_MINUTE), 'open');
