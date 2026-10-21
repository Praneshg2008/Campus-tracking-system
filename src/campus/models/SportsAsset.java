package campus.models;

/**
 * Specialized Child Class: SportsAsset (Tournament gear, kits, safety equipment)
 * Handles specialized attributes like sport type, wear condition, and tournament qualification.
 */
public class SportsAsset extends Resource {
    private String sportName;
    private String wearCondition; // 'NEW', 'GOOD', 'FAIR', 'NEEDS_REPAIR'
    private boolean tournamentGrade;

    public SportsAsset(String resourceId, String name, String department, String status, double dailyLateFee,
                       String sportName, String wearCondition, boolean tournamentGrade) {
        super(resourceId, name, department, "SPORTS_ASSET", status, dailyLateFee);
        this.sportName = sportName;
        this.wearCondition = wearCondition;
        this.tournamentGrade = tournamentGrade;
    }

    @Override
    public void displayDetails() {
        System.out.printf("[SPORTS]  ID: %-14s | Name: %-32s | Dept: %-18s%n", resourceId, name, department);
        System.out.printf("          Status: %-10s | Base Fee: $%.2f/day | Sport: %-20s%n", status, dailyLateFee, sportName);
        System.out.printf("          Condition: %-7s | Tournament Grade: %s%n", wearCondition, (tournamentGrade ? "YES" : "NO"));
    }

    @Override
    public double calculateLateFee(long overdueDays) {
        if (overdueDays <= 0) return 0.0;
        double baseFee = overdueDays * dailyLateFee;
        // Tournament assets held overdue have a 20% surge penalty due to match scheduling constraints
        if (tournamentGrade && overdueDays > 2) {
            baseFee *= 1.20;
        }
        return baseFee;
    }

    @Override
    public String getSpecificAttributeSummary() {
        return String.format("Sport: %s, Wear: %s, Tournament: %s", sportName, wearCondition, (tournamentGrade ? "Yes" : "No"));
    }

    public String getSportName() {
        return sportName;
    }

    public void setSportName(String sportName) {
        this.sportName = sportName;
    }

    public String getWearCondition() {
        return wearCondition;
    }

    public void setWearCondition(String wearCondition) {
        this.wearCondition = wearCondition;
    }

    public boolean isTournamentGrade() {
        return tournamentGrade;
    }

    public void setTournamentGrade(boolean tournamentGrade) {
        this.tournamentGrade = tournamentGrade;
    }
}
