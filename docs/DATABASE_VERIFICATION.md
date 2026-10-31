# Database and application verification log

This file records the checks that were actually executed against a live server —
not what the code is expected to do, but what it did.

Keep it with the submission: if an examiner asks *"how do you know the schema
works?"*, this is the evidence.

---

## 1. Environment used

| Item | Value |
|---|---|
| Server | MariaDB 11.4.5 (Windows x64, portable build) |
| Client | `mysql` command-line client, port 3307 |
| Driver | MySQL Connector/J 8.4.0 (the jar shipped in `lib/`) |
| JDK | OpenJDK 21.0.13 (`javac -encoding UTF-8`) |
| Schema file | `database/schema.sql` |

MariaDB was chosen only because it was available offline; every statement in the
schema is standard MySQL 8 syntax, and the same script imports unchanged on
MySQL 8.0 / 8.4 and on XAMPP.

---

## 2. Import

```bat
mysql -u root -h 127.0.0.1 -P 3307 < database\schema.sql
```

Result: **exit code 0, no errors, no warnings**. The script is idempotent — it
begins with `DROP DATABASE IF EXISTS campus_lost_found`, so it can be re-run
before every demo to return to a known state.

---

## 3. Object counts after import

```sql
SELECT 'tables',   COUNT(*) FROM information_schema.tables
  WHERE table_schema='campus_lost_found' AND table_type='BASE TABLE'
UNION ALL SELECT 'views',    COUNT(*) FROM information_schema.views
  WHERE table_schema='campus_lost_found'
UNION ALL SELECT 'triggers', COUNT(*) FROM information_schema.triggers
  WHERE trigger_schema='campus_lost_found'
UNION ALL SELECT 'foreign keys', COUNT(*) FROM information_schema.key_column_usage
  WHERE table_schema='campus_lost_found' AND referenced_table_name IS NOT NULL;
```

| Object | Expected | Observed |
|---|---|---|
| Base tables | 8 | **8** |
| Views | 2 | **2** |
| Triggers | 2 | **2** |
| Foreign-key columns | 9 | **9** |
| Users | 5 | **5** |
| Items | 14 (7 lost / 7 found) | **14 (7 / 7)** |
| Claims | 5 (1 approved, 1 recovered, 1 rejected, 2 pending) | **5** |
| `item_status_history` rows | one per item | **14** |
| `vw_item_details` rows | 14 | **14** |
| `vw_potential_matches` rows | 6 | **6** |

The two seed claims that are still `PENDING` are **both on item 14** (the Casio
calculator), so the "approve one claim, the competitor is auto-rejected" story
can be demonstrated without any preparation.

---

## 4. Matching view output

```sql
SELECT lost_item_id, found_item_id, category_name, location_name,
       days_apart, match_score
FROM vw_potential_matches;
```

| lost | found | category | location | days apart | score |
|---|---|---|---|---|---|
| 9 | 10 | Electronics | Parking Lot | 0 | 100 |
| 13 | 14 | Electronics | CS Block | 0 | 100 |
| 1 | 2 | Electronics | CS Block | 1 | 100 |
| 3 | 4 | Accessories | Canteen | 1 | 100 |
| 13 | 2 | Electronics | CS Block | 9 | 75 |
| 1 | 14 | Electronics | CS Block | 10 | 75 |

This confirms the scoring rules: 40 (category) + 30 (location) + 20/12/5 (date
proximity) + 10 (title keyword), filtered at `match_score >= 50`.

`vw_potential_matches` deliberately contains **no subquery in its `FROM` clause** —
MySQL rejects that in a view definition. The score is written once in the
`SELECT` list and filtered with `HAVING`, which MySQL allows to reference a
select alias.

---

## 5. The claim transaction, tested by hand

```sql
SET autocommit = 0;
UPDATE claims SET status='APPROVED', verified_by=2, verified_at=NOW() WHERE claim_id=3;
UPDATE items  SET status='CLAIMED' WHERE item_id=14;
UPDATE claims SET status='REJECTED'
 WHERE item_id=14 AND claim_id<>3 AND status='PENDING';
COMMIT;
```

| Check | Observed |
|---|---|
| Claim 3 | `APPROVED` |
| Claim 2 (same item) | `REJECTED` — closed automatically |
| Item 14 | `CLAIMED` |
| `item_status_history` | new row `OPEN → CLAIMED` written by `trg_items_after_update` |

### Rollback path

```sql
SET autocommit = 0;
UPDATE claims SET status='APPROVED' WHERE claim_id=2;
UPDATE items  SET status='RECOVERED' WHERE item_id=14;
ROLLBACK;
```

Both rows were unchanged afterwards — no half-applied state, which is the whole
point of wrapping the three statements in one unit of work.

### Constraint checks

```sql
INSERT INTO claims (item_id, claimant_id, proof, status) VALUES (14, 5, 'duplicate', 'PENDING');
-- ERROR 1062 (23000): Duplicate entry '14-5' for key 'uq_claim_once'
```

Duplicate claims are blocked by the database, not only by Java.

---

## 6. End-to-end application test (real service + DAO + JDBC)

A throw-away harness was compiled against the project's `out/` classes and the
shipped Connector/J jar, then run against the live database. It drives the
production classes only — no test doubles.

**Result: 30 checks, 30 passed, 0 failed.**

| Area | What was exercised | Result |
|---|---|---|
| Authentication | admin / staff / student login; wrong password; unknown e-mail | identical error message for both (`Invalid e-mail address or password.`) — no account enumeration |
| Lookups | `ItemService.categories()` / `locations()` | 9 and 8 |
| Search | no filter, keyword, keyword + category, type filter | 14 / 2 / 2 / 7 rows |
| Injection | keyword `' OR 1=1 -- ` | zero rows, query unaffected |
| Reporting | `reportItem()` for a new `LostItem` | `items` + `lost_items` written in one transaction, `item_id` returned |
| Polymorphism | `findById(newId)` | returns `LostItem`, `getSpecificDetails()` renders contact / reward / urgency |
| Validation | 2-character title | rejected with a readable message |
| Matching | `findMatches()` and `explain()` | 6 pairs; explanation reads “Same category (Electronics) and same location (Parking Lot)…” |
| Claiming | `submitClaim()` then a duplicate | second attempt rejected |
| Approval transaction | `approveClaim()` | item `CLAIMED`, both competing claims `REJECTED`, 0 still pending |
| Authorisation | student calls `approveClaim()` | `Only staff or an administrator can verify claims.` |
| Recovery | `markRecovered()` | claim `RECOVERED`, item `RECOVERED`, audit row written |
| Reports | `buildSummary()` | 15 items, 12 open, 1 claimed, 2 recovered, recovery rate 13.33 % |
| Breakdowns | status / category / monthly | `{Open=12, Claimed=1, Recovered=2}`, `{Electronics=6, Accessories=4, …}` |
| Export | CSV summary | file written, 301 bytes |

After the run the schema was re-imported, so the shipped database is back to the
documented seed state.

---

## 7. Reproducing the checks

```bat
REM 1. import (fresh state)
mysql -u root -p < database\schema.sql

REM 2. object counts and the transaction
mysql -u root -p campus_lost_found < database\verify.sql

REM 3. the application itself
compile.bat
run.bat
```

`database/verify.sql` contains the queries from sections 3–5 as a single script.

---

## 8. Known constraints of this validation

- The Swing interface was compiled and inspected, but the screenshots in the
  deck are placeholders — they must be captured on the presentation machine.
- The app was exercised through its service layer, not through mouse clicks; the
  panel code was reviewed but not UI-automated.
- Passwords are SHA-256 without a salt. This is stated openly as a limitation:
  fine for a campus mini-project, not for production (see `docs/VIVA_QA.md`).
