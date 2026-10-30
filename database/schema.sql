-- ============================================================================
--  CAMPUS LOST & FOUND MANAGEMENT SYSTEM
--  File   : database/schema.sql
--  DBMS   : MySQL 8.0+ / MariaDB 10.4+ (XAMPP compatible)
--  Import : mysql -u root -p < database/schema.sql
--           or  MySQL Workbench -> File -> Open SQL Script -> Execute
--
--  This single script creates the database, all 8 tables, 2 views,
--  2 triggers and demonstration seed data.
--
--  DEMO LOGINS (password shown in plain text only for the demo seed):
--    admin@campus.edu    / admin123     -> ADMIN
--    security@campus.edu / staff123     -> STAFF  (Security Office)
--    arjun@campus.edu    / student123   -> STUDENT
--    priya@campus.edu    / student123   -> STUDENT
--    karthik@campus.edu  / student123   -> STUDENT
-- ============================================================================

DROP DATABASE IF EXISTS campus_lost_found;
CREATE DATABASE campus_lost_found
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE campus_lost_found;

-- ============================================================================
--  1. USERS  -  every person who can report or claim an item
-- ============================================================================
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(80)  NOT NULL,
    email         VARCHAR(120) NOT NULL,
    phone         VARCHAR(15)  NOT NULL,
    password_hash CHAR(64)     NOT NULL,          -- SHA-256 hex digest
    role          ENUM('STUDENT','STAFF','ADMIN') NOT NULL DEFAULT 'STUDENT',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email  UNIQUE (email),
    CONSTRAINT chk_users_phone CHECK (CHAR_LENGTH(phone) >= 10)
) ENGINE = InnoDB;

-- ============================================================================
--  2. CATEGORIES  -  lookup table (Electronics, ID Cards, ...)
-- ============================================================================
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(120) NULL,
    CONSTRAINT uq_categories_name UNIQUE (name)
) ENGINE = InnoDB;

-- ============================================================================
--  3. LOCATIONS  -  lookup table (Library, Canteen, CS Block, ...)
-- ============================================================================
CREATE TABLE locations (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(60) NOT NULL,
    building    VARCHAR(60) NOT NULL,
    CONSTRAINT uq_locations_name UNIQUE (name)
) ENGINE = InnoDB;

-- ============================================================================
--  4. ITEMS  -  super-table for the Item class hierarchy
--     item_type is the discriminator: LOST or FOUND
-- ============================================================================
CREATE TABLE items (
    item_id     INT AUTO_INCREMENT PRIMARY KEY,
    item_type   ENUM('LOST','FOUND') NOT NULL,
    title       VARCHAR(120) NOT NULL,
    description TEXT         NOT NULL,
    category_id INT          NOT NULL,
    location_id INT          NOT NULL,
    event_date  DATE         NOT NULL,
    reporter_id INT          NOT NULL,
    status      ENUM('OPEN','MATCHED','CLAIMED','RECOVERED','CLOSED')
                NOT NULL DEFAULT 'OPEN',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_items_category FOREIGN KEY (category_id)
        REFERENCES categories (category_id),
    CONSTRAINT fk_items_location FOREIGN KEY (location_id)
        REFERENCES locations (location_id),
    CONSTRAINT fk_items_reporter FOREIGN KEY (reporter_id)
        REFERENCES users (user_id),

    INDEX idx_items_type    (item_type),
    INDEX idx_items_status  (status),
    INDEX idx_items_search  (category_id, location_id, event_date)
) ENGINE = InnoDB;

-- ============================================================================
--  5. LOST_ITEMS  -  sub-class table (1:1 with items, inheritance)
-- ============================================================================
CREATE TABLE lost_items (
    item_id        INT PRIMARY KEY,
    contact        VARCHAR(80)   NOT NULL,
    reward_offered DECIMAL(8,2)  NOT NULL DEFAULT 0.00,
    urgency        ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',

    CONSTRAINT fk_lost_items FOREIGN KEY (item_id)
        REFERENCES items (item_id) ON DELETE CASCADE,
    CONSTRAINT chk_reward_non_negative CHECK (reward_offered >= 0)
) ENGINE = InnoDB;

-- ============================================================================
--  6. FOUND_ITEMS  -  sub-class table (1:1 with items, inheritance)
-- ============================================================================
CREATE TABLE found_items (
    item_id           INT PRIMARY KEY,
    storage_location  VARCHAR(120) NOT NULL,
    handed_to_security BOOLEAN     NOT NULL DEFAULT TRUE,
    finder_notes      VARCHAR(255) NULL,

    CONSTRAINT fk_found_items FOREIGN KEY (item_id)
        REFERENCES items (item_id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- ============================================================================
--  7. CLAIMS  -  ownership claims raised against a FOUND item
-- ============================================================================
CREATE TABLE claims (
    claim_id     INT AUTO_INCREMENT PRIMARY KEY,
    item_id      INT  NOT NULL,
    claimant_id  INT  NOT NULL,
    proof        TEXT NOT NULL,
    status       ENUM('PENDING','APPROVED','REJECTED','RECOVERED')
                 NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_by  INT       NULL,
    verified_at  DATETIME  NULL,
    remarks      VARCHAR(255) NULL,

    CONSTRAINT fk_claims_item      FOREIGN KEY (item_id)
        REFERENCES items (item_id) ON DELETE CASCADE,
    CONSTRAINT fk_claims_claimant  FOREIGN KEY (claimant_id)
        REFERENCES users (user_id),
    CONSTRAINT fk_claims_verifier  FOREIGN KEY (verified_by)
        REFERENCES users (user_id),

    -- DBMS constraint demo: a user cannot claim the same item twice
    CONSTRAINT uq_claim_once UNIQUE (item_id, claimant_id),
    INDEX idx_claims_status (status)
) ENGINE = InnoDB;

-- ============================================================================
--  8. ITEM_STATUS_HISTORY  -  audit trail written by triggers
-- ============================================================================
CREATE TABLE item_status_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id    INT NOT NULL,
    old_status VARCHAR(15) NULL,
    new_status VARCHAR(15) NOT NULL,
    note       VARCHAR(120) NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_item FOREIGN KEY (item_id)
        REFERENCES items (item_id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- ============================================================================
--  9. TRIGGERS  -  automatic audit logging (no Java code required)
-- ============================================================================
DELIMITER $$

CREATE TRIGGER trg_items_after_insert
AFTER INSERT ON items
FOR EACH ROW
BEGIN
    INSERT INTO item_status_history (item_id, old_status, new_status, note)
    VALUES (NEW.item_id, NULL, NEW.status, 'Item reported');
END$$

CREATE TRIGGER trg_items_after_update
AFTER UPDATE ON items
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO item_status_history (item_id, old_status, new_status, note)
        VALUES (NEW.item_id, OLD.status, NEW.status, 'Status updated by application');
    END IF;
END$$

DELIMITER ;

-- ============================================================================
--  10. VIEWS  -  saved joins used by the reporting screens
-- ============================================================================
CREATE OR REPLACE VIEW vw_item_details AS
SELECT i.item_id,
       i.item_type,
       i.title,
       i.description,
       i.status,
       i.event_date,
       i.created_at,
       c.category_id,
       c.name        AS category_name,
       l.location_id,
       l.name        AS location_name,
       l.building,
       u.user_id     AS reporter_id,
       u.full_name   AS reporter_name,
       u.email       AS reporter_email,
       u.phone       AS reporter_phone,
       u.role        AS reporter_role,
       li.contact         AS lost_contact,
       li.reward_offered,
       li.urgency,
       fi.storage_location,
       fi.handed_to_security,
       fi.finder_notes
FROM items i
JOIN categories c ON c.category_id = i.category_id
JOIN locations  l ON l.location_id = i.location_id
JOIN users      u ON u.user_id     = i.reporter_id
LEFT JOIN lost_items  li ON li.item_id = i.item_id
LEFT JOIN found_items fi ON fi.item_id = i.item_id;

-- ----------------------------------------------------------------------------
--  Matching engine: pairs every OPEN/MATCHED lost report with every
--  OPEN/MATCHED found report in the same category, reported within 30 days.
--  A weighted score is calculated in SQL:
--      same category .......... 40 points
--      same location .......... 30 points
--      date gap <= 2 days ..... 20 points   (<=7 days -> 12, else 5)
--      first word of title .... 10 points
--  Pairs scoring >= 50 are returned, highest score first.
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_potential_matches AS
--  NOTE: MySQL forbids a subquery in the FROM clause inside a view, so the
--  score is written once in the SELECT list and filtered with HAVING
--  (MySQL allows HAVING to reference a select alias without GROUP BY).
SELECT lo.item_id                            AS lost_item_id,
       lo.title                              AS lost_title,
       fo.item_id                            AS found_item_id,
       fo.title                              AS found_title,
       c.name                                AS category_name,
       l.name                                AS location_name,
       lo.event_date                         AS lost_date,
       fo.event_date                         AS found_date,
       ABS(DATEDIFF(lo.event_date, fo.event_date)) AS days_apart,
       (
           40
         + 30
         + CASE
               WHEN ABS(DATEDIFF(lo.event_date, fo.event_date)) <= 2 THEN 20
               WHEN ABS(DATEDIFF(lo.event_date, fo.event_date)) <= 7 THEN 12
               ELSE 5
           END
         + CASE
               WHEN fo.title LIKE CONCAT('%', SUBSTRING_INDEX(lo.title, ' ', 1), '%')
                 OR lo.title LIKE CONCAT('%', SUBSTRING_INDEX(fo.title, ' ', 1), '%')
               THEN 10 ELSE 0
           END
       ) AS match_score
FROM items lo
JOIN items fo
  ON fo.item_type = 'FOUND'
 AND lo.item_type = 'LOST'
 AND ABS(DATEDIFF(lo.event_date, fo.event_date)) <= 30
JOIN categories c ON c.category_id = lo.category_id
JOIN locations  l ON l.location_id = lo.location_id
WHERE lo.category_id = fo.category_id
  AND lo.location_id = fo.location_id
  AND lo.status IN ('OPEN','MATCHED')
  AND fo.status IN ('OPEN','MATCHED')
HAVING match_score >= 50
ORDER BY match_score DESC, days_apart ASC;

-- ============================================================================
--  11. SEED DATA
-- ============================================================================

-- -- Users ---------------------------------------------------------------
INSERT INTO users (full_name, email, phone, password_hash, role) VALUES
('Dr. Ramesh Kumar', 'admin@campus.edu',    '9840010001',
 '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
('Suresh Babu',      'security@campus.edu', '9840010002',
 '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6', 'STAFF'),
('Arjun Mehta',      'arjun@campus.edu',    '9840010003',
 '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'STUDENT'),
('Priya Nair',       'priya@campus.edu',    '9840010004',
 '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'STUDENT'),
('Karthik Rao',      'karthik@campus.edu',  '9840010005',
 '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'STUDENT');

-- -- Categories ----------------------------------------------------------
INSERT INTO categories (name, description) VALUES
('Electronics',      'Laptops, chargers, earphones, calculators'),
('ID Cards',         'Student ID, library card, access card'),
('Keys',             'Room, vehicle and locker keys'),
('Books & Notes',    'Textbooks, notebooks, lab records'),
('Bags',             'Backpacks, laptop bags, handbags'),
('Clothing',         'Jackets, hoodies, caps, shoes'),
('Accessories',      'Watches, spectacles, bottles, wallets'),
('Sports Equipment', 'Rackets, balls, gym gear'),
('Other',            'Anything that does not fit above');

-- -- Locations -----------------------------------------------------------
INSERT INTO locations (name, building) VALUES
('Library',        'Central Library'),
('Canteen',        'Student Centre'),
('CS Block',       'Academic Block A'),
('Hostel Block A', 'Residence Zone'),
('Parking Lot',    'Main Gate'),
('Sports Ground',  'Sports Complex'),
('Auditorium',     'Student Centre'),
('Admin Block',    'Main Building');

-- -- Items ---------------------------------------------------------------
INSERT INTO items (item_id, item_type, title, description, category_id,
                   location_id, event_date, reporter_id, status) VALUES
(1, 'LOST',  'Black Dell laptop charger',
 '65W Dell charger with a small white label wrapped around the cable.',
 1, 3, '2026-10-10', 3, 'OPEN'),
(2, 'FOUND', 'Dell 65W charger with black cable',
 'Charger found on the charging point in CS Block Lab 3. Cable has a white label.',
 1, 3, '2026-10-11', 2, 'OPEN'),
(3, 'LOST',  'Blue Milton steel water bottle',
 'One litre steel bottle with a Chennai Super Kings sticker on the body.',
 7, 2, '2026-10-18', 4, 'OPEN'),
(4, 'FOUND', 'Steel water bottle - blue with CSK sticker',
 'Blue steel bottle left on canteen table 4. Kept at the canteen counter.',
 7, 2, '2026-10-19', 2, 'OPEN'),
(5, 'LOST',  'Student ID card - Arjun Mehta',
 'Student ID card with a red lanyard. Roll number 21CS1043.',
 2, 3, '2026-10-22', 3, 'OPEN'),
(6, 'FOUND', 'ID card near CS Block notice board',
 'Student ID card with red lanyard found near the CS Block notice board.',
 2, 3, '2026-10-22', 2, 'RECOVERED'),
(7, 'LOST',  'Hostel room keys on a red key ring',
 'Two keys on a red key ring with a small temple token attached.',
 3, 4, '2026-10-05', 5, 'OPEN'),
(8, 'FOUND', 'Bunch of keys with red key ring',
 'Two keys and a temple token, handed over to the hostel warden.',
 3, 4, '2026-10-06', 2, 'CLAIMED'),
(9, 'LOST',  'Logitech M235 wireless mouse',
 'Black wireless mouse. USB receiver is stored inside the battery cover.',
 1, 5, '2026-10-12', 5, 'OPEN'),
(10, 'FOUND', 'Logitech wireless mouse with receiver',
 'Black wireless mouse found near the parking lot gate.',
 1, 5, '2026-10-12', 2, 'OPEN'),
(11, 'FOUND', 'Prescription spectacles in brown case',
 'Black frame spectacles inside a brown case, found in the library reading room.',
 7, 1, '2026-10-25', 2, 'OPEN'),
(12, 'LOST',  'Brown leather wallet',
 'Wallet containing college ID and metro card. Cash is not important.',
 7, 6, '2026-10-28', 4, 'OPEN'),
(13, 'LOST',  'Casio fx-991 scientific calculator',
 'Name written on the back cover: Priya N.',
 1, 3, '2026-10-20', 4, 'OPEN'),
(14, 'FOUND', 'Casio calculator with a name on the back',
 'Scientific calculator found in CS Block room 204.',
 1, 3, '2026-10-20', 2, 'OPEN');

-- -- Lost item details ---------------------------------------------------
INSERT INTO lost_items (item_id, contact, reward_offered, urgency) VALUES
(1,  'arjun@campus.edu / 9840010003', 100.00, 'MEDIUM'),
(3,  'priya@campus.edu / 9840010004',   0.00, 'LOW'),
(5,  'arjun@campus.edu / 9840010003', 200.00, 'HIGH'),
(7,  'karthik@campus.edu / 9840010005',150.00, 'HIGH'),
(9,  'karthik@campus.edu / 9840010005',  0.00, 'LOW'),
(12, 'priya@campus.edu / 9840010004', 500.00, 'HIGH'),
(13, 'priya@campus.edu / 9840010004',   0.00, 'MEDIUM');

-- -- Found item details --------------------------------------------------
INSERT INTO found_items (item_id, storage_location, handed_to_security, finder_notes) VALUES
(2,  'CS Block - Lab 3 cupboard', TRUE,  'Cable tagged with a white label.'),
(4,  'Canteen counter',           TRUE,  'Washed and kept at the counter.'),
(6,  'Security office',           TRUE,  'Owner verified using the college register.'),
(8,  'Hostel warden office',      TRUE,  'Keys matched the room number in the register.'),
(10, 'Security office',           TRUE,  NULL),
(11, 'Library reading room desk', FALSE, 'Handed to the librarian on duty.'),
(14, 'CS Block room 204',         TRUE,  NULL);

-- -- Claims --------------------------------------------------------------
INSERT INTO claims (item_id, claimant_id, proof, status, verified_by, verified_at, remarks) VALUES
(8, 5, 'I lost two keys on a red ring with a temple token on 5 October near Hostel Block A. The warden can confirm my room number.',
 'APPROVED', 2, '2026-10-08 10:20:00', 'Verified against the hostel register.'),
(14, 5, 'Black Casio fx-991. My roll number 21CS1057 is written inside the battery cover, and the slide case has a hairline scratch on the front.',
 'PENDING', NULL, NULL, NULL),
(14, 4, 'Casio fx-991 with "Priya N." written on the back cover.',
 'PENDING', NULL, NULL, NULL),
(6, 3, 'My student ID card, roll number 21CS1043, with a red lanyard.',
 'RECOVERED', 2, '2026-10-23 16:45:00', 'Returned at the security office after ID verification.'),
(11, 3, 'Black frame spectacles in a brown case.',
 'REJECTED', 2, '2026-10-26 09:10:00', 'Frame colour and case do not match the item found.');

-- ============================================================================
--  Verification queries (run these to show the schema works)
-- ============================================================================
-- SELECT * FROM vw_item_details;
-- SELECT * FROM vw_potential_matches;
-- SELECT status, COUNT(*) FROM items GROUP BY status;
-- SELECT * FROM item_status_history;
