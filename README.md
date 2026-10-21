# Campus Shared Resource & Equipment Tracking System (CS-RETS)
> **Academic Project**: Object-Oriented Programming (OOP) & Database Management Systems (DBMS)  
> **Tech Stack**: Java (Core Java + JDBC) & MySQL 8.0  
> **Interface**: Interactive Command-Line Interface (CLI)

---

## 1. Project Concept & Scope

Unlike generic lost-and-found or general inventory systems, **CS-RETS** specifically tracks **high-value, sensitive campus assets** issued by academic departments, research labs, media studios, and sports departments:
- **Lab Equipment & Microcontroller Kits**: Digital oscilloscopes, ESP32-S3 AI IoT kits, STM32 development boards, and sensor arrays requiring periodic calibration.
- **Media & PR Equipment**: Full-frame DSLR cameras (Canon EOS R6 Mark II, Sony Alpha A7 IV), precision cinema lenses, and high-speed audio gear.
- **Sports Assets**: Tournament-grade cricket kits, official league basketball equipment, and athletic safety gear.

---

## 2. Object-Oriented Programming (OOP) Architecture

The project adheres to core OOP principles with a clean modular design:

```
campus.models.Resource (Abstract Class)
   ├── LabKit (Specialized Child Class)
   │     - componentsCount: int
   │     - microcontrollerType: String
   │     - requiresCalibration: boolean
   │     - calculateLateFee(): Base rate + calibration recalibration fee if overdue
   │
   ├── SportsAsset (Specialized Child Class)
   │     - sportName: String
   │     - wearCondition: String ('NEW', 'GOOD', 'FAIR', 'NEEDS_REPAIR')
   │     - tournamentGrade: boolean
   │     - calculateLateFee(): Peak tournament surge multiplier
   │
   └── MediaAsset (Specialized Child Class)
         - lensModel: String
         - shutterCount: int
         - maxResolution: String
         - calculateLateFee(): High-value optical depreciation surcharge
```

### OOP Principles Applied:
1. **Abstraction**: 
   - `abstract class Resource` defines the contract (`displayDetails()`, `calculateLateFee()`, `getSpecificAttributeSummary()`).
2. **Inheritance**: 
   - `LabKit`, `SportsAsset`, and `MediaAsset` inherit base attributes (`resourceId`, `name`, `department`, `dailyLateFee`, `status`) while specializing child attributes.
3. **Polymorphism**: 
   - Polymorphic method dispatch executes specific late-fee rules and detail formatters dynamically based on the instantiated runtime class.
4. **Encapsulation**: 
   - All model fields are private with validated getters and setters.
5. **Maintenance State Logs**: 
   - The `MaintenanceLog` class models service events, repair costs, technician assignments, and equipment state transitions (`AVAILABLE`, `MAINTENANCE`, `RETIRED`).

---

## 3. Database Management System (DBMS) Architecture

### Database Name: `campus_resource_db`

### Entity-Relationship (ER) Structure:
- `resources` (Parent Table: `resource_id` PK)
  - ├── `lab_kits` (`resource_id` FK -> `resources(resource_id)` **ON UPDATE CASCADE ON DELETE CASCADE**)
  - ├── `sports_assets` (`resource_id` FK -> `resources(resource_id)` **ON UPDATE CASCADE ON DELETE CASCADE**)
  - ├── `media_assets` (`resource_id` FK -> `resources(resource_id)` **ON UPDATE CASCADE ON DELETE CASCADE**)
  - ├── `maintenance_logs` (`resource_id` FK -> `resources(resource_id)` **ON UPDATE CASCADE ON DELETE CASCADE**)
  - └── `checkouts` (`resource_id` FK -> `resources(resource_id)` **ON UPDATE CASCADE ON DELETE RESTRICT**)
- `audit_trail` (Independent Audit Ledger: logs all changes via MySQL Triggers)

### Key DBMS Features Implemented:

1. **Cascading Updates (`ON UPDATE CASCADE`)**:
   - Child tables maintain foreign keys to `resources.resource_id` with `ON UPDATE CASCADE`.
   - Modifying an asset tag/ID in the parent `resources` table automatically propagates and updates foreign key references across `lab_kits`, `sports_assets`, `media_assets`, `checkouts`, and `maintenance_logs` without multiple manual queries.
   - Tested and verified in CLI Option `[7]`.

2. **Automated Audit Trails via MySQL Triggers**:
   - `trg_resource_update`: Fires on status changes or asset ID cascading updates.
   - `trg_checkout_insert`: Fires whenever an asset is issued to a student.
   - `trg_checkout_return`: Fires on asset return, recording the return timestamp and assessed late fees.
   - `trg_maintenance_insert`: Fires whenever a maintenance or service record is logged.
   - All trigger events write directly to the `audit_trail` table.

3. **Late-Fee Calculation Logic**:
   - Computed using SQL date arithmetic (`DATEDIFF(return_date, due_date)`) and OOP asset-specific late-fee algorithms.
   - Records late fees in the transaction record and logs the financial event to the audit trail.

4. **Database Transactions (ACID Compliance)**:
   - All multi-table operations (e.g. issuing checkout + updating asset status, logging maintenance + updating asset status) run within JDBC transactions using `conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()`.

---

## 4. Project Directory Structure

```
.
├── database/
│   └── schema.sql                # Complete MySQL DDL, FK cascades, triggers, & seed data
├── lib/
│   └── mysql-connector-j-8.3.0.jar # MySQL JDBC Driver
├── src/
│   └── campus/
│       ├── Main.java             # Menu-driven CLI entry point
│       ├── db/
│       │   └── DBConnection.java # JDBC Connection Manager
│       ├── models/
│       │   ├── Resource.java     # Abstract base class
│       │   ├── LabKit.java       # Child class: Lab equipment & microcontrollers
│       │   ├── SportsAsset.java  # Child class: Sports & tournament assets
│       │   ├── MediaAsset.java   # Child class: DSLR cameras & lens kits
│       │   ├── MaintenanceLog.java # Maintenance log model
│       │   ├── CheckoutTransaction.java # Checkout / return transaction model
│       │   └── AuditRecord.java  # Trigger audit trail model
│       └── dao/
│           ├── ResourceDAO.java  # Resource CRUD & cascading update
│           ├── CheckoutDAO.java  # Checkout, return & late fee calculations
│           ├── MaintenanceDAO.java # Service logs & state changes
│           └── AuditDAO.java     # Audit trail viewer
├── db.properties                 # DB configuration (host, user, password)
├── compile.sh                    # Shell script to compile all Java files
├── run.sh                        # Shell script to execute the CLI application
└── README.md                     # Project documentation & report
```

---

## 5. How to Run

### Step 1: Initialize Database (If not already initialized)
```bash
mysql -u root -pjojo123 < database/schema.sql
```

### Step 2: Compile the Java Application
```bash
./compile.sh
```

### Step 3: Launch the CLI Application
```bash
./run.sh
```

---

## 6. CLI Main Menu Overview

```
--------------------------------------------------------------------------
                           MAIN DASHBOARD                                 
--------------------------------------------------------------------------
 [1] View All High-Value Trackable Assets (Polymorphic Display)
 [2] Register New High-Value Asset (LabKit / SportsAsset / MediaAsset)
 [3] Checkout Asset to Student (Issue with Due Date)
 [4] Return Asset & Compute Overdue Late Fees (DBMS & OOP Logic)
 [5] Log Maintenance & State Transitions (Maintenance State Logs)
 [6] View Maintenance History
 [7] Test DBMS Cascading Update (ON UPDATE CASCADE Demonstration)
 [8] View DBMS Audit Trail (Automatic MySQL Triggers)
 [9] View Checkout Records (Active & Returned)
 [0] Exit Application
--------------------------------------------------------------------------
```
