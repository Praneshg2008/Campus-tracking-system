package campus.models;

/**
 * Specialized Child Class: LabKit (Microcontrollers, sensors, oscilloscopes, FPGA kits)
 * Handles specialized attributes like component count, microcontroller architecture, and calibration requirements.
 */
public class LabKit extends Resource {
    private int componentsCount;
    private String microcontrollerType;
    private boolean requiresCalibration;

    public LabKit(String resourceId, String name, String department, String status, double dailyLateFee,
                  int componentsCount, String microcontrollerType, boolean requiresCalibration) {
        super(resourceId, name, department, "LAB_KIT", status, dailyLateFee);
        this.componentsCount = componentsCount;
        this.microcontrollerType = microcontrollerType;
        this.requiresCalibration = requiresCalibration;
    }

    @Override
    public void displayDetails() {
        System.out.printf("[LAB KIT] ID: %-14s | Name: %-32s | Dept: %-18s%n", resourceId, name, department);
        System.out.printf("          Status: %-10s | Base Fee: $%.2f/day | MC/Board: %-20s%n", status, dailyLateFee, microcontrollerType);
        System.out.printf("          Components: %-5d | Calibration Needed: %s%n", componentsCount, (requiresCalibration ? "YES (Strict Lab Standard)" : "NO"));
    }

    @Override
    public double calculateLateFee(long overdueDays) {
        if (overdueDays <= 0) return 0.0;
        double baseFee = overdueDays * dailyLateFee;
        // If lab equipment requires regular calibration and is held overdue, apply a $25 re-calibration fee
        if (requiresCalibration) {
            baseFee += 25.0;
        }
        return baseFee;
    }

    @Override
    public String getSpecificAttributeSummary() {
        return String.format("MC: %s, Parts: %d, Calib: %s", microcontrollerType, componentsCount, (requiresCalibration ? "Yes" : "No"));
    }

    public int getComponentsCount() {
        return componentsCount;
    }

    public void setComponentsCount(int componentsCount) {
        this.componentsCount = componentsCount;
    }

    public String getMicrocontrollerType() {
        return microcontrollerType;
    }

    public void setMicrocontrollerType(String microcontrollerType) {
        this.microcontrollerType = microcontrollerType;
    }

    public boolean isRequiresCalibration() {
        return requiresCalibration;
    }

    public void setRequiresCalibration(boolean requiresCalibration) {
        this.requiresCalibration = requiresCalibration;
    }
}
