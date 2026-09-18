

# AAK Agency Management System

AAK Agency Management System is a Spring Boot web application built to run the daily operations of a
CBL product distribution agency: one warehouse, several delivery lorries, sales representatives,
drivers/helpers, hundreds of shops, and a bi-weekly stock intake from the mother company (CBL).
It brings customer, product, purchase, sales, stock, payment, cheque, employee, vehicle and reporting
activity into one system with role-based access for the Owner, Office staff, Sales Reps and Drivers.

This README is the operational reference for the system - what exists, who can access what, how to run
it, and what's still on the roadmap. Keep it up to date as the system grows.

## System Preview

<img width="2048" height="1280" alt="dashboard" src="https://github.com/user-attachments/assets/876b6088-e266-401b-91e1-7bf068db9d67" />

*AAK Agency dashboard showing business, financial and stock summaries with quick access to the main management sections.*

## Roles & Access

Every page is protected by Spring Security with method-level `@PreAuthorize` on destructive actions.
Roles map directly to how the agency actually operates:

| Role (DB value) | Who | Access |
|---|---|---|
| `ADMIN` | Owner / Distributor | Full access to every module, only role that can delete records, and the only role that can create/manage other users' logins (`/users`) |
| `OFFICE` | Office staff | Customers, sales invoices, products, purchase invoices, inventory, payments, cheques, collections, employees, vehicles - everything except user management and deletes |
| `SALES_REP` | Sales representatives | Assigned shops (customers) and bills (sales invoices) only - no purchasing, payments or inventory access |
| `DRIVER` | Drivers / cash collectors | Reserved role, no module access yet - office enters delivery/collection records on their behalf for now (matches the proposal's "optional later access") |

Logins are created and managed by the Owner at **Manage Users** (`/users`) - there is no self-service
sign-up. New staff (office, reps, drivers) must be added there before they can log in.

## Main Features

- Secure login with BCrypt password hashing, CSRF protection and a 5-attempt / 15-minute account lockout
- Role-based access control (Owner / Office / Sales Rep / Driver) enforced at both the URL and method level
- User management screen for the Owner to create and manage staff logins
- Management dashboard with business, financial and stock summaries
- Customer (shop) management with credit tracking, and a printable/scannable QR code per shop that
  opens the shop's record directly when scanned
- Employee records for drivers, helpers, sales reps and office staff
- Vehicle registry with driver assignment
- Delivery routes, and delivery trip planning (assign completed bills to a date/route/vehicle, with a
  computed product loading summary and per-bill delivery status tracking)
- Product management with CBL/SKU codes, weights, units and prices
- Purchase invoice management with original CBL invoice image/PDF upload and verification view
- Sales invoice management for cash and credit sales
- Automatic inventory updates from completed purchase and sales invoices
- Stock movement history and manual stock adjustments
- Payment recording for cash, cheque and bank-transfer collections
- Outstanding-credit and overdue-invoice tracking, cheque status management
- Daily, weekly and monthly collection reporting
- Purchase, sales, payment and outstanding-credit reports
- Printable invoice and payment receipt views
- Dark mode, and an app-like bottom navigation bar on mobile
- Server-side input validation (Bean Validation) with inline form error messages
- Global exception handling - database conflicts and business errors redirect with a friendly message
  instead of a raw error page
- Structured logging with a per-request correlation ID (`X-Request-Id`)
- Health/build-info endpoints (`/actuator/health`, `/actuator/info`) for container orchestration

## Project Roadmap

The system is being built in the same stages as the original business proposal. Status as of the last
update:

| Stage | Deliverable | Status |
|---|---|---|
| 1. Foundation | Login, roles, shops, QR identification, products, employees, vehicles | Done |
| 2. Inventory | Supplier receipts, stock movements/adjustments | Mostly done - no dedicated "opening stock" entry screen yet |
| 3. Distribution | Bills, vehicle loading, vehicle stock, delivery tracking (completed/partial/unsuccessful) | Partial - routes, delivery trip planning, bill assignment and a computed loading summary are done; actual warehouse-to-vehicle stock transfer is not wired up yet |
| 4. Financial control | Credit blocking rules, daily cash/cheque handover reconciliation, CBL supplier balance | Partial - payments/cheques/collections exist, no formal handover reconciliation report or supplier balance view |
| 5. Completion | Shop and CBL returns, employee attendance/advances/salary, final reports | Not started |

When picking up new work, check this table first so effort lines up with the agreed stage order.

## Technology Stack

- Java 17
- Spring Boot 4, Spring MVC, Spring Data JPA / Hibernate, Spring Security
- Thymeleaf, HTML, CSS and JavaScript (no frontend framework/build step)
- MySQL 8
- Maven Wrapper
- Docker (multi-stage build) for packaging and deployment
- ZXing for QR code generation

## Requirements

Install the following software before running the project:

- Java Development Kit (JDK) 17
- MySQL Server 8 or later
- Git
- Docker (recommended - see "Running with Docker" below for the easiest path)
- IntelliJ IDEA or another Java IDE (optional)

Maven does not need to be installed separately because the repository includes the Maven Wrapper.

## Clone the Repository

```bash
git clone https://github.com/lakal96/aak-agency-management-system.git
cd aak-agency-management-system
```

## Database Setup

Create an empty MySQL database:

```sql
CREATE DATABASE aak_agency_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

The application uses Hibernate schema update mode, so the required tables are created or updated when the application starts.

## Environment Variables

The project does not store database or administrator passwords in the repository.

### macOS or Linux

```bash
export DB_URL="jdbc:mysql://localhost:3306/aak_agency_db?useSSL=false&serverTimezone=Asia/Colombo"
export DB_USERNAME="root"
export DB_PASSWORD="YOUR_MYSQL_PASSWORD"
export APP_ADMIN_USERNAME="admin"
export APP_ADMIN_PASSWORD="CREATE_A_STRONG_ADMIN_PASSWORD"
export APP_ADMIN_FULL_NAME="AAK Agency Administrator"
```

### Windows PowerShell

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/aak_agency_db?useSSL=false&serverTimezone=Asia/Colombo"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"
$env:APP_ADMIN_USERNAME="admin"
$env:APP_ADMIN_PASSWORD="CREATE_A_STRONG_ADMIN_PASSWORD"
$env:APP_ADMIN_FULL_NAME="AAK Agency Administrator"
```

`APP_ADMIN_PASSWORD` must contain at least eight characters. The initial administrator account is created only when the database does not already contain that username.

## Running with Docker (recommended)

The repository includes a multi-stage `Dockerfile`. To run the full stack (app + MySQL + nginx) locally
without installing Java or MySQL, use the companion **[aak-agency-ops](https://github.com/lakal96/aak-agency-ops)**
repository instead - it owns the `docker-compose.yml`, nginx config and `.env` template, kept
deliberately separate from this app repo so build concerns and deploy concerns don't mix. See that
repo's README for the exact steps (`cp .env.example .env`, fill in values, `./deploy.sh`).

To just build and smoke-test the image from this repo directly:

```bash
docker build -t aak-agency:local .
```

## Build the Project

### macOS or Linux

```bash
./mvnw clean package -DskipTests
```

### Windows

```powershell
.\mvnw.cmd clean package -DskipTests
```

## Run the Application

### macOS or Linux

```bash
./mvnw spring-boot:run
```

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

Open the application in a browser:

```text
http://localhost:8080
```

Sign in using the administrator username and password supplied through the environment variables.

## Uploaded Invoice Files

Original CBL invoice images and PDFs are stored locally in:

```text
uploads/purchase-invoices/
```

Supported formats:

- JPG / JPEG
- PNG
- PDF

The maximum individual file size is 10 MB. The `uploads/` directory is intentionally excluded from Git because uploaded invoices may contain confidential business information.

## Testing

Run the automated test suite (JUnit 5 + Mockito + Spring Security Test, backed by an in-memory H2
database so no MySQL is needed for tests):

```bash
./mvnw test
```

If you don't have a JDK installed locally, run it inside a Maven container instead:

```bash
docker run --rm -v "$PWD":/app -v maven-repo-cache:/root/.m2 -w /app maven:3.9-eclipse-temurin-17 mvn -B test
```

Current coverage focuses on the areas most likely to regress silently: role-based access control per
module, form validation, user management, and core service logic (customer/employee code generation).
Add a test alongside any new controller/service change, especially anything touching security rules.

## Deployment

This repo owns the build: a GitHub Actions workflow (`.github/workflows/build-and-push.yml`) builds the
Docker image on every push to `main` and publishes it to GHCR. A second workflow
(`.github/workflows/deploy-dev.yml`) builds and pushes to Amazon ECR, then deploys to a dev EC2 instance
via AWS Systems Manager (no SSH keys involved).

Deployment *configuration* (docker-compose, nginx, environment values) lives in the separate
**[aak-agency-ops](https://github.com/lakal96/aak-agency-ops)** repository, not here - keeping
"how the app builds" and "where/how it runs" independent so developers never need production secrets
and ops changes never touch application code.

## Security Notes

- Never commit database passwords, administrator passwords or access tokens.
- Never commit original supplier invoices or customer documents.
- Keep the `.env`, database backup and local configuration files outside Git.
- Change passwords immediately if a credential is accidentally exposed.
- Use strong passwords when creating administrator accounts.

## Important Repository Notes

The repository contains the application source code only. It does not include:

- MySQL business data
- Database backup files
- Uploaded CBL invoice images or PDFs
- Local environment variables
- Build output from the `target/` directory

## Project Status

The system is under active development, following the roadmap in "Project Roadmap" above. Current
features should be reviewed and tested before relying on them for real business data.

## Development Practices

- Every change is committed as its own focused, logically-scoped commit (see git history) rather than
  large mixed commits - makes review and rollback straightforward.
- New features are verified locally (build + automated tests + manual check via Docker) before being
  pushed, and pushed changes are verified again against the live deployment.
- Security is treated as a first-class requirement, not an afterthought: RBAC on every module, BCrypt
  password hashing, CSRF protection, login lockout, server-side validation, parameterized queries only,
  no secrets committed to the repository.
- Records that affect stock or money are corrected, not silently deleted, in line with the business
  requirement for a traceable audit trail.

## Author

Developed by **Ridmi Ushara** for AAK Agency.
