# Setup, Run and Demo Guide

Everything needed to get the application running on a lab machine, plus the exact
demo script for the review.

---

## 1. Prerequisites

| Software | Version | Download |
|---|---|---|
| JDK | 8 or newer (17/21 recommended) | <https://adoptium.net> |
| MySQL Server | 8.0+ (or MariaDB 10.4+) | <https://dev.mysql.com/downloads/mysql/> |
| MySQL Connector/J | 8.x | <https://dev.mysql.com/downloads/connector/j/> |
| MySQL Workbench (optional) | any | <https://dev.mysql.com/downloads/workbench/> |

If you already use XAMPP, its MySQL/MariaDB is enough - start MySQL from the XAMPP
control panel and skip the MySQL Server installation.

---

## 2. Install the JDK (Windows)

1. Run the Adoptium installer (`.msi`).
2. Tick **"Set JAVA_HOME variable"** and **"Add to PATH"**.
3. Open a **new** Command Prompt and verify:

```bat
java -version
javac -version
```

Both must print a version. If `javac` is missing, you installed a JRE, not a JDK.

---

## 3. Create the database

### Option A — command line (fastest)

```bat
cd "D:\Web 2\REC - Find & Lost"
cmd /c "mysql -u root -p < database\schema.sql"
```

PowerShell does not support `<` redirection; the `cmd /c` wrapper is required. If
your root account has no password, use `mysql -u root < database\schema.sql`.

### Option B — MySQL Workbench

1. Open the connection to your local server.
2. **File → Open SQL Script** → choose `database/schema.sql`.
3. Click the ⚡ **Execute** button (the whole script, not a single statement).
4. Refresh the schema list - `campus_lost_found` appears with 8 tables, 2 views.

### Verify the import

Run these in Workbench or the CLI:

```sql
USE campus_lost_found;

SHOW TABLES;
-- expect: categories, claims, found_items, item_status_history,
--         items, locations, lost_items, users

SELECT COUNT(*) AS items FROM items;          -- 14
SELECT COUNT(*) AS claims FROM claims;        -- 5
SELECT * FROM vw_potential_matches;           -- several scored pairs
SELECT * FROM item_status_history;            -- rows written by the triggers
```

> The script starts with `DROP DATABASE IF EXISTS campus_lost_found`. Re-running it
> resets everything to the seeded state, which is exactly what you want before a
> demo.

---

## 4. The JDBC driver

`lib/` already contains **mysql-connector-j-8.4.0.jar**, so there is nothing to
download before compiling.

If you want a different version, download **Connector/J** ("Platform Independent"
`.zip`), extract `mysql-connector-j-8.x.x.jar`, drop it into `lib/` and delete the
older jar.

`compile.bat` and `run.bat` use the classpath entry `lib/*`, so the exact file name
does not matter — but never keep two Connector/J jars side by side.

---

## 5. Configure the connection

Edit `resources/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/campus_lost_found?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_MYSQL_PASSWORD
```

Common edits:

| Situation | Change |
|---|---|
| MySQL is not on 3306 | change the port in `db.url` |
| Root password is empty | `db.password=` (leave blank after the `=`) |
| Different database name | change it in `db.url` **and** in `schema.sql` |
| "Public Key Retrieval is not allowed" | keep `allowPublicKeyRetrieval=true` in the URL |

---

## 6. Build and run

Double-click **`compile.bat`**, then **`run.bat`**.

`run.bat` checks port 3306 first and starts the local MySQL server itself (by
calling `start-database.bat`) when the server is not running, so there is no
separate "start the database" step. If MySQL cannot be started, `run.bat` stops
with a clear message instead of letting the app fail with a connection dialog.

Or manually:

```bat
mkdir out
dir /b /s src\*.java > sources.txt
javac -encoding UTF-8 -d out -cp "lib/*" @sources.txt
xcopy resources out /E /I /Y
del sources.txt
java -cp "out;lib/*" com.campus.lostfound.Main
```

Expected result: the navy login window with the demo credentials printed on screen.

### Run from an IDE

**IntelliJ IDEA**
1. *File → Open* → select the project folder.
2. Right-click `src` → *Mark Directory as → Sources Root*.
3. Right-click `resources` → *Mark Directory as → Resources Root*.
4. *File → Project Structure → Libraries → + → Java* → select the jar in `lib/`.
5. Open `Main.java` → run. Set the project SDK to JDK 17+.

**Eclipse**
1. *File → New → Java Project*, uncheck "Use default location", select the folder.
2. *Properties → Java Build Path → Libraries → Add External JARs* → `lib/*.jar`.
3. *Run → Run Configurations → Java Application* → main class
   `com.campus.lostfound.Main`.

**VS Code**
1. Install the *Extension Pack for Java*.
2. Open the folder; add the jar under *Java Projects → Referenced Libraries*.
3. Press **F5**; when asked for the main class, choose `Main`.

---

## 7. Demo accounts and data

| Role | E-mail | Password |
|---|---|---|
| Admin | `admin@campus.edu` | `admin123` |
| Staff (Security) | `security@campus.edu` | `staff123` |
| Student | `arjun@campus.edu` | `student123` |
| Student | `priya@campus.edu` | `student123` |
| Student | `karthik@campus.edu` | `student123` |

Seeded state: 14 reports (7 lost, 7 found), 9 categories, 8 locations, 5 claims
(1 approved, 1 recovered, 1 rejected, 2 pending).

---

## 8. Live demo script (6–7 minutes)

Rehearse this once end-to-end. It is ordered so each step sets up the next.

### Step 1 — Login as a student (40 s)

1. Launch the app.
2. Sign in with `arjun@campus.edu / student123`.
3. Point at the dashboard: **7 lost, 7 found, 2 pending claims, 1 recovered**.
4. Say: *"Counters come from SQL GROUP BY queries, refreshed every time a screen opens."*

### Step 2 — Report a lost item (60 s)

1. **Report an item** → keep "I lost an item".
2. Title: `Black Dell laptop charger`
   Description: `65W charger with a white label on the cable, lost after the DBMS lab.`
3. Category: Electronics · Location: CS Block · Date: today.
4. Contact is pre-filled from the account. Reward: `100`. Urgency: Medium.
5. Submit → note the report number in the confirmation.
6. Say: *"This wrote two rows - `items` and `lost_items` - in a single transaction."*

### Step 3 — Find the match (50 s)

1. **Match suggestions**.
2. Show the new pair at the top: *Dell charger (lost)* ↔ *Dell 65W charger with black
   cable (found)*, score 100, confidence Very High.
3. Click the row → read the explanation strip aloud.
4. Say: *"This list is a view. The scoring is pure SQL, not Java loops."*

### Step 4 — Submit a claim (60 s)

1. Double-click the match → the found item opens.
2. Write proof: `Black 65W Dell charger, white label on the cable, lost in CS Block Lab 3 on Monday.`
3. Submit claim → claim is PENDING.
4. Say: *"The claimant cannot be the reporter, the item must still be open, and the
   database has a unique key preventing a second claim."*

### Step 5 — Switch to staff and verify (70 s)

1. **Sign out** → sign in as `security@campus.edu / staff123`.
2. **Ownership claims** → the new claim is at the top of *All claims*.
3. Select it → **Approve** → remarks `Matched with the owner's description.`
4. Point out: claim becomes APPROVED, item becomes CLAIMED, competing claims close.
5. Say: *"Three statements, one transaction. Any failure and everything rolls back."*

### Step 6 — Mark recovered (40 s)

1. With the approved claim still selected → **Mark recovered** → confirm.
2. Open **Dashboard**: recovered count increased, recovery rate changed.
3. Say: *"The audit row was written by the database trigger, not by Java."*

### Step 7 — Reports and finish (50 s)

1. **Reports** → category table, status table, monthly trend.
2. **Export summary** → save to the Desktop → open the CSV in Excel.
3. Optional: run `SELECT * FROM item_status_history ORDER BY history_id DESC;` in
   Workbench to show the trigger output.

---

## 9. Test cases (use this table in the report)

| # | Test case | Steps | Expected | Actual |
|---|---|---|---|---|
| 1 | Valid login | student credentials | Dashboard opens | Pass |
| 2 | Invalid password | wrong password | "Invalid e-mail address or password" | Pass |
| 3 | Blank login fields | press Sign in | Validation message | Pass |
| 4 | Duplicate registration | register existing e-mail | "already registered" | Pass |
| 5 | Short title | 2-character title | Validation error | Pass |
| 6 | Future date | spinner max = today | Cannot select a future date | Pass |
| 7 | Empty description | submit | Validation error | Pass |
| 8 | Negative reward | `-50` | Validation error | Pass |
| 9 | Filter by category + location | Electronics + CS Block | Only those rows | Pass |
| 10 | Keyword search | `charger` | Title/description matches | Pass |
| 11 | Claim own item | open own report | Claim form hidden | Pass |
| 12 | Duplicate claim | submit twice | Blocked by service + unique key | Pass |
| 13 | Approve claim | staff action | Item CLAIMED, others REJECTED | Pass |
| 14 | Reject claim | staff action | Claim REJECTED, item stays OPEN | Pass |
| 15 | Recover | approved claim | Claim + item RECOVERED | Pass |
| 16 | Student calls approve | role check | ValidationException | Pass |
| 17 | CSV export | Reports → export | Valid file, opens in Excel | Pass |
| 18 | MySQL stopped | start app | Friendly message, no stack trace | Pass |

---

## 10. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `javac is not recognized` | JDK not installed / PATH | reinstall JDK, tick "Add to PATH", open a new terminal |
| `No suitable driver found` | jar missing from `lib/` | copy `mysql-connector-j-*.jar` into `lib/` |
| `Unknown database 'campus_lost_found'` | schema not imported | run `database/schema.sql` |
| `Access denied for user 'root'` | wrong password | update `resources/db.properties` |
| `Communications link failure` | MySQL is stopped | `run.bat` starts it automatically; if that fails run `start-database.bat` |
| `Public Key Retrieval is not allowed` | old JDBC URL | keep `allowPublicKeyRetrieval=true` |
| `The server time zone value ... is unrecognized` | missing timezone | keep `serverTimezone=UTC` |
| Login window never appears | startup exception | run `run.bat` from a terminal to see the message |
| Matches screen is empty | no candidate pairs in seed data | re-import the schema |
| CSV opens with strange characters | Excel locale | the file is UTF-8 with BOM; open via *Data → From Text* if needed |
| Colours look wrong | non-Windows LAF | expected; layout still works, fonts fall back |

### Reset everything

```bat
cmd /c "mysql -u root -p < database\schema.sql"
```

or delete `out/` and run `compile.bat` again after changing `db.properties`.

---

## 11. Screenshot checklist for the PPT

Take these at 1280×800, with realistic data:

- [ ] Login screen (full window)
- [ ] Dashboard with counters visible
- [ ] Report form with the lost fields filled in
- [ ] Report form switched to "I found an item" (shows the CardLayout)
- [ ] Search results with filters applied
- [ ] Item detail dialog with the claim box
- [ ] Match suggestions with the explanation strip
- [ ] Claims screen with an approved row
- [ ] Reports screen with all four tables
- [ ] Workbench showing `vw_potential_matches`
- [ ] Workbench showing `item_status_history` after a recovery

Save them in `docs/screenshots/` and keep the same window size for a tidy montage.

---

## 12. Submission checklist

- [ ] `database/schema.sql` imports cleanly on a fresh MySQL
- [ ] `lib/` contains the connector jar (it ships with the project)
- [ ] `resources/db.properties` contains **your** password - remove it before sharing
- [ ] `compile.bat` and `run.bat` work on a clean machine with the JDK on PATH
- [ ] README and the four documents in `docs/` are included
- [ ] PPT exported to PDF as a backup
- [ ] Demo data reset (`schema.sql` re-imported) before the review
- [ ] Screenshots copied into the report
