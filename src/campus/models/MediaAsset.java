package campus.models;

/**
 * Specialized Child Class: MediaAsset (DSLR Cameras, Cinema Kits, Lens packages)
 * Handles specialized attributes like lens model, shutter count, and max resolution.
 */
public class MediaAsset extends Resource {
    private String lensModel;
    private int shutterCount;
    private String maxResolution;

    public MediaAsset(String resourceId, String name, String department, String status, double dailyLateFee,
                      String lensModel, int shutterCount, String maxResolution) {
        super(resourceId, name, department, "MEDIA_ASSET", status, dailyLateFee);
        this.lensModel = lensModel;
        this.shutterCount = shutterCount;
        this.maxResolution = maxResolution;
    }

    @Override
    public void displayDetails() {
        System.out.printf("[MEDIA]   ID: %-14s | Name: %-32s | Dept: %-18s%n", resourceId, name, department);
        System.out.printf("          Status: %-10s | Base Fee: $%.2f/day | Lens: %-20s%n", status, dailyLateFee, lensModel);
        System.out.printf("          Shutter: %-8d | Max Res: %-12s%n", shutterCount, maxResolution);
    }

    @Override
    public double calculateLateFee(long overdueDays) {
        if (overdueDays <= 0) return 0.0;
        // High-value electronic and optical equipment has an overdue depreciation factor
        double baseFee = overdueDays * dailyLateFee;
        if (overdueDays > 3) {
            baseFee += 30.0; // High-value insurance deposit hold penalty
        }
        return baseFee;
    }

    @Override
    public String getSpecificAttributeSummary() {
        return String.format("Lens: %s, Shutter: %d, Res: %s", lensModel, shutterCount, maxResolution);
    }

    public String getLensModel() {
        return lensModel;
    }

    public void setLensModel(String lensModel) {
        this.lensModel = lensModel;
    }

    public int getShutterCount() {
        return shutterCount;
    }

    public void setShutterCount(int shutterCount) {
        this.shutterCount = shutterCount;
    }

    public String getMaxResolution() {
        return maxResolution;
    }

    public void setMaxResolution(String maxResolution) {
        this.maxResolution = maxResolution;
    }
}
