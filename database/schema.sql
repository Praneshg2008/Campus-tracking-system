-- Campus Shared Resource & Equipment Tracking System
-- Database Schema for MySQL 8.0+

CREATE DATABASE IF NOT EXISTS campus_resource_db;
USE campus_resource_db;

-- 1. Drop existing tables if needed (in reverse dependency order)
DROP TABLE IF EXISTS audit_trail;
DROP TABLE IF EXISTS checkouts;
DROP TABLE IF EXISTS maintenance_logs;
DROP TABLE IF EXISTS lab_kits;
DROP TABLE IF EXISTS sports_assets;
DROP TABLE IF EXISTS media_assets;
DROP TABLE IF EXISTS resources;

-- 2. Master Table: Resources (Base entity for all trackable campus assets)
CREATE TABLE resources (
    resource_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    category ENUM('LAB_KIT', 'SPORTS_ASSET', 'MEDIA_ASSET') NOT NULL,
    status ENUM('AVAILABLE', 'CHECKED_OUT', 'MAINTENANCE', 'RETIRED') DEFAULT 'AVAILABLE',
    daily_late_fee DECIMAL(8, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Child Table: Lab Kits (Demonstrates ON UPDATE CASCADE)
CREATE TABLE lab_kits (
    resource_id VARCHAR(20) PRIMARY KEY,
    components_count INT NOT NULL,
    microcontroller_type VARCHAR(50),
    requires_calibration BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Child Table: Sports Assets (Demonstrates ON UPDATE CASCADE)
CREATE TABLE sports_assets (
    resource_id VARCHAR(20) PRIMARY KEY,
    sport_name VARCHAR(50) NOT NULL,
    wear_condition ENUM('NEW', 'GOOD', 'FAIR', 'NEEDS_REPAIR') DEFAULT 'GOOD',
    tournament_grade BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Child Table: Media Assets (DSLRs, High-end cameras, audio kits)
CREATE TABLE media_assets (
    resource_id VARCHAR(20) PRIMARY KEY,
    lens_model VARCHAR(100) NOT NULL,
    shutter_count INT DEFAULT 0,
    max_resolution VARCHAR(30) DEFAULT '4K',
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Maintenance Logs Table (Tracks repair and inspection history)
CREATE TABLE maintenance_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_id VARCHAR(20) NOT NULL,
    issue_description TEXT NOT NULL,
    technician_name VARCHAR(100) NOT NULL,
    maintenance_cost DECIMAL(10, 2) DEFAULT 0.00,
    service_date DATE NOT NULL,
    status_after VARCHAR(30) NOT NULL,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Checkouts Table (Transactions tracking student checkout, return & late fee)
CREATE TABLE checkouts (
    checkout_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_id VARCHAR(20) NOT NULL,
    student_id VARCHAR(20) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    checkout_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    late_fee DECIMAL(10, 2) DEFAULT 0.00,
    checkout_status ENUM('ACTIVE', 'RETURNED') DEFAULT 'ACTIVE',
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 8. Audit Trail Table (DBMS audit requirement for compliance and tracking)
CREATE TABLE audit_trail (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    record_id VARCHAR(50) NOT NULL,
    details TEXT NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Triggers for DBMS Audit Trails
-- -------------------------------------------------------------

DELIMITER $$

-- Trigger A: Log Resource Status & ID changes (including Cascading updates audit)
DROP TRIGGER IF EXISTS trg_resource_update $$
CREATE TRIGGER trg_resource_update
AFTER UPDATE ON resources
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO audit_trail (table_name, action_type, record_id, details)
        VALUES (
            'resources', 
            'STATUS_CHANGE', 
            NEW.resource_id, 
            CONCAT('Status changed from ', OLD.status, ' to ', NEW.status)
        );
    END IF;

    IF OLD.resource_id <> NEW.resource_id THEN
        INSERT INTO audit_trail (table_name, action_type, record_id, details)
        VALUES (
            'resources', 
            'CASCADE_ID_UPDATE', 
            NEW.resource_id, 
            CONCAT('Resource ID updated from ', OLD.resource_id, ' to ', NEW.resource_id, ' (Cascaded to child tables)')
        );
    END IF;
END $$

-- Trigger B: Log Checkouts
DROP TRIGGER IF EXISTS trg_checkout_insert $$
CREATE TRIGGER trg_checkout_insert
AFTER INSERT ON checkouts
FOR EACH ROW
BEGIN
    INSERT INTO audit_trail (table_name, action_type, record_id, details)
    VALUES (
        'checkouts',
        'CHECKOUT_ISSUED',
        CAST(NEW.checkout_id AS CHAR),
        CONCAT('Asset ', NEW.resource_id, ' issued to student ', NEW.student_name, ' (ID: ', NEW.student_id, ') due on ', NEW.due_date)
    );
END $$

-- Trigger C: Log Returns and Late Fees
DROP TRIGGER IF EXISTS trg_checkout_return $$
CREATE TRIGGER trg_checkout_return
AFTER UPDATE ON checkouts
FOR EACH ROW
BEGIN
    IF OLD.checkout_status = 'ACTIVE' AND NEW.checkout_status = 'RETURNED' THEN
        INSERT INTO audit_trail (table_name, action_type, record_id, details)
        VALUES (
            'checkouts',
            'ASSET_RETURNED',
            CAST(NEW.checkout_id AS CHAR),
            CONCAT('Asset ', NEW.resource_id, ' returned on ', NEW.return_date, '. Late Fee assessed: $', NEW.late_fee)
        );
    END IF;
END $$

-- Trigger D: Log Maintenance Events
DROP TRIGGER IF EXISTS trg_maintenance_insert $$
CREATE TRIGGER trg_maintenance_insert
AFTER INSERT ON maintenance_logs
FOR EACH ROW
BEGIN
    INSERT INTO audit_trail (table_name, action_type, record_id, details)
    VALUES (
        'maintenance_logs',
        'MAINTENANCE_LOGGED',
        CAST(NEW.log_id AS CHAR),
        CONCAT('Maintenance for ', NEW.resource_id, ' by ', NEW.technician_name, '. Cost: $', NEW.maintenance_cost, '. Issue: ', NEW.issue_description)
    );
END $$

DELIMITER ;

-- -------------------------------------------------------------
-- Seed Sample Data (High-Value Official Department Assets)
-- -------------------------------------------------------------

-- Base Resources
INSERT INTO resources (resource_id, name, department, category, status, daily_late_fee) VALUES
('LAB-ESP32-01', 'ESP32-S3 AI IoT Dev Kit', 'ECE Department', 'LAB_KIT', 'AVAILABLE', 10.00),
('LAB-OSC-02', 'Tektronix 100MHz Digital Oscilloscope', 'EE Lab', 'LAB_KIT', 'AVAILABLE', 25.00),
('CAM-DSLR-01', 'Canon EOS R6 Mark II + 24-105mm Kit', 'Media & PR Lab', 'MEDIA_ASSET', 'AVAILABLE', 35.00),
('CAM-DSLR-02', 'Sony Alpha A7 IV Full-Frame Camera', 'Media & PR Lab', 'MEDIA_ASSET', 'AVAILABLE', 40.00),
('SPT-CRIC-01', 'English Willow Grade-1 Tournament Cricket Kit', 'Physical Education', 'SPORTS_ASSET', 'AVAILABLE', 15.00),
('SPT-BASK-02', 'Spalding Official TF-1000 Tournament Set', 'Physical Education', 'SPORTS_ASSET', 'AVAILABLE', 8.00);

-- Specialized Details for Lab Kits
INSERT INTO lab_kits (resource_id, components_count, microcontroller_type, requires_calibration) VALUES
('LAB-ESP32-01', 14, 'ESP32-S3 Dual Core Xtensa', FALSE),
('LAB-OSC-02', 4, 'Hardware DSP Controller', TRUE);

-- Specialized Details for Media Assets
INSERT INTO media_assets (resource_id, lens_model, shutter_count, max_resolution) VALUES
('CAM-DSLR-01', 'RF 24-105mm f/4L IS USM', 3420, '4K 60fps'),
('CAM-DSLR-02', 'FE 24-70mm f/2.8 GM II', 1890, '4K 60fps HDR');

-- Specialized Details for Sports Assets
INSERT INTO sports_assets (resource_id, sport_name, wear_condition, tournament_grade) VALUES
('SPT-CRIC-01', 'Cricket', 'GOOD', TRUE),
('SPT-BASK-02', 'Basketball', 'NEW', TRUE);

-- Initial Audit Record
INSERT INTO audit_trail (table_name, action_type, record_id, details)
VALUES ('system', 'INIT', 'SCHEMA_V1', 'Campus Shared Resource & Equipment Tracking System Database Initialized');
