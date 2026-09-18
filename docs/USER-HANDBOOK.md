---
title: "AAK Agency Management System — User Handbook"
author: "AAK Agency"
date: "2026"
---

# AAK Agency Management System — User Handbook

This handbook is a plain-language guide to using the system day to day. It is written for
the people who will actually use it: the Owner, office staff, and sales representatives.
It does not assume any technical background.

## 1. What this system does

AAK Agency Management System is the day-to-day tool for running a CBL product distribution
agency: one warehouse, several delivery vehicles, sales representatives, and shops (customers)
who buy on cash or credit. It replaces manual notebooks and spreadsheets for:

- Keeping a list of shops and how much credit each one has
- Recording products, purchases from CBL, and warehouse stock
- Billing shops (sales invoices) and recording payments/cheques
- Planning delivery routes and vehicle loading
- Reporting on sales, collections, and outstanding credit

## 2. Logging in

1. Open the system's web address in a browser (ask your administrator for the link).
2. Enter your **username** and **password** and click **Login**.
3. If you type the wrong password 5 times in a row, your account is locked for 15 minutes
   as a security measure. Wait and try again, or ask the Owner to reset it.
4. After logging in, use **Change Password** (top-right menu) to set your own password the
   first time you log in.

You cannot create your own account — only the Owner can add new staff logins (see
Section 4, Manage Users).

## 3. Who can do what (Roles)

| Role | Typical person | What they can access |
|---|---|---|
| **Owner (ADMIN)** | Business owner / distributor | Everything - every module, deleting records, and creating/managing staff logins |
| **Office** | Office staff | Customers, sales invoices, products, purchase invoices, inventory, payments, cheques, collections, employees, vehicles, routes, delivery trips - everything except managing user logins and deleting records |
| **Sales Rep** | Sales representative | Only their assigned shops and the bills for those shops |
| **Driver** | Driver / collector | No direct system access yet - office staff enter delivery and collection records on the driver's behalf |

If a page shows "Access Denied" or doesn't appear in your menu, it's because your role
doesn't include it - this is intentional, not an error.

## 4. Manage Users (Owner only)

**Menu: Manage Users**

- **Add a new staff member**: click **+ Add User**, fill in their name, a username, a
  starting password, and pick their role (Office / Sales Rep). Give them the username and
  password to log in with, and ask them to change the password after their first login.
- **Deactivate a user** who has left: edit their account and set status to Inactive, rather
  than deleting - this keeps their history (past invoices, adjustments) intact.

## 5. Customers (Shops)

**Menu: Customers**

Each shop you sell to is a "Customer" record.

- **Add a shop**: click **+ Add Customer**, enter the shop name, area, contact details,
  credit limit and payment terms (days), and save.
- **Credit tracking**: the customer list shows each shop's current outstanding balance
  against its credit limit, so you can see at a glance who is near their limit.
- **QR code**: every shop automatically gets a printable QR code (see **Credit History**
  page → **Print QR Code**). Stick this at the shop; scanning it with a phone camera opens
  that shop's credit history page directly, which is useful for quick on-the-spot checks
  during a delivery or collection visit.
- **Credit History**: click a shop's **Credit History** to see every invoice, payment and
  the running balance for that shop.

## 6. Products

**Menu: Products**

- Each product has a CBL/SKU code, name, brand, category, weight/unit, and both an MRP and
  the agency's standard selling price.
- Set a **Reorder Level** per product - this is the quantity at which the Inventory page will
  flag the product as low stock.
- Mark a product **Inactive** instead of deleting it if CBL stops supplying it - this keeps
  historical invoices and stock history intact.

## 7. Purchase Invoices (Goods received from CBL)

**Menu: Purchase Invoices**

- Record what was received from CBL: invoice number, date, and each product/quantity/cost.
- You can upload a photo or PDF of the original CBL invoice for reference and verification.
- Completing a purchase invoice automatically adds the received quantities to warehouse
  stock - you do not need to also do a separate stock adjustment for it.

### 7.1 Supplier Balance (what AAK owes CBL)

**Menu: Supplier Balance**

Shows every completed purchase invoice that hasn't been fully paid to CBL yet, with a running
total of what's currently owed. Use the small form next to each invoice to record a payment
made to CBL (cash, cheque or bank transfer) - this reduces that invoice's outstanding balance
and the overall total owed.

## 8. Inventory

**Menu: Inventory**

This page shows the current stock level of every product, plus:

- **Low stock** and **out of stock** indicators, based on each product's Reorder Level.
- **Stock Movement History**, a running log of every change to stock and why it happened
  (purchase, sale, adjustment, opening stock, etc.) - useful for investigating discrepancies.

### 8.1 Recording a stock adjustment

Click **+ Record Stock Adjustment** when stock needs correcting outside of a normal purchase
or sale - for example damaged, expired, or missing stock found during a count. Pick the
reason, enter the quantity, and the system automatically works out whether that adds to or
removes from stock.

### 8.2 Setting Opening Stock (new product or first-time count)

When a brand-new product is added to the system, or when doing an initial physical stock
count for the first time, use **+ Set Opening Stock** instead of a normal adjustment. This
shortcut pre-selects "Opening Stock (Initial Balance)" as the reason and always adds to
stock - it's the correct starting point rather than using Purchase Invoices or a generic
adjustment for a first-time balance.

## 9. Sales Invoices (Bills to shops)

**Menu: Sales Invoices**

- Create a bill for a shop: pick the customer, sale type (Cash or Credit), and add each
  product/quantity/price line.
- Click **Complete Invoice & Deduct Stock** once you are sure the bill is final - this is
  the point where warehouse stock is actually reduced. Before completing, you can still
  edit the invoice; after completing, it becomes a permanent record for accounting purposes.
- Cash invoices are automatically marked Paid on completion. Credit invoices stay Outstanding
  until a payment is recorded against them (see Section 10).
- Every invoice has a printable view for handing a physical copy to the shop.
- **Credit limit protection**: if completing a credit bill would push the shop's outstanding
  balance above their approved credit limit, the system blocks it and tells you the shop's
  credit limit, current outstanding, and how much credit is still available - reduce the bill
  or collect an outstanding payment first.

## 10. Payments & Cheques

**Menu: Payments** / **Cheques**

- Record any money received from a shop (cash, cheque, or bank transfer) against their
  outstanding invoices.
- If a sales rep or driver collected the money out in the field (rather than the shop paying
  at the office directly), select them under **Collected By** on the payment form - this is
  what feeds the Daily Handover page (Section 11.1).
- Cheques have their own status tracking (received → banked → cleared/bounced) via the
  **Cheque Dashboard**, so you always know which cheques are still pending clearance.

## 11. Collections

**Menu: Collections**

The Collection Dashboard gives a daily/weekly/monthly view of money collected, useful for
end-of-day reconciliation and for seeing collection trends over time.

### 11.1 Daily Handover Reconciliation

**Menu: Daily Handover**

Pick a date to see every sales rep/driver who collected cash or cheques out in the field that
day, with their totals, so the office can confirm everyone has physically handed in what they
collected. Once confirmed, click **Mark as Handed Over** for that collector - this is a
separate step from just recording the payment, so the office always has a clear "still
pending handover" list at any point in the day.

## 12. Employees & Vehicles

**Menu: Employees** / **Vehicles**

- **Employees**: keep a record of every driver, helper, sales rep and office staff member -
  contact details, join date, and status (active/inactive).
- **Vehicles**: register each delivery vehicle (number, type, capacity notes) and optionally
  assign a regular driver to it.

## 13. Routes & Delivery Trips

**Menu: Routes** / **Delivery Trips**

### 13.1 Routes (optional)

A **Route** is just a named area you deliver to regularly (e.g. "Colombo North"), used to
group trips for reporting. Routes are **optional** - if a vehicle's coverage changes from day
to day and doesn't match any saved route, you don't have to force-fit it into one.

### 13.2 Planning a Delivery Trip

Click **+ Plan New Trip** and fill in:

- **Trip Date** and **Vehicle** (required)
- **Route** - pick a saved route, *or* leave it blank and instead type a short description
  into **Area Covered Today** (e.g. "Kandy town + Peradeniya - no fixed route")
- **Driver** / **Helper** (optional, from your Employees list)

### 13.3 Assigning bills and confirming loading

On a trip's detail page:

1. **Assign a Bill** - pick from the list of completed-but-undelivered bills to load onto
   this trip.
2. The **Loading Summary** automatically totals up how much of each product is needed across
   all bills assigned to the trip.
3. Before the vehicle leaves, click **Confirm Loading**. This lets you adjust each product's
   quantity if what's physically loaded doesn't exactly match the plan (e.g. a shortage), and
   moves the trip to **Loaded** status.
4. As deliveries happen through the day, update each bill's **Delivery Status** to Completed,
   Partial, or Unsuccessful directly from the trip page.
5. At the end of the day, once the vehicle is back, edit the trip and set its status to
   **Closed**.

### 13.4 Vehicle Stock

**Menu: Vehicle Stock** (under Delivery Trips)

This page shows, in real time, what is currently loaded onto each vehicle that is out on a
trip (status = Loaded) - a live answer to "what's on that truck right now?". A trip drops off
this list automatically once it is closed at the end of the day.

## 14. Reports

**Menu: Reports**

The Report Dashboard brings together purchase, sales, payment and outstanding-credit reports
in one place for management review.

## 15. Everyday tips

- **Dark mode**: use the toggle in the top navigation if you prefer a darker screen.
- **On mobile**: the app shows a bottom navigation bar with quick access to the most-used
  pages, and a "More" button for everything else.
- **If something goes wrong**: the system shows a friendly message instead of a raw error
  page (e.g. "This bill is already assigned to a delivery trip") - read the message, it
  usually tells you exactly what to fix.
- **Never share your password.** Each staff member should have their own login so that
  every action in the system can be traced back to the person who did it.

## 16. Getting help

If you're stuck or think something isn't working as it should, note down:

1. What page you were on and what you clicked.
2. The exact message the system showed you, if any.
3. What you expected to happen instead.

Pass this to your system administrator/developer - it makes fixing the issue much faster
than "it doesn't work".
