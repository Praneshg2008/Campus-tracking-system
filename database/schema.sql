-- Campus Equipment & Resource Tracking System
-- Simplified Database Schema for MySQL (2nd Year CSE OOP & DBMS Project)

CREATE DATABASE IF NOT EXISTS campus_resource_db;
USE campus_resource_db;

-- Drop existing tables (including legacy tables if present) in reverse dependency order
DROP TABLE IF EXISTS audit_trail;
DROP TABLE IF EXISTS maintenance_logs;
DROP TABLE IF EXISTS lab_kits;
DROP TABLE IF EXISTS sports_assets;
DROP TABLE IF EXISTS media_assets;
DROP TABLE IF EXISTS checkouts;
DROP TABLE IF EXISTS maintenance;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS students;

-- 1. Students Table: Stores registered student details
CREATE TABLE students (
    student_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL
);

-- 2. Resources Table: Stores campus equipment details
CREATE TABLE resources (
    resource_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    department VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    daily_late_fee DECIMAL(8, 2) NOT NULL DEFAULT 10.00
);

-- 3. Checkouts Table: Tracks equipment issued to students and returns
CREATE TABLE checkouts (
    checkout_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_id VARCHAR(20) NOT NULL,
    student_id VARCHAR(20) NOT NULL,
    checkout_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE DEFAULT NULL,
    late_fee DECIMAL(8, 2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- 4. Maintenance Table: Tracks basic equipment repair and maintenance records
CREATE TABLE maintenance (
    maintenance_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_id VARCHAR(20) NOT NULL,
    issue_description VARCHAR(255) NOT NULL,
    technician_name VARCHAR(100) NOT NULL,
    maintenance_cost DECIMAL(8, 2) DEFAULT 0.00,
    service_date DATE NOT NULL,
    status_after VARCHAR(20) NOT NULL,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- Sample Data for Demonstration
-- -------------------------------------------------------------

INSERT INTO students (student_id, name, department, email, phone) VALUES
('STU-101', 'Aarav Sharma', 'CSE', 'aarav.sharma@college.edu', '9876543210'),
('STU-102', 'Priya Patel', 'ECE', 'priya.patel@college.edu', '9876543211'),
('STU-103', 'Rohan Verma', 'Mechanical', 'rohan.verma@college.edu', '9876543212'),
('STU-104', 'Sneha Nair', 'CSE', 'sneha.nair@college.edu', '9876543213');

INSERT INTO resources (resource_id, name, category, department, status, daily_late_fee) VALUES
('EQ-101', 'ESP32 IoT Development Kit', 'Lab Equipment', 'ECE Lab', 'AVAILABLE', 10.00),
('EQ-102', 'Digital Storage Oscilloscope', 'Lab Equipment', 'EEE Lab', 'BORROWED', 25.00),
('EQ-103', 'Canon EOS DSLR Camera', 'Media Gear', 'Media Club', 'AVAILABLE', 30.00),
('EQ-104', 'Arduino Sensor Kit', 'Lab Equipment', 'CSE Lab', 'AVAILABLE', 10.00),
('EQ-105', 'Tournament Cricket Kit', 'Sports Gear', 'Sports Dept', 'AVAILABLE', 15.00),
('EQ-106', 'Projector HDMI Portable', 'Media Gear', 'AV Room', 'MAINTENANCE', 20.00);

INSERT INTO checkouts (resource_id, student_id, checkout_date, due_date, return_date, late_fee, status) VALUES
('EQ-101', 'STU-101', '2026-10-15', '2026-10-20', '2026-10-22', 20.00, 'RETURNED'),
('EQ-102', 'STU-102', '2026-11-01', '2026-11-06', NULL, 0.00, 'ACTIVE');

INSERT INTO maintenance (resource_id, issue_description, technician_name, maintenance_cost, service_date, status_after) VALUES
('EQ-106', 'Lamp replacement and lens cleaning', 'Ramesh Kumar', 450.00, '2026-11-02', 'MAINTENANCE'),
('EQ-103', 'Sensor cleaning and firmware update', 'Suresh Babu', 150.00, '2026-10-10', 'AVAILABLE');
