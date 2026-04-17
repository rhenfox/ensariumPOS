-- Run this only if your database was created from an older schema before the cost snapshot columns were added.
-- Skip this file if you created the database from the latest full schema.

ALTER TABLE sale_line ADD COLUMN buying_price DECIMAL(19,4) NOT NULL DEFAULT 0;
ALTER TABLE sale_invoice_line ADD COLUMN buying_price DECIMAL(19,4) NOT NULL DEFAULT 0;
ALTER TABLE sale_invoice_line ADD COLUMN cost_total DECIMAL(19,4) NOT NULL DEFAULT 0;
ALTER TABLE sales_return_line ADD COLUMN buying_price DECIMAL(19,4) NOT NULL DEFAULT 0;
ALTER TABLE sales_return_line ADD COLUMN cost_total_refund DECIMAL(19,4) NOT NULL DEFAULT 0;

UPDATE sale_line SET buying_price = 0 WHERE buying_price IS NULL;
UPDATE sale_invoice_line SET buying_price = 0 WHERE buying_price IS NULL;
UPDATE sale_invoice_line SET cost_total = 0 WHERE cost_total IS NULL;
UPDATE sales_return_line SET buying_price = 0 WHERE buying_price IS NULL;
UPDATE sales_return_line SET cost_total_refund = 0 WHERE cost_total_refund IS NULL;
