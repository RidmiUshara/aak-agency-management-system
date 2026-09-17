

# AAK Agency Management System

AAK Agency Management System is a Spring Boot web application developed to manage the day-to-day operations of a CBL product distribution agency. It brings customer, product, purchase, sales, stock, payment, cheque and reporting activities into one system.

## System Preview

<img width="2048" height="1280" alt="dashboard" src="https://github.com/user-attachments/assets/876b6088-e266-401b-91e1-7bf068db9d67" />

*AAK Agency dashboard showing business, financial and stock summaries with quick access to the main management sections.*

## Main Features

- Secure user login and password management
- Management dashboard with business summaries
- Customer and customer credit management
- Product management with CBL/SKU codes, weights, units and prices
- Purchase invoice management
- Original CBL invoice image/PDF upload and verification view
- Sales invoice management for cash and credit sales
- Automatic inventory updates from completed purchase and sales invoices
- Stock movement history and manual stock adjustments
- Payment recording for cash, cheque and bank-transfer collections
- Outstanding-credit and overdue-invoice tracking
- Cheque status management
- Daily, weekly and monthly collection reporting
- Purchase, sales, payment and outstanding-credit reports
- Printable invoice and payment receipt views

## Technology Stack

- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA / Hibernate
- Spring Security
- Thymeleaf
- MySQL
- Maven Wrapper
- HTML, CSS and JavaScript

## Requirements

Install the following software before running the project:

- Java Development Kit (JDK) 17
- MySQL Server 8 or later
- Git
- IntelliJ IDEA or another Java IDE (optional)

Maven does not need to be installed separately because the repository includes the Maven Wrapper.

## Clone the Repository

```bash
git clone https://github.com/RidmiUshara/aak-agency-management-system.git
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

The system is under active development. Current features should be reviewed and tested before production deployment.

## Author

Developed by **Ridmi Ushara** for AAK Agency.
