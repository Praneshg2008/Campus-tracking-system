import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Main - Menu-driven Command Line Interface (CLI) for the
 * Campus Equipment & Resource Tracking System.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ResourceOperations resourceOps = new ResourceOperations();
    private static final CheckoutOperations checkoutOps = new CheckoutOperations();

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("          CAMPUS EQUIPMENT & RESOURCE TRACKING SYSTEM                     ");
        System.out.println("             OOP & DBMS Project (Core Java + MySQL)                       ");
        System.out.println("==========================================================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("Enter your choice (0-8): ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    equipmentMenu();
                    break;
                case "2":
                    studentMenu();
                    break;
                case "3":
                    issueEquipment();
                    break;
                case "4":
                    returnEquipment();
                    break;
                case "5":
                    trackAvailableAndBorrowed();
                    break;
                case "6":
                    viewBorrowingHistory();
                    break;
                case "7":
                    maintenanceMenu();
                    break;
                case "8":
                    viewStatistics();
                    break;
                case "0":
                    running = false;
                    System.out.println("\nExiting Campus Tracking System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 0 and 8.");
            }
            System.out.println();
        }
    }

    private static void printMainMenu() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("                               MAIN MENU                                  ");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(" [1] Equipment Management (Add, View, Update, Delete, Search)");
        System.out.println(" [2] Student Management (Register, View, Update, Delete)");
        System.out.println(" [3] Issue Equipment to Student");
        System.out.println(" [4] Record Equipment Return & Calculate Overdue Fine");
        System.out.println(" [5] Track Available & Borrowed Equipment");
        System.out.println(" [6] View Borrowing History");
        System.out.println(" [7] Maintenance Records (Add & View)");
        System.out.println(" [8] View System Statistics");
        System.out.println(" [0] Exit");
        System.out.println("--------------------------------------------------------------------------");
    }

    // =========================================================================
    // 1. Equipment Management Sub-Menu
    // =========================================================================
    private static void equipmentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [EQUIPMENT MANAGEMENT] ---");
            System.out.println(" [1] View All Equipment");
            System.out.println(" [2] Add New Equipment");
            System.out.println(" [3] Update Equipment");
            System.out.println(" [4] Delete Equipment");
            System.out.println(" [5] Search Equipment");
            System.out.println(" [0] Back to Main Menu");
            System.out.print("Enter choice: ");
            if (!scanner.hasNextLine()) return;
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllEquipment();
                    break;
                case "2":
                    addEquipment();
                    break;
                case "3":
                    updateEquipment();
                    break;
                case "4":
                    deleteEquipment();
                    break;
                case "5":
                    searchEquipment();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please select 0-5.");
            }
        }
    }

    private static void viewAllEquipment() {
        System.out.println("\n--- [ALL CAMPUS EQUIPMENT] ---");
        List<Resource> resources = resourceOps.getAllResources();
        if (resources.isEmpty()) {
            System.out.println("No equipment found in the database.");
            return;
        }
        for (Resource r : resources) {
            r.displayDetails();
        }
    }

    private static void addEquipment() {
        System.out.println("\n--- [ADD NEW EQUIPMENT] ---");
        System.out.print("Enter Equipment ID (e.g. EQ-107): ");
        String id = scanner.nextLine().trim().toUpperCase();
        if (id.isEmpty()) {
            System.out.println("Error: Equipment ID cannot be empty.");
            return;
        }
        if (resourceOps.getResourceById(id) != null) {
            System.out.println("Error: Equipment with ID '" + id + "' already exists.");
            return;
        }

        System.out.print("Enter Equipment Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Error: Equipment name cannot be empty.");
            return;
        }

        System.out.print("Enter Category (e.g. Lab Equipment, Media Gear, Sports Gear): ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty()) category = "General";

        System.out.print("Enter Department (e.g. CSE Lab, ECE Lab, Sports Dept): ");
        String dept = scanner.nextLine().trim();
        if (dept.isEmpty()) dept = "General";

        System.out.print("Enter Daily Overdue Fine Rate (Rs.): ");
        double dailyFee = readPositiveDouble(10.0);

        Resource resource = new Resource(id, name, category, dept, "AVAILABLE", dailyFee);
        if (resourceOps.addResource(resource)) {
            System.out.println("SUCCESS: Equipment '" + id + "' added successfully.");
        } else {
            System.out.println("FAILED: Could not add equipment.");
        }
    }

    private static void updateEquipment() {
        System.out.println("\n--- [UPDATE EQUIPMENT] ---");
        System.out.print("Enter Equipment ID to update: ");
        String id = scanner.nextLine().trim().toUpperCase();
        Resource existing = resourceOps.getResourceById(id);
        if (existing == null) {
            System.out.println("Error: Equipment with ID '" + id + "' not found.");
            return;
        }

        System.out.println("Current Details:");
        existing.displayDetails();
        System.out.println("(Press ENTER to keep current value)");

        System.out.print("New Name [" + existing.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) existing.setName(name);

        System.out.print("New Category [" + existing.getCategory() + "]: ");
        String category = scanner.nextLine().trim();
        if (!category.isEmpty()) existing.setCategory(category);

        System.out.print("New Department [" + existing.getDepartment() + "]: ");
        String dept = scanner.nextLine().trim();
        if (!dept.isEmpty()) existing.setDepartment(dept);

        System.out.print("New Daily Fine Rate (Rs.) [" + existing.getDailyLateFee() + "]: ");
        String feeStr = scanner.nextLine().trim();
        if (!feeStr.isEmpty()) {
            try {
                double fee = Double.parseDouble(feeStr);
                if (fee >= 0) existing.setDailyLateFee(fee);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Keeping existing rate.");
            }
        }

        System.out.print("New Status (AVAILABLE / BORROWED / MAINTENANCE) [" + existing.getStatus() + "]: ");
        String status = scanner.nextLine().trim().toUpperCase();
        if (status.equals("AVAILABLE") || status.equals("BORROWED") || status.equals("MAINTENANCE")) {
            existing.setStatus(status);
        }

        if (resourceOps.updateResource(existing)) {
            System.out.println("SUCCESS: Equipment '" + id + "' updated successfully.");
        } else {
            System.out.println("FAILED: Could not update equipment.");
        }
    }

    private static void deleteEquipment() {
        System.out.println("\n--- [DELETE EQUIPMENT] ---");
        System.out.print("Enter Equipment ID to delete: ");
        String id = scanner.nextLine().trim().toUpperCase();
        Resource existing = resourceOps.getResourceById(id);
        if (existing == null) {
            System.out.println("Error: Equipment with ID '" + id + "' not found.");
            return;
        }
        if ("BORROWED".equalsIgnoreCase(existing.getStatus())) {
            System.out.println("Error: Cannot delete equipment while it is currently borrowed.");
            return;
        }

        if (resourceOps.deleteResource(id)) {
            System.out.println("SUCCESS: Equipment '" + id + "' deleted.");
        } else {
            System.out.println("FAILED: Could not delete equipment.");
        }
    }

    private static void searchEquipment() {
        System.out.println("\n--- [SEARCH EQUIPMENT] ---");
        System.out.print("Enter search keyword (ID, Name, Category, or Department): ");
        String keyword = scanner.nextLine().trim();
        if (keyword.isEmpty()) {
            System.out.println("Search keyword cannot be empty.");
            return;
        }

        List<Resource> results = resourceOps.searchResources(keyword);
        if (results.isEmpty()) {
            System.out.println("No matching equipment found for '" + keyword + "'.");
            return;
        }
        System.out.println("Matching Equipment (" + results.size() + " found):");
        for (Resource r : results) {
            r.displayDetails();
        }
    }

    // =========================================================================
    // 2. Student Management Sub-Menu
    // =========================================================================
    private static void studentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [STUDENT MANAGEMENT] ---");
            System.out.println(" [1] View All Students");
            System.out.println(" [2] Register New Student");
            System.out.println(" [3] Update Student Details");
            System.out.println(" [4] Delete Student");
            System.out.println(" [0] Back to Main Menu");
            System.out.print("Enter choice: ");
            if (!scanner.hasNextLine()) return;
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllStudents();
                    break;
                case "2":
                    registerStudent();
                    break;
                case "3":
                    updateStudent();
                    break;
                case "4":
                    deleteStudent();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please select 0-4.");
            }
        }
    }

    private static void viewAllStudents() {
        System.out.println("\n--- [REGISTERED STUDENTS] ---");
        List<Student> students = checkoutOps.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students registered.");
            return;
        }
        for (Student s : students) {
            s.displayDetails();
        }
    }

    private static void registerStudent() {
        System.out.println("\n--- [REGISTER NEW STUDENT] ---");
        System.out.print("Enter Student ID (e.g. STU-105): ");
        String id = scanner.nextLine().trim().toUpperCase();
        if (id.isEmpty()) {
            System.out.println("Error: Student ID cannot be empty.");
            return;
        }
        if (checkoutOps.getStudentById(id) != null) {
            System.out.println("Error: Student with ID '" + id + "' is already registered.");
            return;
        }

        System.out.print("Enter Student Full Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Error: Student name cannot be empty.");
            return;
        }

        System.out.print("Enter Department (e.g. CSE, ECE, Mechanical): ");
        String dept = scanner.nextLine().trim();
        if (dept.isEmpty()) dept = "CSE";

        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) email = id.toLowerCase() + "@college.edu";

        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine().trim();
        if (phone.isEmpty()) phone = "N/A";

        Student student = new Student(id, name, dept, email, phone);
        if (checkoutOps.addStudent(student)) {
            System.out.println("SUCCESS: Student '" + name + "' (" + id + ") registered successfully.");
        } else {
            System.out.println("FAILED: Could not register student.");
        }
    }

    private static void updateStudent() {
        System.out.println("\n--- [UPDATE STUDENT DETAILS] ---");
        System.out.print("Enter Student ID to update: ");
        String id = scanner.nextLine().trim().toUpperCase();
        Student existing = checkoutOps.getStudentById(id);
        if (existing == null) {
            System.out.println("Error: Student with ID '" + id + "' not found.");
            return;
        }

        System.out.println("Current Details:");
        existing.displayDetails();
        System.out.println("(Press ENTER to keep current value)");

        System.out.print("New Name [" + existing.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) existing.setName(name);

        System.out.print("New Department [" + existing.getDepartment() + "]: ");
        String dept = scanner.nextLine().trim();
        if (!dept.isEmpty()) existing.setDepartment(dept);

        System.out.print("New Email [" + existing.getEmail() + "]: ");
        String email = scanner.nextLine().trim();
        if (!email.isEmpty()) existing.setEmail(email);

        System.out.print("New Phone [" + existing.getPhone() + "]: ");
        String phone = scanner.nextLine().trim();
        if (!phone.isEmpty()) existing.setPhone(phone);

        if (checkoutOps.updateStudent(existing)) {
            System.out.println("SUCCESS: Student '" + id + "' updated successfully.");
        } else {
            System.out.println("FAILED: Could not update student.");
        }
    }

    private static void deleteStudent() {
        System.out.println("\n--- [DELETE STUDENT] ---");
        System.out.print("Enter Student ID to delete: ");
        String id = scanner.nextLine().trim().toUpperCase();
        Student existing = checkoutOps.getStudentById(id);
        if (existing == null) {
            System.out.println("Error: Student with ID '" + id + "' not found.");
            return;
        }
        if (checkoutOps.studentHasActiveCheckouts(id)) {
            System.out.println("Error: Cannot delete student while they have unreturned borrowed equipment.");
            return;
        }

        if (checkoutOps.deleteStudent(id)) {
            System.out.println("SUCCESS: Student '" + id + "' deleted.");
        } else {
            System.out.println("FAILED: Could not delete student.");
        }
    }

    // =========================================================================
    // 3. Issue Equipment to Student
    // =========================================================================
    private static void issueEquipment() {
        System.out.println("\n--- [ISSUE EQUIPMENT TO STUDENT] ---");
        System.out.print("Enter Equipment ID to issue: ");
        String resourceId = scanner.nextLine().trim().toUpperCase();

        Resource resource = resourceOps.getResourceById(resourceId);
        if (resource == null) {
            System.out.println("Error: Equipment with ID '" + resourceId + "' not found.");
            return;
        }
        if (!resource.isAvailable()) {
            System.out.println("Error: Equipment '" + resource.getName() + "' is currently " + resource.getStatus() + " and cannot be issued.");
            return;
        }

        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine().trim().toUpperCase();
        Student student = checkoutOps.getStudentById(studentId);
        if (student == null) {
            System.out.println("Error: Student with ID '" + studentId + "' is not registered. Please register the student first.");
            return;
        }

        System.out.print("Enter Borrowing Duration in Days (e.g. 5): ");
        int days = readPositiveInt(5);

        if (checkoutOps.issueEquipment(resourceId, studentId, days)) {
            System.out.println("SUCCESS: '" + resource.getName() + "' issued to " + student.getName() + " (" + studentId + ").");
            System.out.println("Due Date: " + LocalDate.now().plusDays(days));
        } else {
            System.out.println("FAILED: Could not issue equipment.");
        }
    }

    // =========================================================================
    // 4. Record Equipment Return & Calculate Overdue Fine
    // =========================================================================
    private static void returnEquipment() {
        System.out.println("\n--- [RETURN EQUIPMENT & CALCULATE FINE] ---");
        System.out.println("Currently Borrowed Equipment:");
        boolean hasActive = checkoutOps.displayActiveCheckouts();
        if (!hasActive) {
            System.out.println("There are currently no borrowed equipment items to return.");
            return;
        }

        System.out.print("Enter Checkout ID to return: ");
        int checkoutId = readPositiveInt(-1);
        if (checkoutId <= 0) {
            System.out.println("Invalid Checkout ID.");
            return;
        }

        System.out.println("Select Return Date Option:");
        System.out.println(" [1] Return Today (" + LocalDate.now() + ")");
        System.out.println(" [2] Enter Custom Return Date (to test overdue fine calculation)");
        System.out.print("Choice: ");
        String dateChoice = scanner.nextLine().trim();

        LocalDate returnDate = LocalDate.now();
        if ("2".equals(dateChoice)) {
            System.out.print("Enter Return Date (YYYY-MM-DD): ");
            String dateInput = scanner.nextLine().trim();
            try {
                returnDate = LocalDate.parse(dateInput);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Using today's date (" + returnDate + ").");
            }
        }

        double fine = checkoutOps.returnEquipment(checkoutId, returnDate);
        if (fine >= 0) {
            System.out.println("\nSUCCESS: Equipment returned on " + returnDate + ".");
            System.out.printf("Overdue Fine Calculated: Rs. %.2f%n", fine);
            if (fine == 0.0) {
                System.out.println("Returned on or before the due date. No fine charged.");
            }
        } else {
            System.out.println("FAILED: Active Checkout ID #" + checkoutId + " was not found.");
        }
    }

    // =========================================================================
    // 5. Track Available & Borrowed Equipment
    // =========================================================================
    private static void trackAvailableAndBorrowed() {
        System.out.println("\n--- [AVAILABLE EQUIPMENT] ---");
        List<Resource> available = resourceOps.getResourcesByStatus("AVAILABLE");
        if (available.isEmpty()) {
            System.out.println("No available equipment right now.");
        } else {
            for (Resource r : available) {
                r.displayDetails();
            }
        }

        System.out.println("\n--- [CURRENTLY BORROWED EQUIPMENT] ---");
        if (!checkoutOps.displayActiveCheckouts()) {
            System.out.println("No equipment is currently borrowed.");
        }
    }

    // =========================================================================
    // 6. View Borrowing History
    // =========================================================================
    private static void viewBorrowingHistory() {
        System.out.println("\n--- [BORROWING HISTORY] ---");
        System.out.print("Enter Student ID to filter (or press ENTER to view all records): ");
        String studentId = scanner.nextLine().trim().toUpperCase();
        System.out.println();
        checkoutOps.displayBorrowingHistory(studentId);
    }

    // =========================================================================
    // 7. Maintenance Records
    // =========================================================================
    private static void maintenanceMenu() {
        System.out.println("\n--- [MAINTENANCE RECORDS] ---");
        System.out.println(" [1] Add Maintenance Record");
        System.out.println(" [2] View Maintenance Records");
        System.out.println(" [0] Back to Main Menu");
        System.out.print("Enter choice: ");
        if (!scanner.hasNextLine()) return;
        String choice = scanner.nextLine().trim();

        if ("1".equals(choice)) {
            System.out.print("Enter Equipment ID: ");
            String resourceId = scanner.nextLine().trim().toUpperCase();
            Resource res = resourceOps.getResourceById(resourceId);
            if (res == null) {
                System.out.println("Error: Equipment with ID '" + resourceId + "' not found.");
                return;
            }
            if ("BORROWED".equalsIgnoreCase(res.getStatus())) {
                System.out.println("Error: Equipment is currently borrowed. Return it before logging maintenance.");
                return;
            }

            System.out.print("Enter Issue / Maintenance Description: ");
            String desc = scanner.nextLine().trim();
            if (desc.isEmpty()) desc = "Routine inspection";

            System.out.print("Enter Technician Name: ");
            String tech = scanner.nextLine().trim();
            if (tech.isEmpty()) tech = "Lab Technician";

            System.out.print("Enter Maintenance Cost (Rs.): ");
            double cost = readPositiveDouble(0.0);

            System.out.println("Select Equipment Status After Maintenance:");
            System.out.println(" [1] AVAILABLE (Repaired and ready for use)");
            System.out.println(" [2] MAINTENANCE (Under repair)");
            System.out.print("Choice: ");
            String statusChoice = scanner.nextLine().trim();
            String statusAfter = "2".equals(statusChoice) ? "MAINTENANCE" : "AVAILABLE";

            if (resourceOps.addMaintenanceRecord(resourceId, desc, tech, cost, LocalDate.now(), statusAfter)) {
                System.out.println("SUCCESS: Maintenance record added and equipment status set to " + statusAfter + ".");
            } else {
                System.out.println("FAILED: Could not add maintenance record.");
            }
        } else if ("2".equals(choice)) {
            System.out.print("Enter Equipment ID to filter (or press ENTER for all): ");
            String filter = scanner.nextLine().trim().toUpperCase();
            System.out.println();
            resourceOps.displayMaintenanceRecords(filter);
        }
    }

    // =========================================================================
    // 8. System Statistics
    // =========================================================================
    private static void viewStatistics() {
        System.out.println("\n--- [SYSTEM STATISTICS] ---");
        resourceOps.displayStatistics();
    }

    // =========================================================================
    // Input Validation Helpers
    // =========================================================================
    private static int readPositiveInt(int defaultValue) {
        String input = scanner.nextLine().trim();
        try {
            int val = Integer.parseInt(input);
            if (val > 0) return val;
        } catch (NumberFormatException ignored) {}
        if (defaultValue > 0) {
            System.out.println("Invalid number entered. Using default: " + defaultValue);
        }
        return defaultValue;
    }

    private static double readPositiveDouble(double defaultValue) {
        String input = scanner.nextLine().trim();
        try {
            double val = Double.parseDouble(input);
            if (val >= 0) return val;
        } catch (NumberFormatException ignored) {}
        System.out.printf("Invalid amount entered. Using default: Rs. %.2f%n", defaultValue);
        return defaultValue;
    }
}
