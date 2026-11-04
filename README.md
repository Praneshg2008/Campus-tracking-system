# Campus Equipment & Resource Tracking System

A simple command-line application built using **Core Java**, **JDBC**, and **MySQL** for a second-year Computer Science & Engineering (CSE) **Object-Oriented Programming (OOP)** and **Database Management Systems (DBMS)** course project.

---

## 1. Project Structure

```
CampusTracking/
│
├── src/
│   ├── Main.java
│   ├── DBConnection.java
│   ├── Resource.java
│   ├── Student.java
│   ├── ResourceOperations.java
│   └── CheckoutOperations.java
│
├── database/
│   └── schema.sql
│
├── lib/
│   └── mysql-connector.jar
│
├── db.properties
├── run.sh
└── README.md
```

---

## 2. Project Objective

College laboratories, media clubs, and sports departments regularly lend equipment (such as microcontroller kits, oscilloscopes, cameras, and sports gear) to students. Keeping track of who borrowed which item, when it is due back, whether any overdue fine applies, and which items are under repair is difficult using paper registers.

The objective of this project is to build a straightforward, menu-driven system that:
- Maintains a catalog of campus equipment (`resources`) and registered students (`students`).
- Issues available equipment to students with a due date and records returns (`checkouts`).
- Calculates a simple daily overdue fine if equipment is returned after its due date.
- Logs basic equipment repair and servicing events (`maintenance`).
- Demonstrates core **Java OOP fundamentals** (classes, objects, encapsulation, constructors, getters/setters, methods) and **MySQL DBMS concepts** (primary keys, foreign keys, `INSERT`, `SELECT`, `UPDATE`, `DELETE`, `WHERE`, `JOIN`, `COUNT`, `GROUP BY`, and JDBC transactions).

---

## 3. Technologies Used

- **Programming Language**: Core Java
- **Database**: MySQL 8.0
- **Database Connectivity**: JDBC (`lib/mysql-connector.jar`)
- **User Interface**: Command-Line Interface (CLI) using `java.util.Scanner`
- **Build & Execution**: Bash script (`run.sh`)

No external frameworks (Spring, Hibernate, Maven, Swing, or JavaFX) are used.

---

## 4. Database Tables and Relationships

The MySQL database (`campus_resource_db`) uses **four tables** defined in `database/schema.sql`:

### 1. `students`
Stores registered students who are allowed to borrow equipment.
- `student_id` (`VARCHAR(20)`, **Primary Key**) — Unique student roll/ID number (e.g., `STU-101`).
- `name` (`VARCHAR(100)`, `NOT NULL`) — Full name of the student.
- `department` (`VARCHAR(50)`, `NOT NULL`) — Student's academic department (e.g., `CSE`, `ECE`).
- `email` (`VARCHAR(100)`, `NOT NULL`) — College email address.
- `phone` (`VARCHAR(20)`, `NOT NULL`) — Contact number.

### 2. `resources`
Stores all trackable campus equipment items.
- `resource_id` (`VARCHAR(20)`, **Primary Key**) — Unique equipment ID (e.g., `EQ-101`).
- `name` (`VARCHAR(100)`, `NOT NULL`) — Equipment name.
- `category` (`VARCHAR(50)`, `NOT NULL`) — Category such as `Lab Equipment`, `Media Gear`, or `Sports Gear`.
- `department` (`VARCHAR(50)`, `NOT NULL`) — Department managing the equipment.
- `status` (`VARCHAR(20)`, `NOT NULL`) — Current status: `AVAILABLE`, `BORROWED`, or `MAINTENANCE`.
- `daily_late_fee` (`DECIMAL(8,2)`, `NOT NULL`) — Overdue fine per day (in Rs.).

### 3. `checkouts`
Records borrowing and return transactions linking students and equipment.
- `checkout_id` (`INT AUTO_INCREMENT`, **Primary Key**) — Unique checkout transaction ID.
- `resource_id` (`VARCHAR(20)`, **Foreign Key** referencing `resources(resource_id)`) — Equipment borrowed.
- `student_id` (`VARCHAR(20)`, **Foreign Key** referencing `students(student_id)`) — Student who borrowed it.
- `checkout_date` (`DATE`, `NOT NULL`) — Date when the equipment was issued.
- `due_date` (`DATE`, `NOT NULL`) — Date by which the equipment must be returned.
- `return_date` (`DATE`, `NULL`) — Actual return date (`NULL` while still borrowed).
- `late_fee` (`DECIMAL(8,2)`) — Calculated overdue fine upon return.
- `status` (`VARCHAR(20)`, `NOT NULL`) — `ACTIVE` (currently borrowed) or `RETURNED`.

### 4. `maintenance`
Stores basic repair and servicing logs for equipment.
- `maintenance_id` (`INT AUTO_INCREMENT`, **Primary Key**) — Unique maintenance record ID.
- `resource_id` (`VARCHAR(20)`, **Foreign Key** referencing `resources(resource_id)`) — Equipment serviced.
- `issue_description` (`VARCHAR(255)`, `NOT NULL`) — Description of the repair or inspection.
- `technician_name` (`VARCHAR(100)`, `NOT NULL`) — Name of the technician.
- `maintenance_cost` (`DECIMAL(8,2)`) — Cost of maintenance.
- `service_date` (`DATE`, `NOT NULL`) — Date of service.
- `status_after` (`VARCHAR(20)`, `NOT NULL`) — Equipment status after logging (`AVAILABLE` or `MAINTENANCE`).

### Entity Relationships
- **One-to-Many (`resources` -> `checkouts`)**: One piece of equipment can be borrowed many times over its lifecycle.
- **One-to-Many (`students` -> `checkouts`)**: One registered student can borrow multiple items over time.
- **One-to-Many (`resources` -> `maintenance`)**: One piece of equipment can have multiple maintenance records.

---

## 5. Java Classes and Their Responsibilities

All 6 Java source files are located in `src/`:

| Class File | Role & Responsibility |
| :--- | :--- |
| `Main.java` | **CLI Controller**: Displays the terminal menus, reads and validates user input, and calls the operation classes. |
| `DBConnection.java` | **Database Helper**: Reads database connection settings (`db.url`, `db.user`, `db.password`) from `db.properties` (or environment variables) and returns a JDBC `Connection`. |
| `Resource.java` | **Model Class**: Represents an equipment item with private fields (`resourceId`, `name`, `category`, `department`, `status`, `dailyLateFee`), constructor, getters/setters, `calculateLateFee(long overdueDays)`, and `displayDetails()`. |
| `Student.java` | **Model Class**: Represents a student with private fields (`studentId`, `name`, `department`, `email`, `phone`), constructor, getters/setters, and `displayDetails()`. |
| `ResourceOperations.java` | **Database Operations**: Executes SQL queries for adding, viewing, updating, deleting, and searching equipment, logging/viewing maintenance records, and displaying `COUNT` / `GROUP BY` statistics. |
| `CheckoutOperations.java` | **Database Operations**: Executes SQL queries for student management (`students`), issuing and returning equipment (`checkouts`), calculating overdue fines, and fetching borrowing history using `JOIN`. |

---

## 6. Important SQL Queries Used

1. **CRUD Operations (`INSERT`, `SELECT`, `UPDATE`, `DELETE`)**:
   ```sql
   INSERT INTO resources (resource_id, name, category, department, status, daily_late_fee)
   VALUES (?, ?, ?, ?, ?, ?);

   SELECT * FROM resources WHERE status = ? ORDER BY resource_id;

   UPDATE resources SET name = ?, category = ?, department = ?, status = ?, daily_late_fee = ?
   WHERE resource_id = ?;

   DELETE FROM resources WHERE resource_id = ?;
   ```

2. **Pattern Search (`WHERE` with `LIKE`)**:
   ```sql
   SELECT * FROM resources
   WHERE resource_id LIKE ? OR name LIKE ? OR category LIKE ? OR department LIKE ?
   ORDER BY resource_id;
   ```

3. **Multi-Table `JOIN` (Combining `checkouts`, `resources`, and `students`)**:
   ```sql
   SELECT c.checkout_id, c.resource_id, r.name AS resource_name,
          c.student_id, s.name AS student_name,
          c.checkout_date, c.due_date, c.return_date, c.late_fee, c.status
   FROM checkouts c
   JOIN resources r ON c.resource_id = r.resource_id
   JOIN students s ON c.student_id = s.student_id
   ORDER BY c.checkout_id DESC;
   ```

4. **Aggregate Statistics (`COUNT`, `SUM`, and `GROUP BY`)**:
   ```sql
   SELECT COUNT(*) AS total FROM resources;
   SELECT status, COUNT(*) AS count FROM resources GROUP BY status;
   SELECT category, COUNT(*) AS count FROM resources GROUP BY category;
   SELECT COUNT(*) AS total_checkouts, COALESCE(SUM(late_fee), 0) AS total_fines FROM checkouts;
   ```

---

## 7. How JDBC Connects Java to MySQL

1. **Configuration (`db.properties`)**: The database URL, username, and password are stored in `db.properties` so credentials are not hardcoded in the Java source code.
2. **Loading the Driver (`Class.forName`)**: `DBConnection` loads `com.mysql.cj.jdbc.Driver` from `lib/mysql-connector.jar`.
3. **Opening a Connection (`DriverManager.getConnection`)**: Each method requests a connection inside a `try-with-resources` block so connections close automatically.
4. **Executing Parameterized Queries (`PreparedStatement`)**: All user inputs are bound using `?` placeholders (`ps.setString(...)`, `ps.setDouble(...)`, `ps.setDate(...)`) to prevent SQL syntax issues and SQL injection.
5. **Transactions (`setAutoCommit(false)`, `commit()`, `rollback()`)**: When issuing or returning equipment, two tables (`checkouts` and `resources`) must be updated together. JDBC transactions ensure that either both updates succeed or both are rolled back on error.

---

## 8. How to Run the Project

### Step 1: Configure Database Credentials
Check `db.properties` in the project root and set your local MySQL username and password:
```properties
db.url=jdbc:mysql://localhost:3306/campus_resource_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=your_mysql_password
```

### Step 2: Initialize the Database Schema & Sample Data
Run the SQL script to create `campus_resource_db`, the four tables, and sample records:
```bash
mysql -u root -p < database/schema.sql
```

### Step 3: Compile and Run the Application
```bash
./run.sh
```
*(The `run.sh` script compiles `src/*.java` and launches the interactive CLI directly.)*

---

## 9. Example Demonstration Workflow (For Viva)

1. **View Existing Equipment & Statistics**:
   - Choose **`[8] View System Statistics`** to show the total equipment count and breakdown by status (`AVAILABLE`, `BORROWED`, `MAINTENANCE`) and category using `COUNT` and `GROUP BY`.
   - Choose **`[1] Equipment Management` -> `[1] View All Equipment`** to display the equipment catalog.
2. **Add & Search Equipment**:
   - Choose **`[1] Equipment Management` -> `[2] Add New Equipment`** and add `EQ-107` (`Raspberry Pi 5 Kit`, `Lab Equipment`, `CSE Lab`, fine `Rs. 15`).
   - Choose **`[5] Search Equipment`** and search for `Raspberry` to demonstrate the `LIKE` query.
3. **Register a Student**:
   - Choose **`[2] Student Management` -> `[2] Register New Student`** and register `STU-105` (`Vikram Singh`, `CSE`).
4. **Issue Equipment to Student**:
   - Choose **`[3] Issue Equipment to Student`**, enter Equipment ID `EQ-107`, Student ID `STU-105`, and duration `3` days.
   - Choose **`[5] Track Available & Borrowed Equipment`** to verify that `EQ-107` now appears under currently borrowed equipment.
5. **Return Equipment & Demonstrate Overdue Fine**:
   - Choose **`[4] Record Equipment Return & Calculate Overdue Fine`**.
   - Select the active checkout ID for `EQ-107` and choose option `[2] Enter Custom Return Date` with a date 4 days after the due date.
   - The system calculates `4 days * Rs. 15.00/day = Rs. 60.00`, records the return in `checkouts`, and updates `EQ-107` back to `AVAILABLE`.
6. **View Borrowing History & Maintenance**:
   - Choose **`[6] View Borrowing History`** to see the returned record with the assessed fine using the 3-table `JOIN` query.
   - Choose **`[7] Maintenance Records`** to add or view equipment repair logs.
