package campus.models;

import java.time.LocalDate;

/**
 * Model representing a student checkout transaction.
 * Tracks checkout period, due dates, returns, and calculated late fees.
 */
public class CheckoutTransaction {
    private int checkoutId;
    private String resourceId;
    private String studentId;
    private String studentName;
    private LocalDate checkoutDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double lateFee;
    private String checkoutStatus; // 'ACTIVE', 'RETURNED'

    public CheckoutTransaction(int checkoutId, String resourceId, String studentId, String studentName,
                               LocalDate checkoutDate, LocalDate dueDate, LocalDate returnDate,
                               double lateFee, String checkoutStatus) {
        this.checkoutId = checkoutId;
        this.resourceId = resourceId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.checkoutDate = checkoutDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.lateFee = lateFee;
        this.checkoutStatus = checkoutStatus;
    }

    public void displayDetails() {
        System.out.printf("[TX #%-4d] Asset: %-14s | Student: %s (%s)%n", checkoutId, resourceId, studentName, studentId);
        System.out.printf("           Issued: %s | Due: %s | Returned: %s%n", checkoutDate, dueDate, (returnDate != null ? returnDate.toString() : "STILL OUT"));
        System.out.printf("           Status: %-8s | Assessed Late Fee: $%.2f%n", checkoutStatus, lateFee);
    }

    public int getCheckoutId() {
        return checkoutId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public double getLateFee() {
        return lateFee;
    }

    public String getCheckoutStatus() {
        return checkoutStatus;
    }
}
