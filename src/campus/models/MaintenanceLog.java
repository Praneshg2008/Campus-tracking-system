package campus.models;

import java.time.LocalDate;

/**
 * Model representing a Maintenance State Log.
 * Tracks equipment servicing, technician details, maintenance costs, and resultant status.
 */
public class MaintenanceLog {
    private int logId;
    private String resourceId;
    private String issueDescription;
    private String technicianName;
    private double maintenanceCost;
    private LocalDate serviceDate;
    private String statusAfter; // 'AVAILABLE', 'MAINTENANCE', 'RETIRED'

    public MaintenanceLog(int logId, String resourceId, String issueDescription, String technicianName,
                          double maintenanceCost, LocalDate serviceDate, String statusAfter) {
        this.logId = logId;
        this.resourceId = resourceId;
        this.issueDescription = issueDescription;
        this.technicianName = technicianName;
        this.maintenanceCost = maintenanceCost;
        this.serviceDate = serviceDate;
        this.statusAfter = statusAfter;
    }

    public MaintenanceLog(String resourceId, String issueDescription, String technicianName,
                          double maintenanceCost, LocalDate serviceDate, String statusAfter) {
        this(0, resourceId, issueDescription, technicianName, maintenanceCost, serviceDate, statusAfter);
    }

    public void displayDetails() {
        System.out.printf("[LOG #%-3d] Asset: %-14s | Date: %s | Cost: $%.2f%n", logId, resourceId, serviceDate, maintenanceCost);
        System.out.printf("          Tech: %-15s | Status After: %-12s%n", technicianName, statusAfter);
        System.out.printf("          Issue / Action: %s%n", issueDescription);
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public double getMaintenanceCost() {
        return maintenanceCost;
    }

    public void setMaintenanceCost(double maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(LocalDate serviceDate) {
        this.serviceDate = serviceDate;
    }

    public String getStatusAfter() {
        return statusAfter;
    }

    public void setStatusAfter(String statusAfter) {
        this.statusAfter = statusAfter;
    }
}
