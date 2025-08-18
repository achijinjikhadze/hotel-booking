
INSERT INTO rooms (roomnumber, roombed, roomprice) VALUES
(101, 1, 75.00),
(102, 2, 120.00),
(103, 3, 150.00),
(104, 2, 100.00),
(105, 3, 180.00);

INSERT INTO guests (firstname, lastname, phone) VALUES
('John', 'Doe', '1234567890'),
('Jane', 'Smith', '0987654321'),
('Alice', 'Johnson', '5551234567');



INSERT INTO bookings (roomid, guestid, checkindate, checkoutdate) VALUES
(1, 1, '2025-07-01', '2025-07-08'),
(2, 2, '2025-07-10', '2025-07-18');



INSERT INTO payments (guestid, amount, paydate) VALUES
(1, 300.00, '2025-07-05'),
(2, 240.00, '2025-07-12');



INSERT INTO users (username, password, isadmin) VALUES
('admin', 'admin123', 1),
('reception', 'welcome1', 0);