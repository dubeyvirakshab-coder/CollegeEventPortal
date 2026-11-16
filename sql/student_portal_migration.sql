-- =====================================================================
-- Student Portal - database migration
-- Database: college_event_portal
--
-- Run this ONCE on the real database (MySQL Workbench or the mysql
-- command line) BEFORE starting the Student Portal.
--
-- What it does: adds two EMPTY columns to the existing `participants`
-- table so students can have a password. Nothing else is changed and no
-- existing data is touched. Existing participants simply have NULL
-- passwords until they activate their account in the Student Portal.
--
-- The password itself is never stored. Only a salted PBKDF2 hash is
-- stored, using the same PasswordUtil that the Admin login uses.
-- =====================================================================

USE college_event_portal;

ALTER TABLE participants
    ADD COLUMN password_hash VARCHAR(255) NULL,
    ADD COLUMN password_salt VARCHAR(255) NULL;

-- ---------------------------------------------------------------------
-- RECOMMENDED CHECK (read-only): the email is the student's login, so it
-- must be unique. This query lists any emails that appear twice.
-- It should return NO rows. If it returns rows, fix those participants
-- in the Admin Portal first.
-- ---------------------------------------------------------------------
SELECT email, COUNT(*) AS times_used
FROM participants
GROUP BY email
HAVING COUNT(*) > 1;

-- ---------------------------------------------------------------------
-- OPTIONAL (only if the check above returned no rows AND the email
-- column does not already have a unique key): stops two accounts from
-- ever sharing one email. Remove the two dashes at the start of the
-- line below to run it.
-- ---------------------------------------------------------------------
-- ALTER TABLE participants ADD CONSTRAINT uq_participants_email UNIQUE (email);
