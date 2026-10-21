package campus;

import campus.dao.AuditDAO;
import campus.dao.CheckoutDAO;
import campus.dao.MaintenanceDAO;
import campus.dao.ResourceDAO;
import campus.models.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Main Application - Command Line Interface (CLI)
 * Campus Shared Resource & Equipment Tracking System
 *
 * OOP Highlights:
 * - Abstract Resource class
 * - Specialized child classes (LabKit, SportsAsset, MediaAsset)
 * - Polymorphic display & late-fee calculations
 * - Maintenance state logging
 *
 * DBMS Highlights:
 * - Foreign Keys with ON UPDATE CASCADE
 * - Automatic Audit Trails via MySQL Triggers
 * - Database Transactions (ACID)
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ResourceDAO resourceDAO = new ResourceDAO();
    private static final CheckoutDAO checkoutDAO = new CheckoutDAO();
    private static final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private static final AuditDAO auditDAO = new AuditDAO();

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("     CAMPUS SHARED RESOURCE & EQUIPMENT TRACKING SYSTEM (CS-RETS)        ");
        System.out.println("               College OOP & DBMS Project | Java + MySQL                  ");
        System.out.println("==========================================================================");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter choice (0-9): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllResources();
                    break;
                case "2":
                    registerNewAsset();
                    break;
                case "3":
                    checkoutAsset();
                    break;
                case "4":
                    returnAsset();
                    break;
                case "5":
                    logMaintenanceEvent();
                    break;
                case "6":
                    viewMaintenanceHistory();
                    break;
                case "7":
                    testCascadingUpdate();
                    break;
                case "8":
                    viewAuditTrail();
                    break;
                case "9":
                    viewCheckouts();
                    break;
                case "0":
                    running = false;
                    System.out.println("\nExiting Campus Tracking System. Have a great day!");
                    break;
                default:
                    System.out.println("Invalid selection. Please choose an option between 0 and 9.");
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("                           MAIN DASHBOARD                                 ");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(" [1] View All High-Value Trackable Assets (Polymorphic Display)");
        System.out.println(" [2] Register New High-Value Asset (LabKit / SportsAsset / MediaAsset)");
        System.out.println(" [3] Checkout Asset to Student (Issue with Due Date)");
        System.out.println(" [4] Return Asset & Compute Overdue Late Fees (DBMS & OOP Logic)");
        System.out.println(" [5] Log Maintenance & State Transitions (Maintenance State Logs)");
        System.out.println(" [6] View Maintenance History");
        System.out.println(" [7] Test DBMS Cascading Update (ON UPDATE CASCADE Demonstration)");
        System.out.println(" [8] View DBMS Audit Trail (Automatic MySQL Triggers)");
        System.out.println(" [9] View Checkout Records (Active & Returned)");
        System.out.println(" [0] Exit Application");
        System.out.println("--------------------------------------------------------------------------");
    }

    private static void viewAllResources() {
        System.out.println("\n--- [ALL REGISTERED HIGH-VALUE CAMPUS ASSETS] ---");
        List<Resource> resources = resourceDAO.getAllResources();
        if (resources.isEmpty()) {
            System.out.println("No assets found in the system.");
            return;
        }

        for (Resource res : resources) {
            res.displayDetails();
            System.out.println("--------------------------------------------------------------------------");
        }
    }

    private static void registerNewAsset() {
        System.out.println("\n--- [REGISTER NEW HIGH-VALUE ASSET] ---");
        System.out.println("Select Asset Category:");
        System.out.println(" 1. Lab Kit (Microcontrollers, Test Equipment, Sensors)");
        System.out.println(" 2. Sports Asset (Tournament Kits, Premium Gear)");
        System.out.println(" 3. Media Asset (DSLR Cameras, Lens packages, 4K Kits)");
        System.out.print("Choice: ");
        String catChoice = scanner.nextLine().trim();

        System.out.print("Enter Unique Asset ID (e.g. LAB-KIT-09, CAM-NIKON-01): ");
        String id = scanner.nextLine().trim().toUpperCase();

        if (id.isEmpty()) {
            System.out.println("Error: Asset ID cannot be empty.");
            return;
        }

        Resource existing = resourceDAO.getResourceById(id);
        if (existing != null) {
            System.out.println("Error: Resource with ID " + id + " already exists!");
            return;
        }

        System.out.print("Enter Asset Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Managing Department (e.g., ECE Lab, Media Center, PE Dept): ");
        String dept = scanner.nextLine().trim();

        System.out.print("Enter Daily Late Fee Rate ($): ");
        double fee = 10.0;
        try {
            fee = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid fee entered. Defaulting to $10.00/day.");
        }

        Resource newResource = null;
        if ("1".equals(catChoice)) {
            System.out.print("Enter Number of Components in Kit: ");
            int count = 1;
            try { count = Integer.parseInt(scanner.nextLine().trim()); } catch (Exception ignored) {}

            System.out.print("Enter Microcontroller / Board Type (e.g., ESP32-S3, STM32, Arduino Mega): ");
            String mc = scanner.nextLine().trim();

            System.out.print("Does it require periodic lab calibration? (y/n): ");
            boolean calib = scanner.nextLine().trim().equalsIgnoreCase("y");

            newResource = new LabKit(id, name, dept, "AVAILABLE", fee, count, mc, calib);

        } else if ("2".equals(catChoice)) {
            System.out.print("Enter Sport Name (e.g., Cricket, Basketball, Badminton): ");
            String sport = scanner.nextLine().trim();

            System.out.print("Enter Wear Condition (NEW / GOOD / FAIR / NEEDS_REPAIR): ");
            String wear = scanner.nextLine().trim().toUpperCase();
            if (!wear.equals("NEW") && !wear.equals("GOOD") && !wear.equals("FAIR") && !wear.equals("NEEDS_REPAIR")) {
                wear = "GOOD";
            }

            System.out.print("Is it Tournament Grade Gear? (y/n): ");
            boolean tourney = scanner.nextLine().trim().equalsIgnoreCase("y");

            newResource = new SportsAsset(id, name, dept, "AVAILABLE", fee, sport, wear, tourney);

        } else if ("3".equals(catChoice)) {
            System.out.print("Enter Lens Model (e.g., 24-70mm f/2.8, 50mm f/1.8): ");
            String lens = scanner.nextLine().trim();

            System.out.print("Enter Current Shutter Count: ");
            int shutter = 0;
            try { shutter = Integer.parseInt(scanner.nextLine().trim()); } catch (Exception ignored) {}

            System.out.print("Enter Max Video/Photo Resolution (e.g., 4K 60fps, 8K, 1080p): ");
            String resStr = scanner.nextLine().trim();
            if (resStr.isEmpty()) resStr = "4K";

            newResource = new MediaAsset(id, name, dept, "AVAILABLE", fee, lens, shutter, resStr);
        } else {
            System.out.println("Invalid category selected.");
            return;
        }

        boolean success = resourceDAO.addResource(newResource);
        if (success) {
            System.out.println("SUCCESS: Asset '" + id + "' successfully registered in database.");
        } else {
            System.out.println("FAILED: Could not register asset. Please check inputs or database logs.");
        }
    }

    private static void checkoutAsset() {
        System.out.println("\n--- [CHECKOUT HIGH-VALUE ASSET] ---");
        System.out.print("Enter Asset ID to Checkout: ");
        String assetId = scanner.nextLine().trim().toUpperCase();

        Resource res = resourceDAO.getResourceById(assetId);
        if (res == null) {
            System.out.println("Asset not found!");
            return;
        }

        if (!res.isAvailable()) {
            System.out.println("Asset '" + assetId + "' is currently " + res.getStatus() + " and cannot be checked out.");
            return;
        }

        System.out.println("Selected Asset: " + res.getName() + " (" + res.getCategory() + ")");
        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine().trim();

        System.out.print("Enter Student Full Name: ");
        String studentName = scanner.nextLine().trim();

        System.out.print("Enter Checkout Duration (in days, e.g. 3): ");
        int days = 3;
        try {
            days = Integer.parseInt(scanner.nextLine().trim());
            if (days <= 0) days = 1;
        } catch (NumberFormatException e) {
            System.out.println("Defaulting to 3 days.");
        }

        boolean ok = checkoutDAO.issueCheckout(assetId, studentId, studentName, days);
        if (ok) {
            System.out.println("SUCCESS: Checkout recorded!");
            System.out.println("Asset '" + assetId + "' issued to " + studentName + ".");
            System.out.println("Due Date: " + LocalDate.now().plusDays(days));
            System.out.println("Audit trail automatically logged by MySQL Trigger.");
        } else {
            System.out.println("FAILED: Checkout could not be processed.");
        }
    }

    private static void returnAsset() {
        System.out.println("\n--- [RETURN ASSET & COMPUTE LATE FEES] ---");
        List<CheckoutTransaction> activeList = checkoutDAO.getActiveCheckouts();
        if (activeList.isEmpty()) {
            System.out.println("No active checkouts currently borrowed.");
            return;
        }

        System.out.println("Currently Borrowed Assets:");
        for (CheckoutTransaction tx : activeList) {
            tx.displayDetails();
        }

        System.out.print("\nEnter Checkout Transaction ID to Return: ");
        int txId;
        try {
            txId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid Transaction ID.");
            return;
        }

        System.out.println("Return Date Option:");
        System.out.println(" 1. Return Today (" + LocalDate.now() + ")");
        System.out.println(" 2. Simulate Return with Specific Date (e.g., Overdue Test: 2026-10-30)");
        System.out.print("Choice: ");
        String dateChoice = scanner.nextLine().trim();

        LocalDate returnDate = LocalDate.now();
        if ("2".equals(dateChoice)) {
            System.out.print("Enter Return Date (YYYY-MM-DD): ");
            String dateInput = scanner.nextLine().trim();
            try {
                returnDate = LocalDate.parse(dateInput);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Using today's date.");
            }
        }

        double fee = checkoutDAO.returnCheckout(txId, returnDate);
        if (fee >= 0) {
            System.out.println("\nSUCCESS: Resource returned successfully.");
            System.out.printf("Assessed Late Fee: $%.2f%n", fee);
            if (fee > 0) {
                System.out.println("Note: Late fee calculated based on polymorphic asset rules and DBMS date difference.");
            } else {
                System.out.println("Returned on time! No late fee applied.");
            }
            System.out.println("Audit trail entry automatically generated by MySQL Trigger.");
        } else {
            System.out.println("Return failed. Please verify transaction ID.");
        }
    }

    private static void logMaintenanceEvent() {
        System.out.println("\n--- [LOG MAINTENANCE & STATE TRANSITION] ---");
        System.out.print("Enter Asset ID: ");
        String assetId = scanner.nextLine().trim().toUpperCase();

        Resource res = resourceDAO.getResourceById(assetId);
        if (res == null) {
            System.out.println("Asset not found!");
            return;
        }

        System.out.println("Current Asset: " + res.getName() + " | Current Status: " + res.getStatus());
        System.out.print("Enter Technician Name / Lab In-Charge: ");
        String tech = scanner.nextLine().trim();

        System.out.print("Enter Issue / Maintenance Description: ");
        String desc = scanner.nextLine().trim();

        System.out.print("Enter Maintenance Cost ($): ");
        double cost = 0.0;
        try {
            cost = Double.parseDouble(scanner.nextLine().trim());
        } catch (Exception ignored) {}

        System.out.println("Select Resultant Status After Maintenance:");
        System.out.println(" 1. MAINTENANCE (Still in service/repairs)");
        System.out.println(" 2. AVAILABLE (Fixed, serviced, and ready for checkout)");
        System.out.println(" 3. RETIRED (Damaged beyond repair, decommissioned)");
        System.out.print("Choice: ");
        String stChoice = scanner.nextLine().trim();

        String statusAfter = "AVAILABLE";
        if ("1".equals(stChoice)) statusAfter = "MAINTENANCE";
        else if ("3".equals(stChoice)) statusAfter = "RETIRED";

        MaintenanceLog log = new MaintenanceLog(assetId, desc, tech, cost, LocalDate.now(), statusAfter);
        boolean ok = maintenanceDAO.logMaintenance(log);
        if (ok) {
            System.out.println("SUCCESS: Maintenance record saved with Log ID #" + log.getLogId());
            System.out.println("Asset status transitioned to: " + statusAfter);
            System.out.println("Audit trail automatically logged by MySQL Trigger.");
        } else {
            System.out.println("Failed to log maintenance event.");
        }
    }

    private static void viewMaintenanceHistory() {
        System.out.println("\n--- [MAINTENANCE & SERVICING HISTORY] ---");
        System.out.print("Enter Asset ID to filter (or press ENTER to view all): ");
        String input = scanner.nextLine().trim().toUpperCase();

        List<MaintenanceLog> logs;
        if (input.isEmpty()) {
            logs = maintenanceDAO.getAllLogs();
        } else {
            logs = maintenanceDAO.getLogsForResource(input);
        }

        if (logs.isEmpty()) {
            System.out.println("No maintenance logs found.");
            return;
        }

        for (MaintenanceLog log : logs) {
            log.displayDetails();
            System.out.println("--------------------------------------------------------------------------");
        }
    }

    private static void testCascadingUpdate() {
        System.out.println("\n--- [TEST DBMS CASCADING UPDATE (ON UPDATE CASCADE)] ---");
        System.out.println("This feature demonstrates relational DBMS Cascading Foreign Keys.");
        System.out.println("Updating the Primary Key 'resource_id' in table 'resources' will");
        System.out.println("automatically cascade updates to child tables (lab_kits / sports_assets /");
        System.out.println("media_assets, maintenance_logs, and checkouts) without any manual query.");

        System.out.print("\nEnter Existing Resource ID to Rename (e.g. LAB-ESP32-01): ");
        String oldId = scanner.nextLine().trim().toUpperCase();

        Resource res = resourceDAO.getResourceById(oldId);
        if (res == null) {
            System.out.println("Resource with ID '" + oldId + "' not found!");
            return;
        }

        System.out.println("Found: " + res.getName() + " [" + res.getCategory() + "]");
        System.out.print("Enter NEW Resource ID (e.g. LAB-ESP32-UPGRADED): ");
        String newId = scanner.nextLine().trim().toUpperCase();

        if (newId.isEmpty() || newId.equals(oldId)) {
            System.out.println("Invalid new ID.");
            return;
        }

        System.out.println("\nExecuting single SQL statement:");
        System.out.println(">> UPDATE resources SET resource_id = '" + newId + "' WHERE resource_id = '" + oldId + "';");

        boolean ok = resourceDAO.updateResourceIdCascading(oldId, newId);
        if (ok) {
            System.out.println("\nSUCCESS: Cascading update executed!");
            System.out.println("Now verifying child tables in MySQL:");

            Resource updated = resourceDAO.getResourceById(newId);
            if (updated != null) {
                System.out.println(">> Master Table 'resources': Found " + updated.getResourceId());
                System.out.println(">> Child Table Details: " + updated.getSpecificAttributeSummary());
                System.out.println(">> Foreign Key automatically updated across child tables via ON UPDATE CASCADE!");
            }
        } else {
            System.out.println("Cascading update failed.");
        }
    }

    private static void viewAuditTrail() {
        System.out.println("\n--- [DBMS AUDIT TRAIL LOGS] ---");
        System.out.println("Audit trail entries generated automatically by MySQL Database Triggers:");
        List<AuditRecord> audits = auditDAO.getRecentAuditLogs(20);
        if (audits.isEmpty()) {
            System.out.println("No audit logs found.");
            return;
        }

        for (AuditRecord a : audits) {
            a.displayDetails();
        }
    }

    private static void viewCheckouts() {
        System.out.println("\n--- [ALL CHECKOUT TRANSACTIONS] ---");
        List<CheckoutTransaction> list = checkoutDAO.getAllCheckouts();
        if (list.isEmpty()) {
            System.out.println("No checkout records found.");
            return;
        }

        for (CheckoutTransaction tx : list) {
            tx.displayDetails();
            System.out.println("--------------------------------------------------------------------------");
        }
    }
}
