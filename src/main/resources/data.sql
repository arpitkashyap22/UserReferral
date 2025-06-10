-- Sample Users (passwords are BCrypt encrypted 'Test1234')
INSERT INTO users (email, password, name, referral_code, referral_points)
VALUES 
    ('john.doe@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'John Doe', 'ABC123', 1),
    ('jane.smith@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Jane Smith', 'DEF456', 0),
    ('bob.wilson@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Bob Wilson', 'GHI789', 0),
    ('alice.johnson@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Alice Johnson', 'JKL012', 0),
    ('charlie.brown@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Charlie Brown', 'MNO345', 0);
--
-- -- Sample Referrals
-- INSERT INTO profiles ( street, city, state, zip_code, phone_number, dob)
-- VALUES
--     ( '123 Main St', 'Springfield', 'IL', '62701', '555-123-4567', '1990-01-01'),
--     ( '456 Oak Ave', 'Chicago', 'IL', '60601', '555-234-5678', '1992-05-15'),
--     ( '789 Pine Rd', 'Peoria', 'IL', '61601', '555-345-6789', '1988-12-31');

-- -- Sample Profiles (2 complete, 1 partial)
-- INSERT INTO referrals (referrer_id, referee_id, status)
-- VALUES
--     (1, 2, 'COMPLETED'),
--     (1, 3, 'PENDING'),
--     (2, 4,  'PENDING');
