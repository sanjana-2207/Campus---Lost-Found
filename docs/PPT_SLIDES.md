# PPT Deck — Campus Lost & Found Management System

**30 slides · 16–18 minutes presentation + 5–7 minutes demo + Q&A**

This file gives you, for every slide: the exact text to put on it, the visual to
build, and what to say. Copy the bullets straight into PowerPoint; read the notes
until they feel natural.

---

## Deck setup (do this once)

| Setting | Value |
|---|---|
| Aspect ratio | 16:9 (Design → Slide Size → Widescreen) |
| Theme colours | Navy `#132A3E`, Steel blue `#2C7DA0`, Background `#F2F5F8`, Card white `#FFFFFF`, Text `#1F2A37`, Muted `#6B7A8C`, Success `#2E7D4F`, Warning `#C77D00`, Danger `#C0392B` |
| Fonts | Headings: **Segoe UI Semibold 32 pt** · Body: **Segoe UI 18–20 pt** · Code: **Consolas 14–16 pt** |
| Slide footer | "Campus Lost & Found Management System · OOPS + DBMS Mini Project" + slide number |
| Rule | Never more than 6 bullets per slide; never below 16 pt text |

The palette is identical to the application, so the deck and the live demo look
like one product.

---

## Slide 1 — Title

**Layout:** Full-bleed navy background with a large translucent circle bottom-right
(the same decoration as the app's login screen), title left-aligned.

**On-slide text:**

> # Campus Lost & Found
> ### Management System
>
> A centralised platform to report, match and recover lost belongings on campus
>
> **Presented by:** ‹Your Name› · ‹Roll Number›
> **Guide:** ‹Guide Name› · ‹Department›
> **Academic year:** 2025–26

**Speaker notes:** "Good morning. Our project is a Campus Lost and Found Management
System. It replaces notice boards and WhatsApp forwards with one searchable
database, and it is built to demonstrate OOPS and DBMS concepts in a real
application."

**Time:** 30 s

---

## Slide 2 — Agenda

**Layout:** Two columns of five items with small numbered circles.

**On-slide text:**

1. Problem and motivation
2. Objectives and scope
3. Existing systems and gaps
4. Technology stack
5. System architecture
6. OOPS design
7. Database design
8. Transactions and matching
9. Module walkthrough
10. Testing, limitations, conclusion

**Speaker notes:** "We will move from why the project exists, to how it is
designed, to a live demonstration, and finish with testing and future scope."

**Time:** 20 s

---

## Slide 3 — The problem

**Layout:** Three "pain" cards with an icon each (notice board, chat bubble, paper
register), plus a red highlight strip at the bottom.

**On-slide text:**

- Lost items are reported on **notice boards, WhatsApp groups and paper registers**
- Each channel reaches a **different audience** — nothing is searchable
- **No proof of ownership**: anyone can walk away with an item
- **No audit trail**: the college cannot say how many items were recovered
- Typical campus reality: **dozens of items lost every month**, a handful returned

**Speaker notes:** "Ask the room: how many of you have lost a bottle, ID card or
charger this semester? The information exists, but it is scattered. The security
office keeps a register, students post in groups, and the two never meet. That gap
is our problem statement."

**Time:** 60 s

---

## Slide 4 — Proposed solution

**Layout:** Left: four feature bullets. Right: a simple 4-step horizontal flow
(Report → Match → Claim → Recover).

**On-slide text:**

**One platform. Four steps.**

- **Report** a lost or found item with description, category, location and date
- **Match** automatically with likely counterparts using a weighted score
- **Claim** ownership by submitting proof; staff verify it
- **Recover** and track the item until it is handed over

**Speaker notes:** "The whole system is one loop. Every report improves the
matching pool, every claim has a verifier, and every recovery is recorded, so the
college finally gets numbers."

**Time:** 40 s

---

## Slide 5 — Objectives

**Layout:** Numbered list, four items, generous whitespace.

**On-slide text:**

1. Provide a **single, searchable repository** of lost and found items
2. **Reduce recovery time** from weeks to hours through automatic matching
3. Enforce **verified ownership** before an item is released
4. Generate **management reports** (lost, found, claimed, recovered)
5. Demonstrate practical use of **OOPS pillars** and **DBMS features** in one system

**Speaker notes:** "Points one to four are the user-facing objectives. Point five
is academic: this is not a CRUD toy, every concept in the syllabus is used because
the problem genuinely needs it."

**Time:** 40 s

---

## Slide 6 — Scope and stakeholders

**Layout:** Two columns: "In scope" (green ticks) / "Out of scope" (grey crosses),
with an actor row underneath.

**On-slide text:**

**In scope**
- Single campus, single MySQL database
- Desktop client (Java Swing) for students, staff and admin
- Lost and found reports, matching, claims, recovery, reports

**Out of scope (future)**
- Mobile app, e-mail/SMS notifications
- Photo uploads, QR-code tags
- Cloud deployment and multi-campus tenancy

**Actors:** Student · Security staff · Administrator

**Speaker notes:** "We deliberately kept the boundary tight so the project could be
finished and tested properly. Each out-of-scope item is a natural next step, which
I will come back to at the end."

**Time:** 40 s

---

## Slide 7 — Existing systems and gaps

**Layout:** Comparison table, four rows.

**On-slide text:**

| Channel | Searchable | Ownership proof | Audit trail |
|---|---|---|---|
| Notice board | No | No | No |
| WhatsApp group | Partly (scrolling) | No | No |
| Paper register | No | Informal | Manual |
| **This system** | **Yes (SQL)** | **Yes (claims)** | **Yes (triggers)** |

**Speaker notes:** "The paper register is the closest existing system, and it fails
on three counts: you cannot search it, anyone can claim an item, and nobody can
produce a report at the end of the semester."

**Time:** 40 s

---

## Slide 8 — Technology stack

**Layout:** Four stacked layers (Presentation, Business, Persistence, Database)
with the technology on the right.

**On-slide text:**

| Layer | Technology | Role |
|---|---|---|
| Presentation | **Java Swing / AWT** | Windows, forms, tables, dialogs |
| Business | **Plain Java classes** | Validation, rules, transactions |
| Persistence | **JDBC + DAO pattern** | SQL execution, row mapping |
| Database | **MySQL 8** | Tables, keys, views, triggers, transactions |

**Why this stack?** Zero external frameworks, available in every college lab,
and it exposes SQL and OOPS instead of hiding them behind an ORM.

**Speaker notes:** "We chose no framework on purpose. Spring or Hibernate would
hide exactly the concepts the review wants to see: joins, transactions, prepared
statements and object-relational mapping."

**Time:** 50 s

---

## Slide 9 — System architecture

**Layout:** Insert the 3-layer diagram from `docs/DIAGRAMS.md` (section 4).

**On-slide text:**

```
┌──────────────────────────────────────────────┐
│  UI  (Swing)                                 │
│  LoginFrame · MainFrame · 6 panels · dialogs  │
└───────────────────┬──────────────────────────┘
                    │ calls methods, never SQL
┌───────────────────▼──────────────────────────┐
│  SERVICE  (business rules)                   │
│  Auth · Item · Claim · Match · Report         │
└───────────────────┬──────────────────────────┘
                    │ DAO interfaces
┌───────────────────▼──────────────────────────┐
│  DAO  (JDBC, PreparedStatement, mapping)      │
└───────────────────┬──────────────────────────┘
                    │ SQL
┌───────────────────▼──────────────────────────┐
│  MySQL 8  (tables · views · triggers)         │
└──────────────────────────────────────────────┘
```

**Speaker notes:** "Each layer only knows the one below it. The UI has no SQL at
all; the DAO has no business rules; the service has no Swing imports. That is what
makes the claim transaction reusable and the code testable."

**Time:** 60 s

---

## Slide 10 — OOPS at a glance

**Layout:** Four quadrants, one pillar per quadrant, each with a one-line definition
and the project file where it appears.

**On-slide text:**

| Pillar | In one line | Where |
|---|---|---|
| **Encapsulation** | Data is private; behaviour is public | `Item`, `User`, `DBConnection` |
| **Abstraction** | Expose *what*, hide *how* | `abstract Item`, `GenericDAO<T>` |
| **Inheritance** | Reuse common state and behaviour | `LostItem/FoundItem extends Item` |
| **Polymorphism** | One reference, many forms | `List<Item>`, overridden methods |

**Speaker notes:** "These are not four separate examples bolted on; they are the
same hierarchy seen from four angles."

**Time:** 40 s

---

## Slide 11 — Class design: the Item hierarchy

**Layout:** Class diagram from `docs/DIAGRAMS.md` (section 2), left half of slide.
Right half: the three mapping rules in text.

**On-slide text:**

```
              ┌───────────────────────────┐
              │   <<abstract>> Item       │
              │  - itemId, title, desc    │
              │  - category, location     │
              │  - eventDate, status      │
              │  + getItemType()  abstract│
              │  + getSpecificDetails()   │
              └─────────┬─────────────────┘
            ┌───────────┴────────────┐
   ┌────────▼────────┐     ┌─────────▼────────┐
   │    LostItem     │     │    FoundItem     │
   │ contact         │     │ storageLocation  │
   │ rewardOffered   │     │ handedToSecurity │
   │ urgency         │     │ finderNotes      │
   └─────────────────┘     └──────────────────┘
```

- Common state lives **once** in `Item`
- Sub-classes add only what is specific
- The database mirrors this with **class-table inheritance**

**Speaker notes:** "This is the core of the OOPS design. If you asked for a single
example of inheritance, abstraction and polymorphism, this hierarchy is it."

**Time:** 60 s

---

## Slide 12 — Encapsulation and abstraction in code

**Layout:** Two code blocks side by side, dark background, syntax highlighting.

**On-slide text:**

**Encapsulation — `Item.java`**
```java
private String title;          // no direct access
private ItemStatus status = ItemStatus.OPEN;

public void setStatus(ItemStatus status) {
    this.status = status == null ? ItemStatus.OPEN : status;
}
```

**Abstraction — `Item.java` + `GenericDAO.java`**
```java
public abstract class Item {
    public abstract ItemType getItemType();
    public abstract String getSpecificDetails();
}

public interface GenericDAO<T> {
    T findById(int id);
    int insert(T entity);
}
```

**Speaker notes:** "Encapsulation is not just getters and setters: the setter
enforces the invariant that status is never null. Abstraction means the UI asks
'are you lost or found?' without knowing which class it holds, and the service asks
the DAO for data without knowing that JDBC exists."

**Time:** 60 s

---

## Slide 13 — Inheritance and polymorphism in code

**Layout:** Code on the left, a runtime table on the right showing the same call
producing different rows.

**On-slide text:**

```java
public class LostItem extends Item {
    @Override public ItemType getItemType() { return ItemType.LOST; }
    @Override public String getSpecificDetails() {
        return "Contact: " + contact + "\nReward: Rs " + rewardOffered;
    }
}

// One reference type, two behaviours:
List<Item> items = itemDAO.findAll();   // lost + found mixed
for (Item item : items) {
    row.add(item.getItemType().getLabel());      // "Lost" / "Found"
    row.add(item.getSpecificDetails());          // resolved at runtime
}
```

| # | Runtime object | `getSpecificDetails()` output |
|---|---|---|
| 1 | `LostItem` | Contact / Reward / Urgency |
| 2 | `FoundItem` | Storage / With security / Notes |

**Speaker notes:** "The loop has no if-else on type. Java dispatches to the right
override, which is exactly what polymorphism buys us: new item types can be added
without editing this loop."

**Time:** 60 s

---

## Slide 14 — Database design: ER diagram

**Layout:** Full-width ER diagram from `docs/DIAGRAMS.md` (section 1). Keep names
large enough to read from the last row.

**On-slide text:**

- 8 tables, 1 lookup pair, 1 super-table + 2 subtype tables
- `users` 1 : N `items`, `items` 1 : N `claims`
- `items` 1 : 1 `lost_items`, `items` 1 : 1 `found_items`
- `items` 1 : N `item_status_history`

**Speaker notes:** "The interesting part is the middle: `items` is the super-table,
and the two subtype tables hold only the fields specific to lost or found reports.
That is the database mirror of the Java hierarchy."

**Time:** 70 s

---

## Slide 15 — Schema, keys and constraints

**Layout:** Table listing every table with its primary key and main foreign keys;
a second small table for constraint types.

**On-slide text:**

| Table | Primary key | Foreign keys |
|---|---|---|
| `users` | `user_id` | — |
| `categories` | `category_id` | — |
| `locations` | `location_id` | — |
| `items` | `item_id` | category, location, reporter |
| `lost_items` | `item_id` | → `items` (1:1, cascade) |
| `found_items` | `item_id` | → `items` (1:1, cascade) |
| `claims` | `claim_id` | item, claimant, verifier |
| `item_status_history` | `history_id` | → `items` (cascade) |

**Constraints used:** `NOT NULL` · `UNIQUE` · `CHECK` · `ENUM` · `DEFAULT` ·
`ON DELETE CASCADE` · 6 indexes

**Speaker notes:** "Two constraints deserve a mention. The unique key on
item + claimant blocks duplicate claims at database level, not just in Java. And
the foreign keys make it impossible to attach a claim to a non-existent item."

**Time:** 60 s

---

## Slide 16 — SQL features in the project

**Layout:** Four small code snippets in a 2×2 grid: JOIN, VIEW, TRIGGER, GROUP BY.

**On-slide text:**

**JOIN** — every list screen
```sql
FROM items i
JOIN categories c ON c.category_id = i.category_id
JOIN locations  l ON l.location_id = i.location_id
JOIN users      u ON u.user_id     = i.reporter_id
LEFT JOIN lost_items li ON li.item_id = i.item_id
```

**VIEW** — `vw_potential_matches`
```sql
CREATE OR REPLACE VIEW vw_potential_matches AS ...
```

**TRIGGER** — audit without Java code
```sql
AFTER UPDATE ON items ... INSERT INTO item_status_history ...
```

**GROUP BY** — dashboard counters
```sql
SELECT status, COUNT(*) FROM items GROUP BY status;
```

**Speaker notes:** "The LEFT JOIN is important: a lost row has no found_items row,
so an inner join would silently drop half the data. The trigger means the audit
trail cannot be forgotten by application code."

**Time:** 70 s

---

## Slide 17 — Transactions: the claim workflow

**Layout:** Left: four numbered SQL statements inside one transaction bracket.
Right: before/after state tables.

**On-slide text:**

```
BEGIN
  1. UPDATE claims SET status='APPROVED'      WHERE claim_id = 3
  2. UPDATE items  SET status='CLAIMED'       WHERE item_id  = 14
  3. UPDATE claims SET status='REJECTED'
        WHERE item_id = 14 AND claim_id <> 3 AND status = 'PENDING'
COMMIT            -- or ROLLBACK on any failure
```

| | Before | After |
|---|---|---|
| Claim 3 | PENDING | **APPROVED** |
| Claim 2 (same item) | PENDING | **REJECTED** |
| Item 14 | OPEN | **CLAIMED** |

**Why a transaction?** Two staff members may approve two claims on the same item at
the same moment. Without atomicity the item could be reserved twice.

**Speaker notes:** "This is the ACID slide. In `ClaimService.runInTransaction` we
disable auto-commit, run the three statements, commit once. Any exception rolls all
three back, so the database can never show an approved claim against an open item."

**Time:** 80 s

---

## Slide 18 — Matching engine

**Layout:** Formula blocks at the top, a worked example table below.

**On-slide text:**

**Candidate filter (SQL view):** same category + same location + reported within
30 days + both still open.

**Score out of 100:**

| Signal | Points |
|---|---|
| Same category | 40 |
| Same location | 30 |
| Reports ≤ 2 days apart | 20 |
| Reports ≤ 7 days apart | 12 |
| Otherwise | 5 |
| First word of title appears in the other title | 10 |

**Worked example:** *"Black Dell laptop charger"* (lost) vs *"Dell 65W charger with
black cable"* (found), same category, same lab, 1 day apart → 40 + 30 + 20 + 10 =
**100 / Very High**.

**Speaker notes:** "The pairing happens in one SQL view, not in Java loops. The
database is better at joining thousands of rows, and we can tune the weights by
editing one view. The UI explains the score in words so a staff member can judge
it."

**Time:** 70 s

---

## Slide 19 — Module 1: login and roles

**Layout:** Screenshot of the login screen (left) + role matrix (right).

**On-slide text:**

- Passwords stored as **SHA-256 digests**, never plain text
- Same error for unknown e-mail and wrong password (**no account enumeration**)
- Session held in `SessionContext`, cleared on sign-out

| Action | Student | Staff / Admin |
|---|---|---|
| Report, search, claim | Yes | Yes |
| Verify, approve, reject, recover | No | **Yes** |

**Speaker notes:** "Authorisation is enforced twice: the UI hides the verification
buttons, and `ClaimService.requireVerifier()` throws if a student calls it
directly."

**Time:** 50 s

---

## Slide 20 — Module 2: reporting an item

**Layout:** Screenshot of the report form.

**On-slide text:**

- Type switch changes only the **type-specific** fields (CardLayout)
- **Lost** → contact, reward, urgency
- **Found** → storage location, handed to security, finder notes
- Validation in `ItemService`: title length, description length, no future date,
  required lookups, non-negative reward
- One report = **two INSERTs in one transaction** (`items` + subtype row)

**Speaker notes:** "This is where the OOPS hierarchy pays off in the UI: the form
literally swaps panels, and the service receives either a LostItem or a FoundItem
through the same method."

**Time:** 60 s

---

## Slide 21 — Module 3: search and filter

**Layout:** Screenshot of the search screen with filters filled in.

**On-slide text:**

- Filters: **keyword · category · location · report type · status**
- Any combination, all executed as **one parameterised SQL query**
- `ItemFilter` object carries the criteria from UI to DAO
- `PreparedStatement` binding → **SQL-injection safe**
- Double-click a row → full details → claim

**Speaker notes:** "The DAO builds the WHERE clause dynamically but every value is
bound as a parameter. Nothing from the user is ever concatenated into SQL - that is
the difference between a demo and a safe application."

**Time:** 60 s

---

## Slide 22 — Module 4: match suggestions

**Layout:** Screenshot of the matches screen with the explanation strip visible.

**On-slide text:**

- Rows are **lost ↔ found pairs**, highest score first
- Confidence labels: Very High (≥90) · High (≥80) · Medium (≥70)
- Selecting a row explains the reason in plain English
- Double-click opens the found item to submit a claim
- **Read-only report**: computed live, no stale data stored

**Speaker notes:** "Because the view is computed on demand, a new report appears in
the match list immediately. There is no nightly job and no cache to invalidate."

**Time:** 50 s

---

## Slide 23 — Module 5: claims and recovery

**Layout:** Screenshot of the claims screen + a state machine strip at the bottom.

**On-slide text:**

```
PENDING ──approve──► APPROVED ──handover──► RECOVERED
   │
   └──reject──► REJECTED        (item stays OPEN)
```

- Proof of ownership is **mandatory and stored**
- Approving one claim **auto-rejects** the competing claims
- Every status change is logged by the **trigger** into `item_status_history`
- Staff see all claims; a student sees only their own (privacy)

**Speaker notes:** "Notice the workflow is enforced on both sides: the item moves to
CLAIMED on approval, so a second claimant cannot file, and the unique key blocks a
duplicate claim anyway."

**Time:** 60 s

---

## Slide 24 — Module 6: reports and analytics

**Layout:** Screenshot of the reports screen.

**On-slide text:**

- Counters: total reports, **recovery rate**, pending claims, total claims
- Breakdowns: category, item status, claim status, monthly lost vs found
- All figures from SQL `GROUP BY` - **no full-table loading into Java**
- **CSV export** for the weekly security review (UTF-8 with BOM)

**Speaker notes:** "The recovery rate is the number the college actually cares
about: recovered items divided by total reports. It is available because every
handover is recorded, which the paper register cannot do."

**Time:** 50 s

---

## Slide 25 — User interface tour

**Layout:** 2×3 montage of screenshots: dashboard, report form, search, matches,
claims, reports.

**On-slide text:**

- Consistent theme defined once in `UIStyle`
- Navy sidebar, card layout, colour-coded statuses
- Every table is sortable; every screen refreshes on navigation
- Errors shown inline where possible, dialogs only for database failures

**Speaker notes:** "All six screens share one style class, so the application looks
like a product rather than six separate assignments."

**Time:** 30 s

---

## Slide 26 — Testing

**Layout:** Test-case table, 8–10 rows, colour-coded Pass column.

**On-slide text:**

| # | Test case | Expected | Result |
|---|---|---|---|
| 1 | Login with valid credentials | Dashboard opens | Pass |
| 2 | Login with wrong password | "Invalid e-mail or password" | Pass |
| 3 | Register duplicate e-mail | Rejected with message | Pass |
| 4 | Report with empty title | Inline validation error | Pass |
| 5 | Report with future date | Blocked by spinner + service | Pass |
| 6 | Search by category + location | Only matching rows | Pass |
| 7 | Claim own reported item | Blocked | Pass |
| 8 | Duplicate claim | Blocked by unique key | Pass |
| 9 | Approve claim | Item CLAIMED, others REJECTED | Pass |
| 10 | Student calls approve directly | ValidationException | Pass |
| 11 | Export CSV | File opens in Excel | Pass |
| 12 | MySQL stopped | Friendly connection message | Pass |

**Speaker notes:** "Testing was manual plus SQL-level checks. The two most
important cases are nine and ten: the transaction is atomic, and authorisation does
not depend on the UI."

**Time:** 60 s

---

## Slide 27 — Results

**Layout:** Four big numbers + one sentence.

**On-slide text:**

- **14** reports seeded, **5** claims, **8** relationships verified
- **6** suggested matches generated from real seed data
- Claim approval executes in **one transaction** - zero inconsistent states in testing
- Recovery rate visible live on the dashboard

**Value delivered:** a searchable, verifiable, auditable replacement for the
notice board and the paper register.

**Speaker notes:** "In the demo you will see the numbers change: approve a claim,
then the dashboard recovery rate and the item status update together."

**Time:** 40 s

---

## Slide 28 — Limitations and future scope

**Layout:** Two columns: limitations (honest) / next steps (ambitious).

**On-slide text:**

**Current limitations**
- Desktop-only, single campus
- No photo attachment (text descriptions only)
- SHA-256 without salt (adequate for a project, not production)
- Matching is rule-based, not machine-learned

**Next steps**
- Photo upload + image similarity
- E-mail / SMS notification on match
- Web or mobile client on the same database
- QR-code tags on lab equipment
- Salted BCrypt hashing and audit login table

**Speaker notes:** "We are explicit about the limits. Each one is a well-scoped
extension rather than a rewrite, because the layers are separated."

**Time:** 50 s

---

## Slide 29 — Conclusion

**Layout:** Large statement + three supporting lines.

**On-slide text:**

> **One database, one workflow, zero lost-and-found chaos.**

- The **problem** was fragmented, unsearchable, unverifiable reporting
- The **solution** is a layered Java + MySQL application with verified recovery
- The **learning** is OOPS and DBMS applied where they genuinely fit:
  inheritance for item types, transactions for claims, views for matching

**Speaker notes:** "To close: the project solves a real campus problem and, more
importantly for this review, every concept we studied has a concrete job inside it."

**Time:** 30 s

---

## Slide 30 — Thank you / Q&A

**Layout:** Navy background, centred text.

**On-slide text:**

> # Thank you
>
> **Questions?**
>
> Demo accounts: `security@campus.edu / staff123` · `arjun@campus.edu / student123`

**Speaker notes:** "We are happy to take questions, and we can demonstrate any
screen live - including the database transaction behind the approve button."

**Time:** Q&A

---

## Delivery plan (18 minutes)

| Minutes | Slides | Focus |
|---|---|---|
| 0:00–1:00 | 1–2 | Title, agenda |
| 1:00–4:00 | 3–7 | Problem, solution, objectives, scope, gap analysis |
| 4:00–6:00 | 8–9 | Stack and architecture |
| 6:00–10:00 | 10–13 | OOPS design and code |
| 10:00–14:00 | 14–18 | Database, SQL, transactions, matching |
| 14:00–17:00 | 19–24 | Module walkthrough |
| 17:00–18:00 | 25–30 | UI, testing, results, limits, conclusion |

**Demo (5–7 min) after slide 24 or after the deck:** see the script in
`docs/SETUP_AND_DEMO.md` (section 8).

---

## Three things that impress reviewers

1. **Show the transaction failing.** Stop MySQL mid-approval, or temporarily change
   a foreign key, and show that no half-updated row remains. Very few projects can
   demonstrate rollback.
2. **Show the view behind the match.** Run
   `SELECT * FROM vw_potential_matches;` in MySQL and then the same list in the UI.
3. **Show the trigger.** Approve a claim, then run
   `SELECT * FROM item_status_history ORDER BY history_id DESC;` - the row was
   written by the database, not by Java.
