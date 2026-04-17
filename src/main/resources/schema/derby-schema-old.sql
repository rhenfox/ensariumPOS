CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.language.sequence.preallocator', '1');

CREATE TABLE users (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  full_name VARCHAR(120),
  photo BLOB(16777216),
  active SMALLINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_users_active CHECK (active IN (0,1))
);

CREATE TABLE roles (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(60) NOT NULL UNIQUE,
  description VARCHAR(200),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code VARCHAR(80) NOT NULL UNIQUE,
  description VARCHAR(200)
);

CREATE TABLE user_roles (
  user_id INT NOT NULL,
  role_id INT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE role_permissions (
  role_id INT NOT NULL,
  permission_id INT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_perms_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  CONSTRAINT fk_role_perms_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE audit_log (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actor_user_id INT,
  action_code VARCHAR(30) NOT NULL,
  details VARCHAR(1000),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE user_ui_settings (
  user_id INT PRIMARY KEY,
  font_family VARCHAR(120),
  font_size INT,
  dark_mode SMALLINT NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ui_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT ck_ui_dark CHECK (dark_mode IN (0,1))
);

CREATE TABLE user_hotkey (
  user_id INT NOT NULL,
  action_code VARCHAR(50) NOT NULL,
  key_stroke VARCHAR(80) NOT NULL,
  enabled SMALLINT NOT NULL DEFAULT 1,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, action_code),
  CONSTRAINT fk_hk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT ck_hk_enabled CHECK (enabled IN (0,1))
);

CREATE TABLE store (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  address VARCHAR(250),
  active SMALLINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_store_active CHECK (active IN (0,1))
);

CREATE TABLE terminal (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  store_id INT NOT NULL,
  code VARCHAR(30) NOT NULL,
  name VARCHAR(120),
  active SMALLINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_terminal_store FOREIGN KEY (store_id) REFERENCES store(id),
  CONSTRAINT uq_terminal_store_code UNIQUE (store_id, code),
  CONSTRAINT ck_terminal_active CHECK (active IN (0,1))
);

CREATE TABLE pos_shift (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  store_id INT NOT NULL,
  terminal_id INT NOT NULL,
  opened_by INT NOT NULL,
  opened_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  closed_by INT,
  closed_at TIMESTAMP,
  opening_cash DECIMAL(19,4) NOT NULL DEFAULT 0,
  closing_cash DECIMAL(19,4),
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  remarks VARCHAR(300),
  CONSTRAINT fk_shift_store FOREIGN KEY (store_id) REFERENCES store(id),
  CONSTRAINT fk_shift_terminal FOREIGN KEY (terminal_id) REFERENCES terminal(id),
  CONSTRAINT fk_shift_opened_by FOREIGN KEY (opened_by) REFERENCES users(id),
  CONSTRAINT fk_shift_closed_by FOREIGN KEY (closed_by) REFERENCES users(id),
  CONSTRAINT ck_shift_status CHECK (status IN ('OPEN','CLOSED'))
);

CREATE INDEX ix_shift_store_status ON pos_shift(store_id, status);
CREATE INDEX ix_shift_terminal ON pos_shift(terminal_id);

CREATE TABLE taxpayer_profile (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  registered_name VARCHAR(160) NOT NULL,
  trade_name VARCHAR(160),
  tin_no VARCHAR(20) NOT NULL UNIQUE,
  head_office_address VARCHAR(250) NOT NULL,
  vat_registration_type VARCHAR(10) NOT NULL DEFAULT 'VAT',
  active SMALLINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_tp_vat_type CHECK (vat_registration_type IN ('VAT','NONVAT')),
  CONSTRAINT ck_tp_active CHECK (active IN (0,1))
);

CREATE TABLE store_fiscal_profile (
  store_id INT PRIMARY KEY,
  taxpayer_profile_id INT NOT NULL,
  branch_code VARCHAR(5) NOT NULL,
  registered_business_address VARCHAR(250) NOT NULL,
  pos_vendor_name VARCHAR(160),
  pos_vendor_tin_no VARCHAR(20),
  pos_vendor_address VARCHAR(250),
  supplier_accreditation_no VARCHAR(60),
  accreditation_issued_at DATE,
  accreditation_valid_until DATE,
  bir_permit_to_use_no VARCHAR(60),
  permit_to_use_issued_at DATE,
  atp_no VARCHAR(60),
  atp_issued_at DATE,
  active SMALLINT NOT NULL DEFAULT 1,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sfp_store FOREIGN KEY (store_id) REFERENCES store(id),
  CONSTRAINT fk_sfp_taxpayer FOREIGN KEY (taxpayer_profile_id) REFERENCES taxpayer_profile(id),
  CONSTRAINT ck_sfp_active CHECK (active IN (0,1))
);

CREATE TABLE terminal_fiscal_series (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  terminal_id INT NOT NULL,
  doc_type VARCHAR(20) NOT NULL DEFAULT 'INVOICE',
  prefix VARCHAR(20),
  serial_from BIGINT NOT NULL,
  serial_to BIGINT NOT NULL,
  next_serial BIGINT NOT NULL,
  active SMALLINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tfs_terminal FOREIGN KEY (terminal_id) REFERENCES terminal(id),
  CONSTRAINT uq_tfs UNIQUE (terminal_id, doc_type, serial_from, serial_to),
  CONSTRAINT ck_tfs_doc CHECK (doc_type IN ('INVOICE','SUPP_RECEIPT')),
  CONSTRAINT ck_tfs_active CHECK (active IN (0,1)),
  CONSTRAINT ck_tfs_range CHECK (
    serial_from > 0 AND
    serial_to >= serial_from AND
    next_serial >= serial_from AND
    next_serial <= serial_to + 1
  )
);

CREATE TABLE customer (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_no VARCHAR(40) UNIQUE,
  full_name VARCHAR(160) NOT NULL,
  tin_no VARCHAR(20),
  phone VARCHAR(60),
  email VARCHAR(120),
  address VARCHAR(250),
  is_senior SMALLINT NOT NULL DEFAULT 0,
  senior_id_no VARCHAR(60),
  is_vat_exempt SMALLINT NOT NULL DEFAULT 0,
  active SMALLINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_customer_flags CHECK (
    is_senior IN (0,1) AND
    is_vat_exempt IN (0,1) AND
    active IN (0,1)
  )
);

CREATE INDEX ix_customer_name ON customer(full_name);

CREATE TABLE customer_benefit_profile (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  benefit_type VARCHAR(20) NOT NULL,
  gov_id_no VARCHAR(60) NOT NULL,
  tin_no VARCHAR(20),
  active SMALLINT NOT NULL DEFAULT 1,
  effective_from DATE,
  effective_to DATE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_cbp_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
  CONSTRAINT uq_cbp UNIQUE (customer_id, benefit_type, gov_id_no),
  CONSTRAINT ck_cbp_type CHECK (benefit_type IN ('SENIOR','PWD')),
  CONSTRAINT ck_cbp_active CHECK (active IN (0,1))
);

CREATE TABLE supplier (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  supplier_no VARCHAR(40) UNIQUE,
  name VARCHAR(160) NOT NULL,
  phone VARCHAR(60),
  email VARCHAR(120),
  address VARCHAR(250),
  active SMALLINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_supplier_active CHECK (active IN (0,1))
);

CREATE TABLE uom (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(60) NOT NULL
);

CREATE TABLE product_category (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE,
  parent_id INT,
  CONSTRAINT fk_cat_parent FOREIGN KEY (parent_id) REFERENCES product_category(id)
);

CREATE TABLE tax (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  rate DECIMAL(9,6) NOT NULL,
  active SMALLINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_tax_rate CHECK (rate >= 0),
  CONSTRAINT ck_tax_active CHECK (active IN (0,1))
);

CREATE TABLE product (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sku VARCHAR(60) NOT NULL UNIQUE,
  name VARCHAR(200) NOT NULL,
  category_id INT,
  base_uom_id INT NOT NULL,
  buying_price DECIMAL(19,4) NOT NULL DEFAULT 0,
  selling_price DECIMAL(19,4) NOT NULL DEFAULT 0,
  price_includes_tax SMALLINT NOT NULL DEFAULT 1,
  default_tax_id INT,
  track_expiry SMALLINT NOT NULL DEFAULT 0,
  active SMALLINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_prod_cat FOREIGN KEY (category_id) REFERENCES product_category(id),
  CONSTRAINT fk_prod_uom FOREIGN KEY (base_uom_id) REFERENCES uom(id),
  CONSTRAINT fk_prod_tax FOREIGN KEY (default_tax_id) REFERENCES tax(id),
  CONSTRAINT ck_prod_flags CHECK (
    price_includes_tax IN (0,1) AND
    track_expiry IN (0,1) AND
    active IN (0,1)
  ),
  CONSTRAINT ck_prod_prices CHECK (buying_price >= 0 AND selling_price >= 0)
);

CREATE INDEX ix_product_name ON product(name);

CREATE TABLE product_barcode (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id BIGINT NOT NULL,
  barcode VARCHAR(80) NOT NULL UNIQUE,
  is_primary SMALLINT NOT NULL DEFAULT 0,
  active SMALLINT NOT NULL DEFAULT 1,
  CONSTRAINT fk_barcode_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
  CONSTRAINT ck_barcode_flags CHECK (is_primary IN (0,1) AND active IN (0,1))
);

CREATE TABLE discount_type (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  kind VARCHAR(10) NOT NULL,
  applies_to VARCHAR(20) NOT NULL,
  active SMALLINT NOT NULL DEFAULT 1,
  CONSTRAINT ck_discount_kind CHECK (kind IN ('PERCENT','AMOUNT')),
  CONSTRAINT ck_discount_applies CHECK (applies_to IN ('LINE','SUBTOTAL','TOTAL')),
  CONSTRAINT ck_discount_active CHECK (active IN (0,1))
);

CREATE TABLE payment_method (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(80) NOT NULL,
  active SMALLINT NOT NULL DEFAULT 1,
  CONSTRAINT ck_pm_active CHECK (active IN (0,1))
);

CREATE TABLE return_reason (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL
);

CREATE TABLE benefit_policy (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  benefit_type VARCHAR(20) NOT NULL,
  kind VARCHAR(10) NOT NULL DEFAULT 'PERCENT',
  default_rate DECIMAL(9,6) NOT NULL,
  min_rate DECIMAL(9,6) NOT NULL,
  max_rate DECIMAL(9,6) NOT NULL,
  vat_exempt SMALLINT NOT NULL DEFAULT 1,
  allow_manual_override SMALLINT NOT NULL DEFAULT 0,
  legal_basis VARCHAR(120),
  effective_from DATE NOT NULL,
  effective_to DATE,
  active SMALLINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_bp_type CHECK (benefit_type IN ('SENIOR','PWD')),
  CONSTRAINT ck_bp_kind CHECK (kind IN ('PERCENT')),
  CONSTRAINT ck_bp_rates CHECK (
    default_rate >= 0 AND
    min_rate >= 0 AND
    max_rate >= min_rate AND
    default_rate BETWEEN min_rate AND max_rate
  ),
  CONSTRAINT ck_bp_flags CHECK (
    vat_exempt IN (0,1) AND
    allow_manual_override IN (0,1) AND
    active IN (0,1)
  )
);

INSERT INTO benefit_policy
(code, name, benefit_type, kind, default_rate, min_rate, max_rate, vat_exempt, allow_manual_override, legal_basis, effective_from, active)
VALUES
('SC', 'Senior Citizen Benefit', 'SENIOR', 'PERCENT', 0.200000, 0.200000, 0.200000, 1, 0, 'RA 9994 / RR 16-2018', CURRENT_DATE, 1);

INSERT INTO benefit_policy
(code, name, benefit_type, kind, default_rate, min_rate, max_rate, vat_exempt, allow_manual_override, legal_basis, effective_from, active)
VALUES
('PWD', 'PWD Benefit', 'PWD', 'PERCENT', 0.200000, 0.200000, 0.200000, 1, 0, 'RA 10754 / RR 16-2018', CURRENT_DATE, 1);

CREATE TABLE inventory_status (
  code VARCHAR(20) PRIMARY KEY,
  name VARCHAR(60) NOT NULL,
  sellable SMALLINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_inv_sellable CHECK (sellable IN (0,1))
);

INSERT INTO inventory_status(code, name, sellable) VALUES ('ONHAND','On Hand',1);
INSERT INTO inventory_status(code, name, sellable) VALUES ('DAMAGED','Damaged',0);
INSERT INTO inventory_status(code, name, sellable) VALUES ('RETURNED','Returned',0);
INSERT INTO inventory_status(code, name, sellable) VALUES ('EXPIRED','Expired',0);

CREATE TABLE inventory_lot (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id BIGINT NOT NULL,
  lot_no VARCHAR(80),
  expiry_date DATE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_lot_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE INDEX ix_lot_product_expiry ON inventory_lot(product_id, expiry_date);

CREATE TABLE stock_balance (
  store_id INT NOT NULL,
  product_id BIGINT NOT NULL,
  status_code VARCHAR(20) NOT NULL,
  qty_in_base DECIMAL(18,4) NOT NULL DEFAULT 0,
  PRIMARY KEY (store_id, product_id, status_code),
  CONSTRAINT fk_bal_store FOREIGN KEY (store_id) REFERENCES store(id),
  CONSTRAINT fk_bal_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_bal_status FOREIGN KEY (status_code) REFERENCES inventory_status(code),
  CONSTRAINT ck_bal_qty CHECK (qty_in_base >= 0)
);

CREATE TABLE purchase_receipt (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  store_id INT NOT NULL,
  supplier_id BIGINT,
  receipt_no VARCHAR(60) UNIQUE,
  received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  received_by INT,
  notes VARCHAR(400),
  CONSTRAINT fk_pr_store FOREIGN KEY (store_id) REFERENCES store(id),
  CONSTRAINT fk_pr_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id),
  CONSTRAINT fk_pr_user FOREIGN KEY (received_by) REFERENCES users(id)
);

CREATE TABLE purchase_receipt_line (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  purchase_receipt_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  lot_id BIGINT,
  qty_in_base DECIMAL(18,4) NOT NULL,
  unit_cost DECIMAL(19,4) NOT NULL,
  total_cost DECIMAL(19,4) NOT NULL,
  CONSTRAINT fk_prl_pr FOREIGN KEY (purchase_receipt_id) REFERENCES purchase_receipt(id) ON DELETE CASCADE,
  CONSTRAINT fk_prl_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_prl_lot FOREIGN KEY (lot_id) REFERENCES inventory_lot(id),
  CONSTRAINT ck_prl_qty CHECK (qty_in_base > 0),
  CONSTRAINT ck_prl_cost CHECK (unit_cost >= 0 AND total_cost >= 0)
);

CREATE INDEX ix_prl_pr ON purchase_receipt_line(purchase_receipt_id);

CREATE TABLE sale (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  store_id INT NOT NULL,
  terminal_id INT NOT NULL,
  shift_id BIGINT NOT NULL,
  sale_no VARCHAR(60) UNIQUE,
  sale_type VARCHAR(20) NOT NULL DEFAULT 'RETAIL',
  status VARCHAR(20) NOT NULL DEFAULT 'POSTED',
  customer_id BIGINT,
  cashier_user_id INT,
  sold_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  notes VARCHAR(400),
  CONSTRAINT fk_sale_store FOREIGN KEY (store_id) REFERENCES store(id),
  CONSTRAINT fk_sale_terminal FOREIGN KEY (terminal_id) REFERENCES terminal(id),
  CONSTRAINT fk_sale_shift FOREIGN KEY (shift_id) REFERENCES pos_shift(id),
  CONSTRAINT fk_sale_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  CONSTRAINT fk_sale_cashier FOREIGN KEY (cashier_user_id) REFERENCES users(id),
  CONSTRAINT ck_sale_type CHECK (sale_type IN ('RETAIL','WHOLESALE')),
  CONSTRAINT ck_sale_status CHECK (status IN ('POSTED','VOIDED'))
);

CREATE INDEX ix_sale_store_date ON sale(store_id, sold_at);
CREATE INDEX ix_sale_shift ON sale(shift_id);

CREATE TABLE sale_line (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sale_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  product_id BIGINT NOT NULL,
  lot_id BIGINT,
  qty_in_base DECIMAL(18,4) NOT NULL,
  unit_price DECIMAL(19,4) NOT NULL,
  price_includes_tax SMALLINT NOT NULL DEFAULT 1,
  tax_rate DECIMAL(9,6) NOT NULL DEFAULT 0,
  cost_total DECIMAL(19,4) NOT NULL DEFAULT 0,
  CONSTRAINT fk_sl_sale FOREIGN KEY (sale_id) REFERENCES sale(id) ON DELETE CASCADE,
  CONSTRAINT fk_sl_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_sl_lot FOREIGN KEY (lot_id) REFERENCES inventory_lot(id),
  CONSTRAINT uq_sl UNIQUE (sale_id, line_no),
  CONSTRAINT ck_sl_qty CHECK (qty_in_base > 0),
  CONSTRAINT ck_sl_price CHECK (unit_price >= 0),
  CONSTRAINT ck_sl_flags CHECK (price_includes_tax IN (0,1)),
  CONSTRAINT ck_sl_taxrate CHECK (tax_rate >= 0)
);

CREATE INDEX ix_sl_sale ON sale_line(sale_id);
CREATE INDEX ix_sl_product ON sale_line(product_id);

CREATE TABLE sale_discount (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sale_id BIGINT NOT NULL,
  sale_line_id BIGINT,
  discount_type_id INT,
  discount_name VARCHAR(120),
  kind VARCHAR(10) NOT NULL,
  value DECIMAL(19,6) NOT NULL,
  amount DECIMAL(19,4) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sd_sale FOREIGN KEY (sale_id) REFERENCES sale(id) ON DELETE CASCADE,
  CONSTRAINT fk_sd_line FOREIGN KEY (sale_line_id) REFERENCES sale_line(id) ON DELETE CASCADE,
  CONSTRAINT fk_sd_dtype FOREIGN KEY (discount_type_id) REFERENCES discount_type(id),
  CONSTRAINT ck_sd_kind CHECK (kind IN ('PERCENT','AMOUNT')),
  CONSTRAINT ck_sd_vals CHECK (value >= 0 AND amount >= 0)
);

CREATE INDEX ix_sd_sale ON sale_discount(sale_id);
CREATE INDEX ix_sd_line ON sale_discount(sale_line_id);

CREATE TABLE sale_line_tax (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sale_line_id BIGINT NOT NULL,
  tax_id INT,
  tax_code VARCHAR(30),
  tax_rate DECIMAL(9,6) NOT NULL,
  tax_amount DECIMAL(19,4) NOT NULL,
  CONSTRAINT fk_slt_line FOREIGN KEY (sale_line_id) REFERENCES sale_line(id) ON DELETE CASCADE,
  CONSTRAINT fk_slt_tax FOREIGN KEY (tax_id) REFERENCES tax(id),
  CONSTRAINT ck_slt_rate CHECK (tax_rate >= 0),
  CONSTRAINT ck_slt_amt CHECK (tax_amount >= 0)
);

CREATE INDEX ix_slt_line ON sale_line_tax(sale_line_id);

CREATE TABLE sale_payment (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sale_id BIGINT NOT NULL,
  method_id INT NOT NULL,
  amount DECIMAL(19,4) NOT NULL,
  reference_no VARCHAR(80),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sp_sale FOREIGN KEY (sale_id) REFERENCES sale(id) ON DELETE CASCADE,
  CONSTRAINT fk_sp_method FOREIGN KEY (method_id) REFERENCES payment_method(id),
  CONSTRAINT ck_sp_amt CHECK (amount >= 0)
);

CREATE INDEX ix_sp_sale ON sale_payment(sale_id);

CREATE TABLE sale_invoice (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sale_id BIGINT NOT NULL UNIQUE,
  store_id INT NOT NULL,
  terminal_id INT NOT NULL,
  fiscal_series_id BIGINT NOT NULL,
  invoice_no VARCHAR(60) NOT NULL UNIQUE,
  serial_no BIGINT NOT NULL,
  seller_registered_name VARCHAR(160) NOT NULL,
  seller_trade_name VARCHAR(160),
  seller_tin_no VARCHAR(20) NOT NULL,
  seller_branch_code VARCHAR(5) NOT NULL,
  seller_business_address VARCHAR(250) NOT NULL,
  seller_vat_registration_type VARCHAR(10) NOT NULL,
  buyer_registered_name VARCHAR(160),
  buyer_tin_no VARCHAR(20),
  buyer_business_address VARCHAR(250),
  cash_sales SMALLINT NOT NULL DEFAULT 1,
  charge_sales SMALLINT NOT NULL DEFAULT 0,
  gross_sales DECIMAL(19,4) NOT NULL DEFAULT 0,
  vatable_sales DECIMAL(19,4) NOT NULL DEFAULT 0,
  vat_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
  vat_exempt_sales DECIMAL(19,4) NOT NULL DEFAULT 0,
  zero_rated_sales DECIMAL(19,4) NOT NULL DEFAULT 0,
  discount_total DECIMAL(19,4) NOT NULL DEFAULT 0,
  withholding_tax_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
  total_amount_due DECIMAL(19,4) NOT NULL DEFAULT 0,
  pos_vendor_name VARCHAR(160),
  pos_vendor_tin_no VARCHAR(20),
  pos_vendor_address VARCHAR(250),
  supplier_accreditation_no VARCHAR(60),
  accreditation_issued_at DATE,
  accreditation_valid_until DATE,
  bir_permit_to_use_no VARCHAR(60),
  permit_to_use_issued_at DATE,
  atp_no VARCHAR(60),
  atp_issued_at DATE,
  approved_series_from BIGINT,
  approved_series_to BIGINT,
  is_printed SMALLINT NOT NULL DEFAULT 0,
  printed_at TIMESTAMP,
  reprint_count INT NOT NULL DEFAULT 0,
  voided SMALLINT NOT NULL DEFAULT 0,
  voided_at TIMESTAMP,
  void_reason VARCHAR(250),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_si_sale FOREIGN KEY (sale_id) REFERENCES sale(id) ON DELETE CASCADE,
  CONSTRAINT fk_si_store FOREIGN KEY (store_id) REFERENCES store(id),
  CONSTRAINT fk_si_terminal FOREIGN KEY (terminal_id) REFERENCES terminal(id),
  CONSTRAINT fk_si_series FOREIGN KEY (fiscal_series_id) REFERENCES terminal_fiscal_series(id),
  CONSTRAINT ck_si_vat_type CHECK (seller_vat_registration_type IN ('VAT','NONVAT')),
  CONSTRAINT ck_si_cash_charge CHECK (cash_sales IN (0,1) AND charge_sales IN (0,1)),
  CONSTRAINT ck_si_printed CHECK (is_printed IN (0,1)),
  CONSTRAINT ck_si_voided CHECK (voided IN (0,1)),
  CONSTRAINT ck_si_amounts CHECK (
    gross_sales >= 0 AND
    vatable_sales >= 0 AND
    vat_amount >= 0 AND
    vat_exempt_sales >= 0 AND
    zero_rated_sales >= 0 AND
    discount_total >= 0 AND
    withholding_tax_amount >= 0 AND
    total_amount_due >= 0
  )
);

CREATE INDEX ix_si_store_date ON sale_invoice(store_id, created_at);
CREATE INDEX ix_si_buyer_tin ON sale_invoice(buyer_tin_no);

CREATE TABLE sale_invoice_line (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sale_invoice_id BIGINT NOT NULL,
  sale_line_id BIGINT,
  line_no INT NOT NULL,
  product_id BIGINT,
  item_description VARCHAR(250) NOT NULL,
  qty_in_base DECIMAL(18,4) NOT NULL,
  unit_price DECIMAL(19,4) NOT NULL,
  line_gross_amount DECIMAL(19,4) NOT NULL,
  line_discount_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
  line_vat_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
  line_net_amount DECIMAL(19,4) NOT NULL,
  tax_code VARCHAR(30),
  tax_rate DECIMAL(9,6) NOT NULL DEFAULT 0,
  is_vat_exempt SMALLINT NOT NULL DEFAULT 0,
  is_zero_rated SMALLINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_sil_inv FOREIGN KEY (sale_invoice_id) REFERENCES sale_invoice(id),
  CONSTRAINT fk_sil_sale_line FOREIGN KEY (sale_line_id) REFERENCES sale_line(id),
  CONSTRAINT fk_sil_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT uq_sil_line UNIQUE (sale_invoice_id, line_no),
  CONSTRAINT ck_sil_qty CHECK (qty_in_base > 0),
  CONSTRAINT ck_sil_flags CHECK (is_vat_exempt IN (0,1) AND is_zero_rated IN (0,1)),
  CONSTRAINT ck_sil_money CHECK (
    unit_price >= 0 AND
    line_gross_amount >= 0 AND
    line_discount_amount >= 0 AND
    line_vat_amount >= 0 AND
    line_net_amount >= 0 AND
    tax_rate >= 0
  )
);

CREATE INDEX ix_sil_inv ON sale_invoice_line(sale_invoice_id);
CREATE INDEX ix_sil_sale_line ON sale_invoice_line(sale_line_id);

CREATE TABLE sale_benefit_claim (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sale_invoice_id BIGINT NOT NULL,
  sale_invoice_line_id BIGINT,
  policy_id INT NOT NULL,
  customer_id BIGINT,
  benefit_type VARCHAR(20) NOT NULL,
  beneficiary_name VARCHAR(160),
  beneficiary_tin_no VARCHAR(20),
  gov_id_no VARCHAR(60) NOT NULL,
  signature_name VARCHAR(160),
  gross_eligible_amount DECIMAL(19,4) NOT NULL,
  vat_exempt_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
  applied_rate DECIMAL(9,6) NOT NULL,
  discount_amount DECIMAL(19,4) NOT NULL,
  override_reason VARCHAR(250),
  approved_by INT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sbc_inv FOREIGN KEY (sale_invoice_id) REFERENCES sale_invoice(id),
  CONSTRAINT fk_sbc_line FOREIGN KEY (sale_invoice_line_id) REFERENCES sale_invoice_line(id),
  CONSTRAINT fk_sbc_policy FOREIGN KEY (policy_id) REFERENCES benefit_policy(id),
  CONSTRAINT fk_sbc_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  CONSTRAINT fk_sbc_user FOREIGN KEY (approved_by) REFERENCES users(id),
  CONSTRAINT ck_sbc_type CHECK (benefit_type IN ('SENIOR','PWD')),
  CONSTRAINT ck_sbc_money CHECK (
    gross_eligible_amount >= 0 AND
    vat_exempt_amount >= 0 AND
    applied_rate >= 0 AND
    discount_amount >= 0
  )
);

CREATE INDEX ix_sbc_inv ON sale_benefit_claim(sale_invoice_id);
CREATE INDEX ix_sbc_type ON sale_benefit_claim(benefit_type);

CREATE TABLE sales_return (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  store_id INT NOT NULL,
  terminal_id INT NOT NULL,
  shift_id BIGINT NOT NULL,
  return_no VARCHAR(60) UNIQUE,
  status VARCHAR(20) NOT NULL DEFAULT 'POSTED',
  returned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  returned_by INT,
  sale_id BIGINT,
  customer_id BIGINT,
  notes VARCHAR(400),
  CONSTRAINT fk_sr_store FOREIGN KEY (store_id) REFERENCES store(id),
  CONSTRAINT fk_sr_terminal FOREIGN KEY (terminal_id) REFERENCES terminal(id),
  CONSTRAINT fk_sr_shift FOREIGN KEY (shift_id) REFERENCES pos_shift(id),
  CONSTRAINT fk_sr_user FOREIGN KEY (returned_by) REFERENCES users(id),
  CONSTRAINT fk_sr_sale FOREIGN KEY (sale_id) REFERENCES sale(id),
  CONSTRAINT fk_sr_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  CONSTRAINT ck_sr_status CHECK (status IN ('POSTED','VOIDED'))
);

CREATE TABLE sales_return_line (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sales_return_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  sale_line_id BIGINT,
  product_id BIGINT NOT NULL,
  lot_id BIGINT,
  qty_in_base DECIMAL(18,4) NOT NULL,
  unit_price_refund DECIMAL(19,4) NOT NULL DEFAULT 0,
  tax_refund DECIMAL(19,4) NOT NULL DEFAULT 0,
  discount_refund DECIMAL(19,4) NOT NULL DEFAULT 0,
  reason_id INT,
  restock_to_status VARCHAR(20) NOT NULL DEFAULT 'RETURNED',
  CONSTRAINT fk_srl_sr FOREIGN KEY (sales_return_id) REFERENCES sales_return(id) ON DELETE CASCADE,
  CONSTRAINT fk_srl_sl FOREIGN KEY (sale_line_id) REFERENCES sale_line(id),
  CONSTRAINT fk_srl_prod FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_srl_lot FOREIGN KEY (lot_id) REFERENCES inventory_lot(id),
  CONSTRAINT fk_srl_reason FOREIGN KEY (reason_id) REFERENCES return_reason(id),
  CONSTRAINT fk_srl_status FOREIGN KEY (restock_to_status) REFERENCES inventory_status(code),
  CONSTRAINT uq_srl UNIQUE (sales_return_id, line_no),
  CONSTRAINT ck_srl_qty CHECK (qty_in_base > 0),
  CONSTRAINT ck_srl_money CHECK (
    unit_price_refund >= 0 AND
    tax_refund >= 0 AND
    discount_refund >= 0
  )
);

CREATE INDEX ix_srl_sr ON sales_return_line(sales_return_id);

CREATE TABLE sales_return_refund (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sales_return_id BIGINT NOT NULL,
  method_id INT NOT NULL,
  amount DECIMAL(19,4) NOT NULL,
  reference_no VARCHAR(80),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_srr_sr FOREIGN KEY (sales_return_id) REFERENCES sales_return(id) ON DELETE CASCADE,
  CONSTRAINT fk_srr_method FOREIGN KEY (method_id) REFERENCES payment_method(id),
  CONSTRAINT ck_srr_amt CHECK (amount >= 0)
);

CREATE TABLE inventory_txn (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  store_id INT NOT NULL,
  txn_type VARCHAR(30) NOT NULL,
  ref_no VARCHAR(60),
  sale_id BIGINT,
  sales_return_id BIGINT,
  purchase_receipt_id BIGINT,
  notes VARCHAR(400),
  created_by INT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_inv_store FOREIGN KEY (store_id) REFERENCES store(id),
  CONSTRAINT fk_inv_user FOREIGN KEY (created_by) REFERENCES users(id),
  CONSTRAINT ck_inv_type CHECK (
    txn_type IN ('PURCHASE_RECEIPT','SALE','SALE_RETURN','DAMAGE','EXPIRE','ADJUSTMENT')
  )
);

CREATE INDEX ix_inv_txn_store_date ON inventory_txn(store_id, created_at);

CREATE TABLE inventory_txn_line (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  txn_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  lot_id BIGINT,
  from_status VARCHAR(20),
  to_status VARCHAR(20),
  qty_in_base DECIMAL(18,4) NOT NULL,
  unit_cost DECIMAL(19,4) NOT NULL DEFAULT 0,
  total_cost DECIMAL(19,4) NOT NULL DEFAULT 0,
  CONSTRAINT fk_itl_txn FOREIGN KEY (txn_id) REFERENCES inventory_txn(id) ON DELETE CASCADE,
  CONSTRAINT fk_itl_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_itl_lot FOREIGN KEY (lot_id) REFERENCES inventory_lot(id),
  CONSTRAINT fk_itl_from_status FOREIGN KEY (from_status) REFERENCES inventory_status(code),
  CONSTRAINT fk_itl_to_status FOREIGN KEY (to_status) REFERENCES inventory_status(code),
  CONSTRAINT ck_itl_qty CHECK (qty_in_base > 0),
  CONSTRAINT ck_itl_cost CHECK (unit_cost >= 0 AND total_cost >= 0)
);

CREATE INDEX ix_itl_txn ON inventory_txn_line(txn_id);
CREATE INDEX ix_itl_product ON inventory_txn_line(product_id);

CREATE TABLE pos_reading (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  terminal_id INT NOT NULL,
  shift_id BIGINT,
  business_date DATE NOT NULL,
  reading_type VARCHAR(1) NOT NULL,
  generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sales_count INT NOT NULL DEFAULT 0,
  gross_sales DECIMAL(19,4) NOT NULL DEFAULT 0,
  vatable_sales DECIMAL(19,4) NOT NULL DEFAULT 0,
  vat_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
  vat_exempt_sales DECIMAL(19,4) NOT NULL DEFAULT 0,
  zero_rated_sales DECIMAL(19,4) NOT NULL DEFAULT 0,
  discount_total DECIMAL(19,4) NOT NULL DEFAULT 0,
  refund_total DECIMAL(19,4) NOT NULL DEFAULT 0,
  void_total DECIMAL(19,4) NOT NULL DEFAULT 0,
  net_sales DECIMAL(19,4) NOT NULL DEFAULT 0,
  CONSTRAINT fk_prd_terminal FOREIGN KEY (terminal_id) REFERENCES terminal(id),
  CONSTRAINT fk_prd_shift FOREIGN KEY (shift_id) REFERENCES pos_shift(id),
  CONSTRAINT ck_prd_type CHECK (reading_type IN ('X','Z')),
  CONSTRAINT ck_prd_money CHECK (
    sales_count >= 0 AND
    gross_sales >= 0 AND
    vatable_sales >= 0 AND
    vat_amount >= 0 AND
    vat_exempt_sales >= 0 AND
    zero_rated_sales >= 0 AND
    discount_total >= 0 AND
    refund_total >= 0 AND
    void_total >= 0 AND
    net_sales >= 0
  )
);



CREATE TABLE product_benefit_rule (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id BIGINT NOT NULL,
  benefit_type VARCHAR(20) NOT NULL,
  benefit_mode VARCHAR(20) NOT NULL,
  vat_exempt SMALLINT NOT NULL DEFAULT 0,
  active SMALLINT NOT NULL DEFAULT 1,
  effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
  effective_to DATE,
  CONSTRAINT fk_pbr_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
  CONSTRAINT ck_pbr_type CHECK (benefit_type IN ('SENIOR','PWD')),
  CONSTRAINT ck_pbr_mode CHECK (benefit_mode IN ('NONE','DISCOUNT_20','DISCOUNT_5_BNPC')),
  CONSTRAINT ck_pbr_flags CHECK (vat_exempt IN (0,1) AND active IN (0,1))
);



CREATE VIEW v_sale_line_discount AS
SELECT
  sl.id AS sale_line_id,
  COALESCE(SUM(sd.amount), 0) AS discount_total
FROM sale_line sl
LEFT JOIN sale_discount sd ON sd.sale_line_id = sl.id
GROUP BY sl.id;

CREATE VIEW v_sale_header_discount AS
SELECT
  s.id AS sale_id,
  COALESCE(SUM(sd.amount), 0) AS discount_total
FROM sale s
LEFT JOIN sale_discount sd
  ON sd.sale_id = s.id
 AND sd.sale_line_id IS NULL
GROUP BY s.id;

CREATE VIEW v_sale_line_tax_total AS
SELECT
  sl.id AS sale_line_id,
  COALESCE(SUM(t.tax_amount), 0) AS tax_total
FROM sale_line sl
LEFT JOIN sale_line_tax t ON t.sale_line_id = sl.id
GROUP BY sl.id;

CREATE VIEW v_sale_totals AS
SELECT
  s.id AS sale_id,
  s.sale_no,
  s.store_id,
  s.terminal_id,
  s.shift_id,
  s.sale_type,
  s.status,
  s.customer_id,
  s.sold_at,
  COALESCE(SUM(sl.qty_in_base * sl.unit_price), 0) AS gross_amount,
  COALESCE(SUM(COALESCE(vld.discount_total, 0)), 0) AS line_discount_total,
  COALESCE(MAX(vhd.discount_total), 0) AS header_discount_total,
  COALESCE(SUM(COALESCE(vld.discount_total, 0)), 0) + COALESCE(MAX(vhd.discount_total), 0) AS discount_total,
  COALESCE(SUM(COALESCE(vlt.tax_total, 0)), 0) AS tax_total,
  COALESCE(SUM(
      (sl.qty_in_base * sl.unit_price)
      - COALESCE(vld.discount_total,0)
      + CASE WHEN sl.price_includes_tax = 1 THEN 0 ELSE COALESCE(vlt.tax_total,0) END
  ), 0) - COALESCE(MAX(vhd.discount_total),0) AS total_due,
  COALESCE(SUM(sl.cost_total), 0) AS cost_total,
  (
    (
      COALESCE(SUM(
          (sl.qty_in_base * sl.unit_price)
          - COALESCE(vld.discount_total,0)
          + CASE WHEN sl.price_includes_tax = 1 THEN 0 ELSE COALESCE(vlt.tax_total,0) END
      ), 0) - COALESCE(MAX(vhd.discount_total),0)
    )
    - COALESCE(SUM(COALESCE(vlt.tax_total, 0)), 0)
    - COALESCE(SUM(sl.cost_total), 0)
  ) AS profit_amount
FROM sale s
LEFT JOIN sale_line sl ON sl.sale_id = s.id
LEFT JOIN v_sale_line_discount vld ON vld.sale_line_id = sl.id
LEFT JOIN v_sale_line_tax_total vlt ON vlt.sale_line_id = sl.id
LEFT JOIN v_sale_header_discount vhd ON vhd.sale_id = s.id
GROUP BY
  s.id, s.sale_no, s.store_id, s.terminal_id, s.shift_id, s.sale_type, s.status, s.customer_id, s.sold_at;

CREATE VIEW v_sale_tax_breakdown AS
SELECT
  s.id AS sale_id,
  COALESCE(t.tax_code, 'NO_TAX') AS tax_code,
  COALESCE(t.tax_rate, 0) AS tax_rate,
  COALESCE(SUM(t.tax_amount), 0) AS tax_amount
FROM sale s
JOIN sale_line sl ON sl.sale_id = s.id
LEFT JOIN sale_line_tax t ON t.sale_line_id = sl.id
GROUP BY s.id, COALESCE(t.tax_code, 'NO_TAX'), COALESCE(t.tax_rate, 0);

CREATE VIEW v_stock_balance AS
SELECT
  sb.store_id,
  sb.product_id,
  p.sku,
  p.name AS product_name,
  sb.status_code,
  sb.qty_in_base
FROM stock_balance sb
JOIN product p ON p.id = sb.product_id;

CREATE TABLE database_trail (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  location VARCHAR(255) NOT NULL,
  date_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  transactions INT NOT NULL DEFAULT 0
);
