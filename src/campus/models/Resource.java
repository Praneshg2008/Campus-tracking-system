package campus.models;

/**
 * Abstract Base Class representing a high-value campus asset.
 * Demonstrates OOP Abstraction, Polymorphism, and Encapsulation.
 */
public abstract class Resource {
    protected String resourceId;
    protected String name;
    protected String department;
    protected String category;
    protected String status; // 'AVAILABLE', 'CHECKED_OUT', 'MAINTENANCE', 'RETIRED'
    protected double dailyLateFee;

    public Resource(String resourceId, String name, String department, String category, String status, double dailyLateFee) {
        this.resourceId = resourceId;
        this.name = name;
        this.department = department;
        this.category = category;
        this.status = status;
        this.dailyLateFee = dailyLateFee;
    }

    // Abstract methods to be specialized by child classes (LabKit, SportsAsset, MediaAsset)
    public abstract void displayDetails();
    public abstract double calculateLateFee(long overdueDays);
    public abstract String getSpecificAttributeSummary();

    // Getters and Setters
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(this.status);
    }
}
