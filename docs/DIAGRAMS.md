# Diagrams — Campus Lost & Found

Every diagram below is plain text so you can redraw it in PowerPoint, draw.io or
dbdiagram.io. Redrawing takes 10–15 minutes and looks far better than a screenshot.

Recommended tools:
- **ER diagram** → <https://dbdiagram.io> (paste the DBML at the end of section 1)
- **Class diagram** → draw.io → UML → Class shapes
- **Architecture / DFD** → PowerPoint SmartArt or draw.io boxes

---

## 1. Entity–Relationship diagram

### 1.1 Layout

```
        ┌────────────────────┐        ┌────────────────────┐        ┌────────────────────┐
        │       USERS        │        │     CATEGORIES     │        │     LOCATIONS      │
        ├────────────────────┤        ├────────────────────┤        ├────────────────────┤
        │ user_id       PK   │        │ category_id   PK   │        │ location_id   PK   │
        │ full_name          │        │ name         UQ    │        │ name         UQ    │
        │ email         UQ   │        │ description        │        │ building           │
        │ phone              │        └────────────────────┘        └────────────────────┘
        │ password_hash      │                  │                            │
        │ role               │                  │ 1                          │ 1
        │ created_at         │                  │                            │
        └─────────┬──────────┘                  │                            │
                  │ 1                           │                            │
                  │                             │ N                          │ N
                  │              ┌──────────────┴────────────────────────────┴──┐
                  │              │                    ITEMS                      │
                  └──────────────┤ item_id            PK                       │
                        reports  │ item_type          {LOST | FOUND}           │
                             N   │ title / description                         │
                                 │ category_id        FK → categories          │
                                 │ location_id        FK → locations           │
                                 │ event_date / status / created_at             │
                                 │ reporter_id        FK → users               │
                                 └────────┬──────────────────────┬─────────────┘
                                          │ 1                    │ 1
                          ┌───────────────┴──────┐      ┌────────┴─────────────┐
                          │      LOST_ITEMS      │      │     FOUND_ITEMS      │
                          ├──────────────────────┤      ├──────────────────────┤
                          │ item_id  PK FK       │      │ item_id  PK FK       │
                          │ contact              │      │ storage_location     │
                          │ reward_offered       │      │ handed_to_security   │
                          │ urgency              │      │ finder_notes         │
                          └──────────────────────┘      └────────┬─────────────┘
                                                                  │ 1
                                                                  │
        ┌────────────────────┐                          ┌─────────┴──────────┐
        │ ITEM_STATUS_HISTORY│                          │       CLAIMS       │
        ├────────────────────┤                          ├────────────────────┤
        │ history_id    PK   │                          │ claim_id      PK   │
        │ item_id       FK   │◄──────── N ── 1 ────────►│ item_id       FK   │
        │ old_status         │                          │ claimant_id   FK   │
        │ new_status         │                          │ verified_by   FK   │
        │ note / changed_at  │                          │ proof / status     │
        └────────────────────┘                          │ submitted_at       │
                                                        │ verified_at        │
                                                        │ remarks            │
                                                        └────────────────────┘
```

### 1.2 Relationships

| # | Relationship | Cardinality | Implementation |
|---|---|---|---|
| 1 | A user reports many items | 1 : N | `items.reporter_id` |
| 2 | A category groups many items | 1 : N | `items.category_id` |
| 3 | A location holds many items | 1 : N | `items.location_id` |
| 4 | An item is exactly one lost report | 1 : 1 | `lost_items.item_id` PK = FK |
| 5 | An item is exactly one found report | 1 : 1 | `found_items.item_id` PK = FK |
| 6 | An item receives many claims | 1 : N | `claims.item_id` |
| 7 | A user files many claims | 1 : N | `claims.claimant_id` |
| 8 | A staff member verifies many claims | 1 : N | `claims.verified_by` |
| 9 | An item has many status changes | 1 : N | `item_status_history.item_id` |

### 1.3 DBML (paste into dbdiagram.io)

```dbml
Table users {
  user_id       int [pk, increment]
  full_name     varchar(80)  [not null]
  email         varchar(120) [not null, unique]
  phone         varchar(15)  [not null]
  password_hash char(64)     [not null]
  role          enum("STUDENT","STAFF","ADMIN") [not null, default: 'STUDENT']
  created_at    timestamp    [default: `now()`]
}

Table categories {
  category_id int [pk, increment]
  name        varchar(50) [not null, unique]
  description varchar(120)
}

Table locations {
  location_id int [pk, increment]
  name        varchar(60) [not null, unique]
  building    varchar(60) [not null]
}

Table items {
  item_id     int [pk, increment]
  item_type   enum("LOST","FOUND") [not null]
  title       varchar(120) [not null]
  description text         [not null]
  category_id int [not null, ref: > categories.category_id]
  location_id int [not null, ref: > locations.location_id]
  event_date  date [not null]
  reporter_id int  [not null, ref: > users.user_id]
  status      enum("OPEN","MATCHED","CLAIMED","RECOVERED","CLOSED") [not null, default: 'OPEN']
  created_at  timestamp [default: `now()`]
  indexes { (item_type) (status) (category_id, location_id, event_date) }
}

Table lost_items {
  item_id        int [pk, ref: - items.item_id]
  contact        varchar(80)  [not null]
  reward_offered decimal(8,2) [not null, default: 0]
  urgency        enum("LOW","MEDIUM","HIGH") [not null, default: 'MEDIUM']
}

Table found_items {
  item_id            int [pk, ref: - items.item_id]
  storage_location   varchar(120) [not null]
  handed_to_security boolean [not null, default: true]
  finder_notes       varchar(255)
}

Table claims {
  claim_id     int [pk, increment]
  item_id      int [not null, ref: > items.item_id]
  claimant_id  int [not null, ref: > users.user_id]
  proof        text [not null]
  status       enum("PENDING","APPROVED","REJECTED","RECOVERED") [not null, default: 'PENDING']
  submitted_at timestamp [default: `now()`]
  verified_by  int [ref: > users.user_id]
  verified_at  datetime
  remarks      varchar(255)
  indexes { (item_id, claimant_id) [unique] (status) }
}

Table item_status_history {
  history_id int [pk, increment]
  item_id    int [not null, ref: > items.item_id]
  old_status varchar(15)
  new_status varchar(15) [not null]
  note       varchar(120)
  changed_at timestamp [default: `now()`]
}
```

---

## 2. Class diagram

### 2.1 Model layer

```
                         ┌────────────────────────────────────┐
                         │            <<abstract>>            │
                         │               Item                 │
                         ├────────────────────────────────────┤
                         │ - itemId : int                     │
                         │ - title : String                   │
                         │ - description : String             │
                         │ - category : Category              │
                         │ - location : Location              │
                         │ - eventDate : LocalDate            │
                         │ - reportedBy : User                │
                         │ - status : ItemStatus              │
                         │ - createdAt : Timestamp            │
                         ├────────────────────────────────────┤
                         │ + getItemType() : ItemType      ▲  │
                         │ + getSpecificDetails() : String ▲  │  abstract
                         │ + describe() : String              │
                         └───────────────┬────────────────────┘
                                         │
                 ┌───────────────────────┴────────────────────────┐
                 │                                                │
   ┌─────────────▼──────────────┐                 ┌───────────────▼────────────┐
   │         LostItem           │                 │         FoundItem          │
   ├────────────────────────────┤                 ├────────────────────────────┤
   │ - contact : String         │                 │ - storageLocation : String │
   │ - rewardOffered : BigDecimal│                │ - handedToSecurity : boolean│
   │ - urgency : Urgency        │                 │ - finderNotes : String     │
   ├────────────────────────────┤                 ├────────────────────────────┤
   │ + getItemType() : LOST     │                 │ + getItemType() : FOUND    │
   │ + getSpecificDetails()     │                 │ + getSpecificDetails()     │
   └────────────────────────────┘                 └────────────────────────────┘

   Item  *── 1  Category            Item  *── 1  Location
   Item  *── 1  User (reporter)      Claim *── 1  Item
   Claim *── 1  User (claimant)      Claim *── 1  User (verifier)
```

### 2.2 Persistence and service layers

```
«interface»                       «abstract»
GenericDAO<T>                     AbstractJdbcDAO<T>
  + findById(int) : T               # openConnection() : Connection
  + findAll() : List<T>             # wrap(String, SQLException)
  + insert(T) : int                 # bind(PreparedStatement, List)
  + update(T) : boolean             # toLocalDate(java.sql.Date)
  + delete(int) : boolean
        ▲                                   ▲
        │ implements                        │ extends
   ┌────┴──────────┬──────────────┬──────────┴────────┐
ItemDAO        UserDAO        ClaimDAO           LookupDAO
   ▲                ▲              ▲                  ▲
JdbcItemDAO   JdbcUserDAO   JdbcClaimDAO      JdbcLookupDAO
                                    (all extend AbstractJdbcDAO)

Services (use the interfaces only):
  AuthService   → UserDAO
  ItemService   → ItemDAO, LookupDAO
  ClaimService  → ClaimDAO, ItemDAO      ← owns the transaction
  MatchService  → ItemDAO
  ReportService → ItemDAO, ClaimDAO
```

**Talking point:** services depend on interfaces, not on `JdbcXxxDAO`. Swapping
MySQL for another database touches only the DAO implementations.

---

## 3. Use-case diagram (text form)

```
Actor: STUDENT
  ├── Register account
  ├── Sign in / sign out
  ├── Report lost item
  ├── Report found item
  ├── Search and filter items
  ├── View item details
  ├── Submit ownership claim
  └── Track own claims

Actor: STAFF (Security office)
  ├── everything a student can do
  ├── View all claims
  ├── Approve / reject claim        <<include>> verify proof of ownership
  └── Mark item recovered           <<extend>> hand over item

Actor: ADMIN
  ├── everything staff can do
  └── (accounts and master data maintained at database level)

System boundary: Campus Lost & Found System
External dependency: MySQL Server
```

---

## 4. System architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                              │
│  LoginFrame · MainFrame                                                │
│  DashboardPanel · ReportItemPanel · SearchPanel · MatchesPanel          │
│  ClaimsPanel · ReportsPanel · ItemDetailDialog · UIStyle               │
│  (Swing/AWT only - no SQL, no JDBC imports)                            │
└───────────────────────────────┬────────────────────────────────────────┘
                                │ method calls (domain objects)
┌───────────────────────────────▼────────────────────────────────────────┐
│                          SERVICE LAYER                                 │
│  AuthService (login, register)                                         │
│  ItemService (validation, reporting, search)                            │
│  ClaimService (claim rules, TRANSACTION boundary)                       │
│  MatchService (match lookup, explanation)                              │
│  ReportService (aggregates, CSV export)                                │
│  (no Swing imports, no SQL strings)                                    │
└───────────────────────────────┬────────────────────────────────────────┘
                                │ DAO interfaces (polymorphism)
┌───────────────────────────────▼────────────────────────────────────────┐
│                       PERSISTENCE LAYER                                │
│  GenericDAO<T> · ItemDAO · UserDAO · ClaimDAO · LookupDAO               │
│  AbstractJdbcDAO<T>                                                    │
│  JdbcItemDAO · JdbcUserDAO · JdbcClaimDAO · JdbcLookupDAO               │
│  (PreparedStatement, ResultSet mapping, DataAccessException)            │
└───────────────────────────────┬────────────────────────────────────────┘
                                │ JDBC (TCP 3306)
┌───────────────────────────────▼────────────────────────────────────────┐
│                          DATABASE LAYER                                │
│  MySQL 8: 8 tables · 2 views · 2 triggers · 6 indexes                   │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Data-flow diagrams

### 5.1 Level 0 (context)

```
   ┌──────────┐   item report, claim, search    ┌──────────────────────┐
   │ Student  │────────────────────────────────►│                      │
   │          │◄────────────────────────────────│  Campus Lost & Found │
   └──────────┘   match list, claim status      │      System          │
   ┌──────────┐   verify claim, handover        │                      │
   │  Staff   │────────────────────────────────►│                      │
   │          │◄────────────────────────────────│                      │
   └──────────┘   pending claims, reports       └──────────┬───────────┘
                                                           │ SQL
                                                ┌──────────▼───────────┐
                                                │   MySQL Database     │
                                                └──────────────────────┘
```

### 5.2 Level 1 (main processes)

```
 Student ──► 1.0 Authenticate ──► users table
             │
             ▼
         2.0 Report item ──► items + lost_items/found_items (transaction)
             │
             ▼
         3.0 Search / filter ──► items ⋈ categories ⋈ locations ⋈ users
             │
             ▼
         4.0 Match engine ──► vw_potential_matches ──► scored pairs
             │
             ▼
         5.0 Claim workflow ──► claims + items (transaction)
             │                        │
             │                        └──► trigger ──► item_status_history
             ▼
         6.0 Reports ──► GROUP BY aggregates ──► dashboard + CSV
```

---

## 6. Sequence diagrams

### 6.1 Login

```
User      LoginFrame        AuthService        JdbcUserDAO        MySQL
 │            │                  │                  │               │
 │ email+pwd  │                  │                  │               │
 ├───────────►│ login(email,pwd) │                  │               │
 │            ├─────────────────►│ findByEmail      │               │
 │            │                  ├─────────────────►│ SELECT ...    │
 │            │                  │                  ├──────────────►│
 │            │                  │                  │◄──────────────┤
 │            │                  │◄─────────────────┤ User + hash   │
 │            │                  │ SHA-256 compare  │               │
 │            │◄─────────────────┤ User / throw     │               │
 │            │ SessionContext   │                  │               │
 │            │ .login(user)     │                  │               │
 │◄───────────┤ MainFrame opens  │                  │               │
```

### 6.2 Report an item (two INSERTs, one transaction)

```
UI          ItemService        JdbcItemDAO              MySQL
 │              │                   │                     │
 │ buildItem()  │                   │                     │
 ├─────────────►│ validate(item)    │                     │
 │              ├──────────────────►│ setAutoCommit(false)│
 │              │                   ├────────────────────►│
 │              │                   │ INSERT items        │
 │              │                   ├────────────────────►│
 │              │                   │◄── generated id ────┤
 │              │                   │ INSERT lost/found   │
 │              │                   ├────────────────────►│
 │              │                   │ COMMIT              │
 │              │◄──────────────────┤ id                  │
 │◄─────────────┤                   │                     │
```

### 6.3 Approve claim (atomic transaction)

```
Staff        ClaimsPanel        ClaimService        ClaimDAO / ItemDAO      MySQL
 │              │                    │                    │                  │
 │ Approve ────►│ approveClaim(...)  │                    │                  │
 │              ├───────────────────►│ requireVerifier    │                  │
 │              │                    │ conn.setAutoCommit(false)             │
 │              │                    ├───────────────────►│                  │
 │              │                    │                    ├─────────────────►│
 │              │                    │ 1. claims → APPROVED                  │
 │              │                    │ 2. items  → CLAIMED                   │
 │              │                    │ 3. other pending claims → REJECTED    │
 │              │                    │                    │◄── trigger logs  │
 │              │                    │ COMMIT             │                  │
 │              │◄───────────────────┤ ok                 │                  │
 │◄─────────────┤ refresh            │                    │                  │
 │              │                    │ on exception: ROLLBACK ──► no changes │
```

### 6.4 Matching query

```
MatchesPanel       JdbcItemDAO                 MySQL (view)
     │                  │                          │
     │ refreshData()    │                          │
     ├─────────────────►│ SELECT * FROM            │
     │                  │ vw_potential_matches     │
     │                  ├─────────────────────────►│ join items lo × fo
     │                  │                          │ same category + location
     │                  │                          │ ≤ 30 days, both open
     │                  │                          │ compute score, sort
     │                  │◄─────────────────────────┤ rows
     │◄─────────────────┤ List<MatchResult>        │
     │ render + explain │                          │
```

---

## 7. State diagrams

### 7.1 Item life cycle

```
                 report
                   │
                   ▼
              ┌─────────┐   claim approved    ┌──────────┐   handover   ┌────────────┐
              │  OPEN   │────────────────────►│ CLAIMED  │─────────────►│ RECOVERED  │
              └────┬────┘                     └──────────┘              └─────┬──────┘
                   │                                                         │
        match found│                                            archive      │
                   ▼                                                         ▼
              ┌─────────┐                                                ┌────────┐
              │ MATCHED │───────────────────────────────────────────────►│ CLOSED │
              └─────────┘        cancel / item not collected             └────────┘
```

### 7.2 Claim life cycle

```
   submit
     │
     ▼
 ┌─────────┐   staff approve   ┌──────────┐   item handed over   ┌───────────┐
 │ PENDING │──────────────────►│ APPROVED │─────────────────────►│ RECOVERED │
 └────┬────┘                   └──────────┘                      └───────────┘
      │
      │ staff reject
      ▼
 ┌──────────┐
 │ REJECTED │   (item returns to / stays OPEN)
 └──────────┘
```

---

## 8. Data dictionary

### users
| Column | Type | Constraint | Meaning |
|---|---|---|---|
| user_id | INT AI | PK | Surrogate key |
| full_name | VARCHAR(80) | NOT NULL | Display name |
| email | VARCHAR(120) | NOT NULL, UNIQUE | Login id |
| phone | VARCHAR(15) | NOT NULL, CHECK length ≥ 10 | Contact |
| password_hash | CHAR(64) | NOT NULL | SHA-256 hex digest |
| role | ENUM | NOT NULL, default STUDENT | STUDENT / STAFF / ADMIN |
| created_at | TIMESTAMP | default now | Registration time |

### categories / locations
| Column | Type | Constraint | Meaning |
|---|---|---|---|
| category_id / location_id | INT AI | PK | Surrogate key |
| name | VARCHAR | NOT NULL, UNIQUE | Display value in combo boxes |
| description / building | VARCHAR | nullable / NOT NULL | Extra context |

### items
| Column | Type | Constraint | Meaning |
|---|---|---|---|
| item_id | INT AI | PK | Surrogate key |
| item_type | ENUM('LOST','FOUND') | NOT NULL | Inheritance discriminator |
| title | VARCHAR(120) | NOT NULL | Short label |
| description | TEXT | NOT NULL | Detail used by search and matching |
| category_id | INT | FK → categories | Classification |
| location_id | INT | FK → locations | Where it was lost/found |
| event_date | DATE | NOT NULL, validated ≤ today | When it happened |
| reporter_id | INT | FK → users | Who filed the report |
| status | ENUM(5 values) | NOT NULL, default OPEN | Life cycle |
| created_at | TIMESTAMP | default now | Audit |

### lost_items
| Column | Type | Constraint | Meaning |
|---|---|---|---|
| item_id | INT | PK, FK → items, cascade | 1:1 link |
| contact | VARCHAR(80) | NOT NULL | How to reach the owner |
| reward_offered | DECIMAL(8,2) | NOT NULL, CHECK ≥ 0 | Optional reward |
| urgency | ENUM | NOT NULL | LOW / MEDIUM / HIGH |

### found_items
| Column | Type | Constraint | Meaning |
|---|---|---|---|
| item_id | INT | PK, FK → items, cascade | 1:1 link |
| storage_location | VARCHAR(120) | NOT NULL | Where the item is kept |
| handed_to_security | BOOLEAN | NOT NULL | Custody flag |
| finder_notes | VARCHAR(255) | nullable | Extra remarks |

### claims
| Column | Type | Constraint | Meaning |
|---|---|---|---|
| claim_id | INT AI | PK | Surrogate key |
| item_id | INT | FK → items | Claimed item |
| claimant_id | INT | FK → users | Who claims it |
| proof | TEXT | NOT NULL | Ownership description |
| status | ENUM(4) | NOT NULL, default PENDING | Workflow state |
| submitted_at | TIMESTAMP | default now | When filed |
| verified_by | INT | FK → users, nullable | Verifying staff |
| verified_at | DATETIME | nullable | Decision time |
| remarks | VARCHAR(255) | nullable | Reason / note |

### item_status_history
| Column | Type | Constraint | Meaning |
|---|---|---|---|
| history_id | INT AI | PK | Surrogate key |
| item_id | INT | FK → items, cascade | Audited item |
| old_status | VARCHAR(15) | nullable | Before (null on insert) |
| new_status | VARCHAR(15) | NOT NULL | After |
| note | VARCHAR(120) | nullable | Trigger description |
| changed_at | TIMESTAMP | default now | When |

---

## 9. Normalisation argument (say this in the viva)

1. **1NF** — every column holds one atomic value; the `contact` field is the only
   composite string, and it is display data, not a key.
2. **2NF** — no partial dependency: `lost_items` and `found_items` use the full
   primary key (`item_id`), and all non-key attributes depend on it entirely.
3. **3NF** — no transitive dependency: category name and location name live in
   their own tables and are referenced by id, so renaming a location happens in one
   row. `items` stores only foreign keys, dates and descriptions.
4. **Deliberate denormalisation** — the match score is not stored; it is computed in
   a view so it can never become stale. This is a performance/correctness trade-off
   worth explaining.
