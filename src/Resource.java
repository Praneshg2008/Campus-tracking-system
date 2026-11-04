/**
 * Resource - Represents a piece of campus equipment.
 * Demonstrates encapsulation with private fields, constructors, getters, setters,
 * and simple overdue fine calculation.
 */
public class Resource {
    private String resourceId;
    private String name;
    private String category;
    private String department;
    private String status; // AVAILABLE, BORROWED, MAINTENANCE
    private double dailyLateFee;

    public Resource(String resourceId, String name, String category, String department, String status, double dailyLateFee) {
        this.resourceId = resourceId;
        this.name = name;
        this.category = category;
        this.department = department;
        this.status = status;
        this.dailyLateFee = dailyLateFee;
    }

    /**
     * Calculates overdue fine based on number of days past the due date.
     */
    public double calculateLateFee(long overdueDays) {
        if (overdueDays <= 0) {
            return 0.0;
        }
        return overdueDays * dailyLateFee;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }

    public void displayDetails() {
        System.out.printf("ID: %-10s | Name: %-30s | Category: %-15s | Dept: %-12s | Status: %-12s | Fine/Day: Rs. %.2f%n",
                resourceId, name, category, department, status, dailyLateFee);
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getDailyLateFee() {
        return dailyLateFee;
    }

    public void setDailyLateFee(double dailyLateFee) {
        this.dailyLateFee = dailyLateFee;
    }
}
