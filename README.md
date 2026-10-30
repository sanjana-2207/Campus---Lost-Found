# Campus Lost & Found Management System

A desktop application that gives a college campus one place to report, search, match
and recover lost belongings. Built with **Java + Swing + JDBC + MySQL** for a
DBMS/OOPS mini project review.

> The application is fully functional: real database, real authentication, real
> transactions, real Swing screens. Nothing in the UI is a mock-up.

---

## 1. What it does

| Feature | How it works |
|---|---|
| Report lost / found items | Type-specific form (lost: contact + reward + urgency, found: storage + handover) |
| Search and filter | Keyword, category, location, report type, status - all executed in SQL |
| Match suggestions | SQL view pairs lost and found reports and scores them out of 100 |
| Ownership claims | Claimant submits proof; staff verify; competing claims auto-close |
| Recovery tracking | OPEN → MATCHED → CLAIMED → RECOVERED, with an audit trail |
| Reports & analytics | Category, status, monthly trend, recovery rate, CSV export |
| Roles | STUDENT, STAFF, ADMIN - only staff/admin may verify claims |

---

## 2. Technology stack

| Layer | Technology | Why |
|---|---|---|
| Language | Java 8+ (tested with JDK 21) | Platform independent, strong OOPS support |
| UI | Java Swing / AWT | Included in the JDK, no extra framework needed |
| Database | MySQL 8 / MariaDB 10.4+ | Free, widely available in college labs (XAMPP) |
| Connectivity | JDBC + MySQL Connector/J | Direct SQL, ideal for demonstrating DBMS concepts |
| Architecture | 3-layer: UI → Service → DAO → MySQL | Clean separation, easy to explain in a viva |

No Maven/Gradle is required: `compile.bat` and `run.bat` build and start the app
with plain `javac`/`java`.

---

## 3. Folder structure

```
REC - Find & Lost/
├── Doc.txt                        Original project brief
├── README.md                      This file
├── compile.bat                    javac build script (Windows)
├── run.bat                        java launcher (Windows)
├── lib/                           mysql-connector-j-8.4.0.jar (included)
├── resources/
│   └── db.properties              Database URL / user / password
├── database/
│   └── schema.sql                 Tables, keys, views, triggers, seed data
├── docs/
│   ├── PPT_SLIDES.md              30-slide deck: content + speaker notes
│   ├── DIAGRAMS.md                ER, class, architecture, sequence diagrams
│   ├── VIVA_QA.md                 70 viva questions with answers
│   └── SETUP_AND_DEMO.md          Install, run, demo script, troubleshooting
└── src/com/campus/lostfound/
    ├── Main.java                  Entry point
    ├── config/DBConnection.java   Singleton JDBC connection factory
    ├── model/                     Item (abstract), LostItem, FoundItem, User,
    │                              Claim, Category, Location, enums, filters
    ├── dao/                       GenericDAO + JDBC implementations
    ├── service/                   Auth, Item, Claim, Match, Report services
    ├── ui/                        LoginFrame, MainFrame, 6 screens, dialogs
    └── util/                      PasswordUtil, SessionContext
```

---

## 4. Quick start

```text
1. Install JDK 17 or newer and MySQL 8 (or start XAMPP MySQL).
2. Import the schema:
       mysql -u root -p < database/schema.sql
      (or open database/schema.sql in MySQL Workbench and execute it)
3. The JDBC driver is already in lib/ (mysql-connector-j-8.4.0.jar)
4. Edit resources/db.properties and set db.password to your MySQL password.
5. Double-click compile.bat  →  then run.bat
```

Full instructions, including IntelliJ / Eclipse / VS Code setup, are in
[`docs/SETUP_AND_DEMO.md`](docs/SETUP_AND_DEMO.md).

### Demo accounts

| Role | E-mail | Password |
|---|---|---|
| Administrator | `admin@campus.edu` | `admin123` |
| Staff / Security | `security@campus.edu` | `staff123` |
| Student | `arjun@campus.edu` | `student123` |
| Student | `priya@campus.edu` | `student123` |
| Student | `karthik@campus.edu` | `student123` |

The seed data contains 14 reports, 5 claims and several ready-made matches, so the
matching screen is not empty during the presentation.

---

## 5. Where each syllabus concept lives

Reviewers usually ask "show me where you used X". Use this table.

### OOPS

| Concept | Evidence in the code |
|---|---|
| **Class & Object** | `model/*.java`, `service/*.java`; objects created in the UI (`new LostItem(...)`, `new Claim(...)`) |
| **Encapsulation** | `Item` fields are `private` with getters/setters; `DBConnection` keeps `url`, `userName`, `password` private |
| **Abstraction** | `abstract class Item` with abstract `getItemType()` / `getSpecificDetails()`; `GenericDAO<T>` and service interfaces hide JDBC |
| **Inheritance** | `LostItem extends Item`, `FoundItem extends Item`; every `Jdbc*DAO extends AbstractJdbcDAO<T> implements XxxDAO` |
| **Polymorphism** | `List<Item>` renders mixed lost/found rows; `item.getSpecificDetails()` resolves at runtime; services accept DAO interfaces (constructor injection) |
| **Association** | `Claim` holds an `Item` and two `User` references; `Item` holds `Category` and `Location` |
| **Composition** | `MainFrame` owns the service objects; `ItemDetailDialog` owns its claim form |
| **Enums** | `ItemType`, `ItemStatus`, `ClaimStatus`, `Role`, `Urgency` - type-safe constants stored as MySQL `ENUM` |
| **Generics** | `GenericDAO<T>` and `AbstractJdbcDAO<T>` serve every entity |
| **Singleton** | `DBConnection` (configuration) and `SessionContext` (current user) |

### DBMS

| Concept | Evidence |
|---|---|
| **Tables & data types** | 8 tables in `database/schema.sql` |
| **Primary key** | `user_id`, `item_id`, `claim_id`, `history_id`, `category_id`, `location_id` |
| **Foreign key** | `items.category_id`, `items.location_id`, `items.reporter_id`, `claims.*`, `lost_items.item_id`, `found_items.item_id` |
| **Constraints** | `UNIQUE` e-mail, `UNIQUE (item_id, claimant_id)`, `CHECK (reward >= 0)`, `CHECK (CHAR_LENGTH(phone) >= 10)`, `ENUM` domains, `NOT NULL` |
| **CRUD** | `INSERT` in `JdbcItemDAO.insert`, `SELECT` in `search`, `UPDATE` in `updateStatus`, `DELETE` in `delete` |
| **Joins** | `BASE_SELECT` joins `items` + `categories` + `locations` + `users` + subtype tables; `vw_item_details` repeats it as a view |
| **Views** | `vw_item_details` (reporting join), `vw_potential_matches` (matching engine) |
| **Triggers** | `trg_items_after_insert`, `trg_items_after_update` write to `item_status_history` |
| **Transactions** | `ClaimService.runInTransaction` - approve claim + reserve item + reject competitors, with commit/rollback |
| **Aggregation** | `COUNT(*) ... GROUP BY` in `countByStatus`, `countByCategory`, `countByLocation`, `countByMonth` |
| **Indexes** | `idx_items_type`, `idx_items_status`, `idx_items_search`, `idx_claims_status` |
| **Normalisation** | 3NF: categories and locations are lookup tables, never repeated text |
| **Inheritance mapping** | Class-table inheritance: `items` super-table + `lost_items` / `found_items` sub-tables |

### Security / good practice

| Practice | Where |
|---|---|
| Prepared statements (SQL-injection safe) | every DAO method |
| Password hashing (SHA-256) | `PasswordUtil`, `AuthService` |
| No plain-text password in DB | `users.password_hash` is `CHAR(64)` |
| Role-based authorisation | `SessionContext.isVerifier()`, `ClaimService.requireVerifier()` |
| Unchecked exception boundary | `DataAccessException`, `ValidationException` |
| Resources always closed | try-with-resources in every DAO |

---

## 6. Screens

| Screen | Purpose |
|---|---|
| Login / Register | Authentication and account creation |
| Dashboard | Live counters, recovery rate, ten most recent reports |
| Report an item | Dynamic lost/found form |
| Search and claim | Filtered table + item detail dialog + claim submission |
| Match suggestions | Scored lost↔found pairs with a plain-English explanation |
| Ownership claims | Staff verification: approve, reject, mark recovered |
| Reports | Category/status/monthly analytics + CSV export |

---

## 7. Documentation for the presentation

| File | Use it for |
|---|---|
| [`docs/PPT_SLIDES.md`](docs/PPT_SLIDES.md) | 30 slides with exact text, visual suggestions, speaker notes and timings |
| [`docs/DIAGRAMS.md`](docs/DIAGRAMS.md) | ER, class, architecture, DFD, use-case and sequence diagrams to redraw |
| [`docs/VIVA_QA.md`](docs/VIVA_QA.md) | 85 likely questions with model answers |
| [`docs/SETUP_AND_DEMO.md`](docs/SETUP_AND_DEMO.md) | Installation, demo script, test cases, troubleshooting |

---

## 8. Project facts for the report

- **Entities:** 8 tables, 9 relationships, 2 views, 2 triggers
- **Java files:** 49 (14 model, 10 DAO, 6 service, 13 UI, 1 main, 1 config, 2 exceptions, 2 utilities)
- **Lines of code:** roughly 5,000 Java + 450 SQL
- **Seed data:** 5 users, 9 categories, 8 locations, 14 reports, 5 claims
- **Tested on:** JDK 21, MySQL 8.0, Windows 10/11
