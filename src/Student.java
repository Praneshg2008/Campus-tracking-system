/**
 * Student - Represents a registered college student who can borrow campus equipment.
 */
public class Student {
    private String studentId;
    private String name;
    private String department;
    private String email;
    private String phone;

    public Student(String studentId, String name, String department, String email, String phone) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.email = email;
        this.phone = phone;
    }

    public void displayDetails() {
        System.out.printf("ID: %-10s | Name: %-22s | Dept: %-12s | Email: %-26s | Phone: %s%n",
                studentId, name, department, email, phone);
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
