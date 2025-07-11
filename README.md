# 💳 Bank of Spring – Internet Banking System

A full-featured and secure internet banking web application built using **Spring Boot**, **Spring Security**, and **Thymeleaf**. It allows users to manage their accounts, perform transactions, and receive email alerts. Admins can view user accounts, transaction history, and perform deposits or withdrawals on any user's behalf.

---

## 🚀 Features

### 👤 User Features
- Register a new account with auto-generated account number
- Secure login using Spring Security and encrypted passwords (BCrypt)
- View account details and current balance
- Transfer money to other users
- View complete transaction history with timestamps, purpose, and balances
- Receive email alerts for:
  - Account creation
  - Transactions (send/receive)
  - Deposits and withdrawals
  - Password resets/changes

### 🔒 Admin Features
- Admin login with elevated privileges
- View any user's account details by account number
- View all transactions of any user
- Deposit and withdraw funds for any user
- Monitor and assist users securely

---

## 🛠 Tech Stack

| Layer        | Technology Used                  |
|--------------|----------------------------------|
| Backend      | Spring Boot, Spring MVC, Spring Security |
| Frontend     | Thymeleaf, HTML5, CSS3           |
| Database     |  MySQL (optional)|
| Email        | JavaMailSender (SMTP)            |
| Tools        | Maven, Git, VS Code   |

---
## 📽 Demo Video

Watch the full walkthrough of the project functionality in this demo video:

➡️ [Click here to watch](https://drive.google.com/file/d/1WpQKyP6ptzLfOCa0q1Y86MqQtsrLSTOb/view?usp=drive_link)

> The video includes:
> - User registration and login
> - Deposits, withdrawals, and fund transfers
> - Admin operations (view user transactions, deposit/withdraw for users)
> - Email notification previews
> - Secure authentication flow
---

## 📸 Screenshots

> Add screenshots like:
- Login Page
- Account Dashboard
- Transaction Table
- Admin View of Transactions

---

## 📦 Setup Instructions

### 🔧 Requirements
- Java 17+
- Maven
- (Optional) MySQL for persistent storage

### ⚙️ Run Locally

1. **Clone the repo**
   ```bash
   git clone https://github.com/yourusername/bank-of-spring.git
   cd bank-of-spring
   ## 🛠️ How to Run Locally
 2. **Create the MySQL Database**

Login to MySQL and run the following commands:

```sql
-- Create the database
CREATE DATABASE bank;

-- Insert admin account (optional)
INSERT INTO account (account_number, user_name, balance, govt_id_number, opened_on, role, email, mobile_number, gender, password)
VALUES (
  10000001, 
  'Admin', 
  0.0, 
  'XYZ123456', 
  CURDATE(), 
  'ADMIN', 
  'admin@example.com', 
  '9999999999', 
  'Male', 
  '$2a$10$C5o5JQ7As8Z1WZoJhKlMYO6p2KLhoS/SkW/j7BqG9RC6AXU1KkdnS'  -- password: admin123(example password)
);

