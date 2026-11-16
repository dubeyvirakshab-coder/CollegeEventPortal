# Student Portal – Integration Guide (for the teammate who owns the database)

The Student Portal is an extra module inside the existing EventPortal project.
Same package structure, same database, same Maven setup. `pom.xml`, `Main.java`
and `DBConnection.java` are NOT changed.

## 1. Files

**New (copy into the project at the same paths)**

| File | Purpose |
|---|---|
| `src/main/java/com/eventportal/dao/StudentDAO.java` | Student password storage / checking |
| `src/main/java/com/eventportal/service/StudentService.java` | All student rules + validation |
| `src/main/java/com/eventportal/exception/StudentServiceException.java` | Friendly error messages |
| `src/main/java/com/eventportal/ui/StudentLoginFrame.java` | Login + create account |
| `src/main/java/com/eventportal/ui/StudentDashboardFrame.java` | Home screen |
| `src/main/java/com/eventportal/ui/BrowseEventsFrame.java` | Browse / search / register |
| `src/main/java/com/eventportal/ui/MyRegistrationsFrame.java` | My registrations / cancel |
| `src/main/java/com/eventportal/ui/StudentProfileFrame.java` | Profile |
| `src/main/java/com/eventportal/ui/StudentTheme.java`, `StudentSidebar.java` | Shared look + navigation |
| `src/main/java/com/eventportal/TestStudentPortal.java` | Self-cleaning end-to-end check |
| `src/test/java/com/eventportal/preview/StudentUiPreview.java` | Demo-data preview (no DB) |
| `sql/student_portal_migration.sql` | The one schema change |

**Modified (replace the existing file; changes are additions only)**

| File | What was added |
|---|---|
| `dao/ParticipantDAO.java` | `getParticipantById`, `updateProfile` (appended; nothing removed) |
| `dao/RegistrationDAO.java` | `getRegistrationsByParticipant`, `hasActiveRegistration`, and `cancelRegistration(registrationId, participantId)`. The existing `cancelRegistration(int)` now delegates to a shared private method with identical behaviour (admin path = no owner check). |
| `ui/LoginFrame.java` | A small "Student Portal" link under the admin login; card height 490 → 530 |

## 2. Database change (run once)

Run `sql/student_portal_migration.sql` on `college_event_portal`. It adds two
nullable columns to `participants`: `password_hash`, `password_salt`.
No existing data changes. Passwords are never stored, only salted PBKDF2 hashes
(same `PasswordUtil` as the admin login).

## 3. How students get accounts

- **New student:** "Create Student Account" → a new `participants` row with a password.
- **Student already added by an admin:** they use the same email **and the same phone number**
  on record to set a password (it keeps their existing participant ID and registrations).
  Phone is the only proof of identity, since the app has no email verification.
- Students log in with email + password. A student can never reach admin screens.

## 4. Please verify on the real database

1. **Schema was not in the ZIP.** I tested on a schema inferred from the DAO SQL
   (table/column names used by the existing code). Run `TestStudentPortal` on yours.
2. **UNIQUE key on registrations?** Run `SHOW CREATE TABLE registrations;`.
   If there is `UNIQUE(event_id, participant_id)`, re-registering after a cancel fails
   ("Registration could not be completed"). That limitation also exists on the admin side.
   Fix = drop that unique key (the DAO already prevents duplicate *active* registrations).
3. **Unique emails:** the migration script contains a duplicate-email check and an optional
   `UNIQUE(email)` line.
4. **DB password is hard-coded in `DBConnection.java`.** Move it to an environment variable
   (or keep that file out of GitHub) before publishing the repo.
5. `EventDAO.getAllEvents()` throws a NullPointerException if the DB is down (it doesn't
   null-check the connection). The student service catches this and shows a friendly message.

## 5. Student rules I added (change in `StudentService` if the college wants different ones)

- Cannot register for an event that has already started, is full, or is already registered.
- Cannot cancel once the event has started or attendance is marked.
- Email is read-only in the profile (it is the login); name and phone are editable.

## 6. Run / test

- Real app: run `Main` → admin login → "Student Portal" link (or run `StudentLoginFrame`).
- Check everything: run `TestStudentPortal` (creates `ZZ_TEST…` data, deletes it afterwards).
- Preview without DB: run `StudentUiPreview` (login `demo@example.com` / `demo12345`).

## 7. Tests I ran (MariaDB 10.11 + inferred schema + migration script)

- 39/39 checks in `TestStudentPortal` passed: account creation/activation, login failures,
  hashing, validation, register, duplicate, past event, full/last-seat race, privacy of
  "My Registrations", cancel-another-student's-registration blocked (service and SQL),
  seat restore, attendance rule, dashboard counts, profile edit, cleanup.
- Admin-style `cancelRegistration(id)` still works. Admin dashboard still opens.
- All screens rendered and compared visually with the admin portal; sidebar navigation
  smoke-tested on real database data.

**Not tested:** MySQL 8 + Connector/J 9.5.0 (I used MariaDB + its driver, as Maven Central
was unreachable) and your real schema.
