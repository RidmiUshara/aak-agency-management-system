-- Baseline schema migration, generated from the existing dev database (mysqldump --no-data).
-- FK checks are disabled during creation so table order doesn't matter.
SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE `customers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` text,
  `area` varchar(255) DEFAULT NULL,
  `assigned_employee` varchar(255) DEFAULT NULL,
  `cbl_outlet_id` varchar(255) DEFAULT NULL,
  `contact_person` varchar(255) DEFAULT NULL,
  `credit_limit` decimal(12,2) DEFAULT NULL,
  `customer_code` varchar(50) NOT NULL,
  `customer_name` varchar(150) NOT NULL,
  `customer_type` varchar(255) DEFAULT NULL,
  `notes` text,
  `payment_terms_days` int DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `qr_code` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKiqv746oh5t5is1vr4p2nl79r6` (`customer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `delivery_trip_load_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `delivery_trip_id` bigint NOT NULL,
  `loaded_quantity` decimal(15,2) NOT NULL,
  `planned_quantity` decimal(15,2) NOT NULL,
  `product_id` bigint NOT NULL,
  `product_name` varchar(255) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `delivery_trips` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `driver_employee_id` bigint DEFAULT NULL,
  `driver_name` varchar(255) DEFAULT NULL,
  `helper_employee_id` bigint DEFAULT NULL,
  `helper_name` varchar(255) DEFAULT NULL,
  `notes` text,
  `route_id` bigint DEFAULT NULL,
  `route_name` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `trip_date` date NOT NULL,
  `vehicle_id` bigint NOT NULL,
  `vehicle_number` varchar(255) DEFAULT NULL,
  `area_covered` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `employee_advances` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `advance_date` date NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `employee_id` bigint NOT NULL,
  `employee_name` varchar(255) DEFAULT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `settled` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `employee_attendance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attendance_date` date NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `employee_id` bigint NOT NULL,
  `employee_name` varchar(255) DEFAULT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `check_in_time` time DEFAULT NULL,
  `check_out_time` time DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendance_employee_date` (`employee_id`,`attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `employee_salary_payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `advance_deduction` decimal(15,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `employee_id` bigint NOT NULL,
  `employee_name` varchar(255) DEFAULT NULL,
  `gross_salary` decimal(15,2) NOT NULL,
  `net_paid` decimal(15,2) NOT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `pay_period_month` varchar(7) NOT NULL,
  `payment_date` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `employees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` text,
  `designation` varchar(255) NOT NULL,
  `employee_code` varchar(255) NOT NULL,
  `full_name` varchar(150) NOT NULL,
  `join_date` date DEFAULT NULL,
  `nic_number` varchar(255) DEFAULT NULL,
  `notes` text,
  `phone` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `system_username` varchar(255) DEFAULT NULL,
  `biometric_device_user_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKetqhw9qqnad1kyjq3ks1glw8x` (`employee_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(15,2) NOT NULL,
  `bank_name` varchar(100) DEFAULT NULL,
  `cheque_cleared_date` date DEFAULT NULL,
  `cheque_date` date DEFAULT NULL,
  `cheque_deposited_date` date DEFAULT NULL,
  `cheque_number` varchar(50) DEFAULT NULL,
  `cheque_return_date` date DEFAULT NULL,
  `cheque_return_reason` varchar(500) DEFAULT NULL,
  `cheque_status` varchar(30) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `payment_date` date NOT NULL,
  `payment_method` varchar(30) NOT NULL,
  `receipt_number` varchar(50) NOT NULL,
  `reference_number` varchar(100) DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `sales_invoice_id` bigint NOT NULL,
  `collected_by_employee_id` bigint DEFAULT NULL,
  `collected_by_name` varchar(255) DEFAULT NULL,
  `handover_date` date DEFAULT NULL,
  `handover_status` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKu6rnuxne864s4rh7qgeql1vx` (`receipt_number`),
  KEY `FKhyreo18yep5hpt2ejsjat9r6o` (`sales_invoice_id`),
  CONSTRAINT `FKhyreo18yep5hpt2ejsjat9r6o` FOREIGN KEY (`sales_invoice_id`) REFERENCES `sales_invoices` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `brand` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `cbl_product_code` varchar(100) NOT NULL,
  `mrp` decimal(12,2) DEFAULT NULL,
  `net_weight` varchar(255) DEFAULT NULL,
  `notes` text,
  `product_name` varchar(200) NOT NULL,
  `reorder_level` decimal(15,2) DEFAULT NULL,
  `standard_selling_price` decimal(12,2) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `unit` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqqjk1ct085jr1tjdgysr1j66v` (`cbl_product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `purchase_invoice_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount_including_vat` decimal(15,2) NOT NULL,
  `purchased_quantity` decimal(12,2) NOT NULL,
  `purchase_unit` varchar(255) NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `product_id` bigint NOT NULL,
  `purchase_invoice_id` bigint NOT NULL,
  `expiry_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnrkxxlg2k74bmf8vo1nf155f` (`product_id`),
  KEY `FK3p8c0w7amab2k7i0nr5shosjb` (`purchase_invoice_id`),
  CONSTRAINT `FK3p8c0w7amab2k7i0nr5shosjb` FOREIGN KEY (`purchase_invoice_id`) REFERENCES `purchase_invoices` (`id`),
  CONSTRAINT `FKnrkxxlg2k74bmf8vo1nf155f` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `purchase_invoices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `delivery_date` date DEFAULT NULL,
  `discount_amount` decimal(15,2) DEFAULT NULL,
  `document_number` varchar(255) NOT NULL,
  `invoice_date` date NOT NULL,
  `invoice_file_content_type` varchar(100) DEFAULT NULL,
  `invoice_file_original_name` varchar(255) DEFAULT NULL,
  `invoice_file_size` bigint DEFAULT NULL,
  `invoice_file_stored_name` varchar(255) DEFAULT NULL,
  `invoice_file_uploaded_at` datetime(6) DEFAULT NULL,
  `invoice_verified` bit(1) NOT NULL,
  `invoice_verified_at` datetime(6) DEFAULT NULL,
  `notes` text,
  `payment_method` varchar(255) DEFAULT NULL,
  `place_of_supply` varchar(255) NOT NULL,
  `po_number` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `subtotal` decimal(15,2) DEFAULT NULL,
  `supplier_name` varchar(255) NOT NULL,
  `tax_invoice_number` varchar(255) DEFAULT NULL,
  `territory` varchar(255) DEFAULT NULL,
  `total_amount` decimal(15,2) DEFAULT NULL,
  `vat_amount` decimal(15,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK2gv6iab8vld7fb1gd6e3yiwco` (`document_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `routes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `area_description` text,
  `route_name` varchar(100) NOT NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKe67qs86hetwbh0qsey22l699s` (`route_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `sales_invoice_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(15,2) NOT NULL,
  `quantity` decimal(15,2) NOT NULL,
  `sales_unit` varchar(255) NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `product_id` bigint NOT NULL,
  `sales_invoice_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmrxthgx63ii0hqmjitpos349j` (`product_id`),
  KEY `FKaa05pdtjhb2kuhdbq48rxxb9a` (`sales_invoice_id`),
  CONSTRAINT `FKaa05pdtjhb2kuhdbq48rxxb9a` FOREIGN KEY (`sales_invoice_id`) REFERENCES `sales_invoices` (`id`),
  CONSTRAINT `FKmrxthgx63ii0hqmjitpos349j` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `sales_invoices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `discount_amount` decimal(15,2) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `gross_amount` decimal(15,2) DEFAULT NULL,
  `invoice_date` date NOT NULL,
  `invoice_number` varchar(255) NOT NULL,
  `net_amount` decimal(15,2) DEFAULT NULL,
  `notes` text,
  `payment_status` varchar(255) DEFAULT NULL,
  `return_amount` decimal(15,2) DEFAULT NULL,
  `route_code` varchar(255) DEFAULT NULL,
  `sale_type` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `delivery_status` varchar(255) DEFAULT NULL,
  `delivery_trip_id` bigint DEFAULT NULL,
  `credit_override_approved_by` varchar(255) DEFAULT NULL,
  `credit_override_at` datetime(6) DEFAULT NULL,
  `credit_override_reason` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKgn9ujwddfu20nghov7s7qubal` (`invoice_number`),
  KEY `FK6boqj01yqbp2q3q30w7lh1ijf` (`customer_id`),
  CONSTRAINT `FK6boqj01yqbp2q3q30w7lh1ijf` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `shop_return_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(15,2) DEFAULT NULL,
  `quantity` decimal(15,2) NOT NULL,
  `shop_return_id` bigint NOT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `unit_price` decimal(15,2) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `category` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh59eqyaf0q1uval9b1n6h3b8l` (`product_id`),
  CONSTRAINT `FKh59eqyaf0q1uval9b1n6h3b8l` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `shop_returns` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `customer_id` bigint NOT NULL,
  `customer_name` varchar(255) DEFAULT NULL,
  `notes` text,
  `reason` varchar(500) DEFAULT NULL,
  `return_date` date NOT NULL,
  `sales_invoice_id` bigint DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `stock_adjustments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `adjustment_date` datetime(6) NOT NULL,
  `adjustment_type` varchar(40) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(100) DEFAULT NULL,
  `direction` varchar(10) NOT NULL,
  `notes` text,
  `quantity` decimal(15,2) NOT NULL,
  `reference_number` varchar(100) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh0emjv0ifyv4bpghf1pfcaue4` (`product_id`),
  CONSTRAINT `FKh0emjv0ifyv4bpghf1pfcaue4` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `stock_movements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `movement_date` datetime(6) NOT NULL,
  `movement_type` varchar(255) NOT NULL,
  `notes` text,
  `quantity_change` decimal(15,2) NOT NULL,
  `reference_item_id` bigint NOT NULL,
  `reference_number` varchar(255) DEFAULT NULL,
  `reference_type` varchar(255) NOT NULL,
  `stock_unit` varchar(255) NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_movement_source` (`reference_type`,`reference_item_id`),
  KEY `FKjcaag8ogfjxpwmqypi1wfdaog` (`product_id`),
  CONSTRAINT `FKjcaag8ogfjxpwmqypi1wfdaog` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `supplier_payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(15,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `payment_date` date NOT NULL,
  `payment_method` varchar(30) NOT NULL,
  `purchase_invoice_document_number` varchar(255) DEFAULT NULL,
  `purchase_invoice_id` bigint NOT NULL,
  `reference_number` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `supplier_return_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(15,2) DEFAULT NULL,
  `quantity` decimal(15,2) NOT NULL,
  `supplier_return_id` bigint NOT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `unit_price` decimal(15,2) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmo5n682iqwaajftm0s8r8ovtq` (`product_id`),
  CONSTRAINT `FKmo5n682iqwaajftm0s8r8ovtq` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `supplier_returns` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `notes` text,
  `purchase_invoice_document_number` varchar(255) DEFAULT NULL,
  `purchase_invoice_id` bigint DEFAULT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `reference_number` varchar(100) DEFAULT NULL,
  `return_date` date NOT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `system_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enabled` bit(1) NOT NULL,
  `full_name` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL,
  `username` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKtr0kj1o2dqfwm13a6fvwrg867` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `vehicles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assigned_driver_id` bigint DEFAULT NULL,
  `assigned_driver_name` varchar(255) DEFAULT NULL,
  `capacity_notes` varchar(255) DEFAULT NULL,
  `notes` text,
  `status` varchar(255) NOT NULL,
  `vehicle_number` varchar(20) NOT NULL,
  `vehicle_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpy3agdr9g5molxe2mj30p9i8i` (`vehicle_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS=1;
