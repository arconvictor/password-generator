# 🔐 Password Generator — DBA Password Rotation Tool

A Java command-line tool that lets a database administrator securely rotate the passwords of technical/service database users — the accounts applications use to connect to an Oracle database — and keep those credentials in sync with [HashiCorp Vault](https://www.vaultproject.io/).

> 🧪 This is a portfolio version of a tool I originally built to solve a real need: keeping technical DB users' passwords up to date without manual, error-prone steps. Internal environment-specific values have been replaced with generic placeholders.

---

## 🚀 What it does

Given a list of database usernames (technical users, not people), the tool:

1. Generates a new, validated random password for each user.
2. Writes the new password to Vault.
3. If — and only if — the Vault write succeeds, updates the password for that same user in the Oracle database (`ALTER USER ... IDENTIFIED BY ...`).
4. Logs a summary: how many identifiers were received, how many were updated successfully, and the details of any failures.

Each user is updated independently: **Vault and the database are kept in sync per user**, so one user's failure doesn't roll back or block the others. Every outcome is captured explicitly as a success or a failure (using a small `Try`/`Success`/`Failure` pattern) rather than relying on exceptions bubbling up silently.

---

## 🛠️ Tech stack

- Java 17
- [Picocli](https://picocli.info/) — command-line argument parsing
- Oracle JDBC — direct database access
- [Vault Java Driver](https://github.com/jopenlibs/vault-java-driver) — HashiCorp Vault integration
- Log4j2 — logging
- JUnit 5 + AssertJ — unit tests
- Docker (Oracle XE + Vault dev server) — integration tests

---

## 📦 Build

```shell
./mvnw clean package
```

This produces a self-contained "fat jar" at `target/password-generator-cli-1.0-SNAPSHOT.jar`.

---

## ▶️ Usage

The database connection is always provided in the form:

```
user@host:port:sid
```

### Option 1 — pass usernames directly

```shell
java -jar target/password-generator-cli-1.0-SNAPSHOT.jar \
  -db DBA_USER@host.example.com:1521:DBSID \
  -vault https://vault.example.com \
  -s db/data/ \
  -u user1,user2 \
  -p -t
```

### Option 2 — read usernames from a file

```shell
java -jar target/password-generator-cli-1.0-SNAPSHOT.jar \
  -db DBA_USER@host.example.com:1521:DBSID \
  -vault https://vault.example.com \
  -s db/data/ \
  -f /path/to/usernames.txt \
  -p -t
```

`-p` (DB password) and `-t` (Vault token) are prompted interactively — they're never passed as plain arguments, so they don't end up in your shell history or process list.

### Options reference

| Flag | Description |
|---|---|
| `-db, --database` | **Required.** DBA connection string: `user@host:port:sid` |
| `-u, --usernames` | Comma-separated list of usernames to update |
| `-f, --file` | Path to a text file with one username per line (alternative to `-u`) |
| `-vault, --vault-url` | Vault server URL (default: `https://vault.example.com`) |
| `-s, --secret-prefix` | Prefix for the Vault secret path (default: `db/data/`) |
| `-p, --password` | DBA's database password (prompted, hidden) |
| `-t, --token` | Vault access token (prompted, hidden) |

---

## 🧪 Testing

Unit tests run with the normal Maven lifecycle:

```shell
./mvnw test
```

Integration tests spin up a real Oracle XE instance and a Vault dev server via Docker:

```shell
./start-test-db
```

or, using the provided Compose file:

```shell
docker compose -f docker-compose-test.yml up -d
```

---

## 🏗️ Project structure

```
de.victorarcon
├── Main                          → CLI entry point
└── pwdgenerator
    ├── boundary                  → CLI parsing, orchestration, I/O (reading files, logging)
    ├── control                   → core logic: password generation/validation, Vault client, DB+Vault transaction
    └── entity                    → Oracle data source / user lookups
```

This follows a light **Boundary–Control–Entity** split: `boundary` talks to the outside world (CLI, files, logs), `control` holds the business rules, and `entity` wraps the raw data access.
