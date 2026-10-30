# Viva / Interview Question Bank

85 questions an examiner is likely to ask, grouped by topic. Answers are written
the way you should say them: short first sentence, then one supporting detail.

> Tip: whenever you answer a theory question, finish with **"…and in our project
> that is at ‹file name›."** It converts a textbook answer into evidence.

---

## A. Project overview (1–10)

**1. What is your project?**
A desktop Campus Lost and Found Management System. Students and staff report lost
or found items, the system suggests matches, ownership claims are verified by
security staff, and every recovery is tracked and reported.

**2. Why is this project needed?**
Campus lost-and-found information is scattered across notice boards, WhatsApp groups
and paper registers. It is not searchable, there is no proof of ownership, and the
college cannot produce recovery statistics.

**3. What are the main modules?**
Authentication, item reporting, search and filter, match suggestions, claim
verification and recovery, and reports/analytics. Six screens plus a login window.

**4. What is your technology stack and why?**
Java + Swing for the client, JDBC for persistence, MySQL for storage. No framework,
because the review needs to see SQL and OOPS directly instead of an ORM hiding them.

**5. How many tables and relationships do you have?**
Eight tables and nine relationships. Two 1:1 subtype tables, the rest 1:N.

**6. How is your architecture organised?**
Three layers: UI → service → DAO → MySQL. The UI contains no SQL, the DAO contains
no business rules, and the service owns transactions.

**7. Which part of the project are you most proud of?**
The claim approval transaction: three coordinated updates, with rollback, plus a
database trigger writing the audit trail. It is the part that is genuinely hard to
get right.

**8. What was the hardest bug you faced?**
Mapping one row to two different Java classes. The join returns lost and found
columns merged, so `mapRow` reads the `item_type` discriminator and decides whether
to build a `LostItem` or a `FoundItem`.

**9. How did you test it?**
Manual functional testing with an 18-case table, negative testing (wrong password,
duplicate claim, future date, stopped MySQL), and SQL-level verification of the
transaction and trigger results.

**10. What are the limitations?**
Desktop-only, no photo upload, rule-based matching, SHA-256 without a salt, and
master data (categories, locations) is maintained in SQL rather than in the UI.

---

## B. OOPS (11–32)

**11. What are the four pillars of OOPS?**
Encapsulation, abstraction, inheritance and polymorphism. All four are used in the
`Item` hierarchy and the DAO layer.

**12. Define encapsulation and show it.**
Wrapping data and the methods that operate on it in one class, with private fields.
In `Item` every field is private; `setStatus` also prevents a null status, so the
object cannot enter an invalid state.

**13. Is a getter/setter pair enough for encapsulation?**
No. Encapsulation means the class protects its invariants. Our setters normalise
input (null status becomes OPEN), and validation is centralised in the service so a
caller cannot bypass it.

**14. Define abstraction.**
Exposing only the essential behaviour and hiding the implementation. `Item` declares
`getItemType()` and `getSpecificDetails()` without saying how sub-classes compute
them; `GenericDAO<T>` declares CRUD without mentioning JDBC.

**15. Can you instantiate an abstract class?**
No. `new Item()` is a compile error. You instantiate `LostItem` or `FoundItem`.

**16. Can an abstract class have a constructor?**
Yes. Ours has a protected constructor used by sub-classes and by the DAO while
mapping rows. It cannot be called with `new`, but `super(...)` works.

**17. Difference between abstract class and interface?**
An abstract class can hold state and partial implementation (our `Item` has fields
and `describe()`); an interface is a pure contract (our `GenericDAO`, `ItemDAO`).
A class extends one abstract class but can implement many interfaces.

**18. Why is `GenericDAO` generic?**
So one contract serves every entity: `GenericDAO<Item>`, `GenericDAO<User>`,
`GenericDAO<Claim>`. It removes duplicate method signatures and gives compile-time
type safety.

**19. Define inheritance and show it.**
`LostItem extends Item` and `FoundItem extends Item`. Both reuse id, title,
description, category, location, date, reporter and status, and add only their own
fields.

**20. What is method overriding here?**
`getItemType()` and `getSpecificDetails()` are abstract in `Item` and implemented in
both sub-classes. At runtime the JVM calls the sub-class version.

**21. What is method overloading? Give an example.**
Same name, different parameter list. `ItemDAO.updateStatus(int, ItemStatus)` and
`updateStatus(Connection, int, ItemStatus)`; the second participates in a
caller-owned transaction.

**22. Difference between overriding and overloading?**
Overriding replaces inherited behaviour with the same signature (runtime
polymorphism); overloading adds variants distinguished by parameters (compile-time).

**23. Define polymorphism.**
One reference type, many runtime behaviours. A `List<Item>` holds both lost and
found objects, and `item.getSpecificDetails()` returns different text per object
without any `instanceof` check in the UI.

**24. What are the types of polymorphism?**
Compile-time (overloading) and runtime (overriding / dynamic dispatch). We use both.

**25. Where do you use `instanceof`?**
Only in the persistence layer (`insertSubtypeRow`, `updateSubtypeRow`), because the
DAO must decide which table to write. The UI never needs it - that is the point of
the abstract methods.

**26. What is the difference between composition and inheritance?**
Inheritance is "is-a" (`LostItem` is an `Item`); composition is "has-a"
(`Claim` has an `Item` and a `User`). We use inheritance for item types and
composition for everything else, which is the usual recommendation.

**27. Why not make `LostItem` and `FoundItem` two unrelated classes?**
Because search, reports, claims and matching all treat items uniformly. A common
super-type lets one `items` table, one DAO and one table model serve both.

**28. What is a `final` class / method?**
Cannot be extended / overridden. Our utility classes (`PasswordUtil`,
`DBConnection`) are `final` because inheritance would add no value and could break
their invariants.

**29. What does the `static` keyword mean in your code?**
Belongs to the class, not an object. `DBConnection.getInstance()` is static, and
`SessionContext` keeps the current user statically.

**30. Is your Singleton thread-safe?**
`getInstance()` is `synchronized`, so only one instance is created even if two
threads call it simultaneously. The Swing UI itself is single-threaded.

**31. What is an enum and why use it?**
A fixed set of named constants. `ItemStatus`, `ClaimStatus`, `Role`, `ItemType`,
`Urgency` make invalid values impossible and map directly to MySQL `ENUM` columns.

**32. What is a sealed class / record, and would you use them?**
Records (Java 16+) suit immutable DTOs like `MatchResult`; sealed classes could
restrict `Item` to exactly two sub-types. We kept plain classes plus enums so the
code compiles on Java 8, which many college labs still use.

---

## C. DBMS (33–52)

**33. What is a primary key? Give examples.**
A column (or set) that uniquely identifies a row and cannot be null. `item_id`,
`claim_id`, `user_id`; `lost_items.item_id` is both PK and FK.

**34. What is a foreign key?**
A column referencing another table's primary key, enforcing referential integrity.
`claims.item_id` references `items.item_id`, so a claim cannot exist without an item.

**35. What happens if you delete an item that has claims?**
`claims.item_id` uses `ON DELETE CASCADE`, so the claims and the subtype rows are
deleted with it. Without cascade the delete would fail with a constraint error.

**36. What is the difference between `DELETE`, `TRUNCATE` and `DROP`?**
`DELETE` removes rows (can be filtered, can be rolled back, fires triggers);
`TRUNCATE` empties the table quickly and resets the counter; `DROP` removes the
table definition itself.

**37. Explain your normalisation.**
1NF: atomic columns. 2NF: no partial dependency on a composite key. 3NF: no
transitive dependency - category and location names live in their own tables. The
schema is in 3NF.

**38. Why is the match score not stored?**
It depends on both items and their dates, so storing it would duplicate derived
data and go stale. Computing it in a view guarantees correctness.

**39. What is a view? Which views do you have?**
A named, stored SELECT. `vw_item_details` flattens the join for reporting;
`vw_potential_matches` generates and scores candidate pairs.

**40. Can you insert into a view?**
Only simple, single-table views are updatable; ours contain joins and aggregates, so
they are read-only.

**41. What is a trigger? Show yours.**
A procedure that fires automatically on INSERT/UPDATE/DELETE. We have
`trg_items_after_insert` and `trg_items_after_update`, which insert a row into
`item_status_history` whenever an item is created or its status changes.

**42. Why use a trigger instead of Java code?**
The audit trail becomes independent of the application: any UPDATE, from any client,
is logged. It cannot be forgotten by a developer.

**43. What is a stored procedure and do you use one?**
A named, precompiled block of SQL with parameters. We do not need one: the
multi-statement workflow is implemented in Java so the transaction boundary is
visible in the service layer. A procedure would move the same logic into the
database.

**44. What is a transaction?**
A unit of work that either commits completely or rolls back completely (ACID).

**45. Explain ACID.**
Atomicity (all or nothing), Consistency (constraints hold before and after),
Isolation (concurrent transactions do not see partial work), Durability (committed
data survives a crash). Our approve-claim workflow is the example.

**46. Show your transaction code.**
`ClaimService.runInTransaction`: `setAutoCommit(false)`, three DAO updates,
`commit()`; any exception triggers `rollback()` in the catch block and the
connection is restored in `finally`.

**47. What is a savepoint?**
A named point inside a transaction you can roll back to without discarding the whole
transaction. We do not need one because our unit of work is short.

**48. What is a deadlock? How would you handle it?**
Two transactions each holding a lock the other needs. MySQL detects it and rolls one
back with error 1213. Our transactions touch rows in a consistent order
(claims → items), which reduces the chance; a production system would add retry.

**49. What are indexes and where did you create them?**
B-tree structures that speed up lookups. `idx_items_type`, `idx_items_status`,
`idx_items_search (category_id, location_id, event_date)` and `idx_claims_status`.
The composite index supports the search screen's most common filter combination.

**50. Does an index slow anything down?**
Yes, writes: every INSERT/UPDATE must maintain the index. That is why we index only
the filter and join columns, not every column.

**51. Difference between `WHERE` and `HAVING`?**
`WHERE` filters rows before grouping; `HAVING` filters groups after aggregation.
Our match view uses `WHERE` for candidate conditions and `HAVING`/outer filter for
the score threshold.

**52. Difference between `INNER JOIN` and `LEFT JOIN` in your query?**
The lookup joins are inner because every item must have a category, location and
reporter. The subtype joins are `LEFT` because a lost item has no `found_items` row;
an inner join would drop half the results.

**53. What is a self join and do you use one?**
A table joined to itself. The matching view joins `items` twice - `lo` for lost and
`fo` for found - which is a self join with aliases.

**54. What is a surrogate vs natural key?**
Surrogate: meaningless generated id (`item_id`). Natural: real-world unique value
(`email`). We use surrogates as PKs and keep natural keys as UNIQUE constraints.

**55. Why `DECIMAL` for the reward, not `FLOAT`?**
`FLOAT` is binary floating point and produces rounding errors; `DECIMAL(8,2)` stores
exact paise.

**56. Why `ENUM` instead of a status lookup table?**
The status set is fixed by the workflow and mapped to Java enums. A lookup table
would add a join for no benefit; if the statuses became dynamic, a table would be
the right choice.

---

## D. JDBC (57–64)

**57. What is JDBC?**
A Java API for executing SQL against relational databases. It gives us
`Connection`, `PreparedStatement` and `ResultSet` without tying the code to MySQL.

**58. What are the five steps of JDBC?**
Load the driver, get a connection, create a statement, execute and read the result,
close the resources. We close with try-with-resources.

**59. `Statement` vs `PreparedStatement`?**
`PreparedStatement` is precompiled, faster on repeat use, handles data types, and
binds parameters, which prevents SQL injection. Every DAO uses it.

**60. How does `PreparedStatement` prevent SQL injection?**
The SQL text and the values travel separately; the database never parses a value as
SQL. `' OR 1=1 --` becomes a literal string that matches nothing.

**61. What is `ResultSet`? How do you read it?**
A cursor over the returned rows. `while (rs.next())` advances; `rs.getInt("item_id")`
reads by column label. `mapRow` converts one row into a domain object.

**62. How do you get an auto-generated primary key?**
`prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)`, then read
`getGeneratedKeys()`. Used when inserting an item and a claim.

**63. `executeQuery` vs `executeUpdate`?**
`executeQuery` returns a `ResultSet` (SELECT); `executeUpdate` returns a row count
(INSERT/UPDATE/DELETE).

**64. Why do you convert between `LocalDate` and `java.sql.Date`?**
`LocalDate` is the modern, immutable Java date type; JDBC expects `java.sql.Date`.
`AbstractJdbcDAO` converts in both directions so the rest of the code stays modern.

**65. What is connection pooling and do you use it?**
Reusing a pool of physical connections instead of opening one per request. We open
one per operation for clarity - adequate for a single-user desktop client; a web
deployment would use HikariCP.

**66. What is `Class.forName("com.mysql.cj.jdbc.Driver")` for?**
It loads and registers the driver. Modern JDBC auto-loads via the service loader,
but doing it explicitly gives a clear error if the jar is missing.

---

## E. Swing (67–71)

**67. What is Swing?**
A lightweight GUI toolkit in the JDK, built on AWT, with pluggable look and feel and
platform-independent components.

**68. What layout managers do you use?**
`BorderLayout` for the page skeleton, `BoxLayout` for vertical forms, `GridLayout`
for equal-width filter rows and counter cards, `CardLayout` for switching screens and
the lost/found form section.

**69. Why is `CardLayout` important in your UI?**
It lets one window host many screens and swap them without creating new frames. The
main shell and the report form both use it.

**70. What is the event dispatch thread?**
The single thread that handles Swing events. UI updates must run on it; our
`main` starts the window inside `SwingUtilities.invokeLater`.

**71. What is the difference between `JFrame`, `JDialog` and `JPanel`?**
`JFrame` is a top-level window; `JDialog` is a modal/secondary window (our item
details and claim form); `JPanel` is a container placed inside another container.

**72. How do you share the look across screens?**
One `UIStyle` class holds colours, fonts, buttons, cards, field and table styling.
Changing the theme there changes every screen.

---

## F. Security and design (73–77)

**73. How are passwords stored?**
As SHA-256 hex digests in `users.password_hash`. Login hashes the entered password
and compares digests, so the plain text is never stored.

**74. Why is SHA-256 not ideal for passwords?**
It is fast, which helps an attacker brute-force. Production systems should use a
salted, slow algorithm (BCrypt, SCrypt, Argon2). We document this as a limitation.

**75. How do you prevent a student from approving claims?**
Two layers: the UI hides the buttons, and `ClaimService.requireVerifier()` throws a
`ValidationException` if the role is not STAFF or ADMIN. Authorisation never depends
on the UI alone.

**76. How do you avoid duplicate claims?**
Three layers: the UI checks status, `ClaimService` calls `claimDAO.existsFor`, and
the database has `UNIQUE (item_id, claimant_id)`. The constraint is the final
authority even if two clients race.

**77. What happens if the database is down?**
`DBConnection` fails fast with a readable message, and every DAO call is wrapped in
`DataAccessException` so the UI shows a dialog instead of a stack trace.

---

## G. Rapid-fire and trap questions (78–84)

**78. Is Java 100% object-oriented?**
No - it has primitives (`int`, `boolean`) and static members. Smalltalk is the usual
example of a pure OO language.

**79. Can you override a static method?**
No, it is hidden, not overridden. Calls are resolved at compile time.

**80. Can you have a `private` constructor?**
Yes, and we do: `DBConnection` and `PasswordUtil` prevent instantiation.

**81. Why does your `Item` class have a no-argument constructor?**
The DAO creates the object first and then fills it from the `ResultSet`. A
framework-free mapper needs an empty object.

**82. What if two users report the same item twice?**
The schema does not forbid duplicate reports, and that is intentional: one may be a
lost report and one a found report. The matching view then pairs them. Exact
duplicate prevention would need a similarity rule, which is future work.

**83. Why is the match list not saved in a table?**
It is derived data. Storing it would require refresh logic on every insert, update
and delete. A view is always consistent.

**84. If you had two more weeks, what would you build first?**
Photo upload with image similarity, then notifications on match. Both directly
reduce recovery time, which is the project's main metric.

**85. What did you learn from this project?**
That OOPS and DBMS are not separate subjects: the inheritance hierarchy forces the
class-table schema, the transaction boundary lives in the service layer, and the
matching engine is pure SQL. Getting the layers right made every later feature
cheap.

---

## Quick mapping: question → file to open

| Examiner asks | Open this |
|---|---|
| Inheritance / polymorphism | `model/Item.java`, `LostItem.java`, `FoundItem.java` |
| Encapsulation | `model/Item.java` (private fields + guarded setters) |
| Abstraction / interfaces | `dao/GenericDAO.java`, `dao/ItemDAO.java` |
| Generics | `dao/GenericDAO.java`, `dao/AbstractJdbcDAO.java` |
| Singleton | `config/DBConnection.java` |
| Transaction | `service/ClaimService.java` → `runInTransaction` |
| SQL joins | `dao/JdbcItemDAO.java` → `BASE_SELECT` |
| SQL injection safety | any `prepareStatement` + `bind(...)` |
| Trigger | `database/schema.sql` → section 9 |
| View / self join | `database/schema.sql` → `vw_potential_matches` |
| Aggregates | `dao/JdbcItemDAO.java` → `countByCategory` |
| Role checks | `util/SessionContext.java`, `ClaimService.requireVerifier` |
| Swing layouts | `ui/MainFrame.java`, `ui/ReportItemPanel.java` |
