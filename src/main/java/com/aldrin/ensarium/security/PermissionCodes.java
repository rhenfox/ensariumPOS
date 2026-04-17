package com.aldrin.ensarium.security;

import java.util.List;

public final class PermissionCodes {

    private PermissionCodes() {
    }

    public static final String DASH = "DASH";
    public static final String SALES = "SALES";
    public static final String SHIFT = "SHIFT";
    public static final String STOCKIN = "STOCKIN";
    public static final String ORDER = "ORDER";
    public static final String CHARTS = "CHARTS";
    public static final String MAINTENANCE = "MAINTENANCE";
    public static final String STATUS = "STATUS";
    public static final String INVENTORY = "INVENTORY";
    public static final String INVENTORY_TXN = "INVENTORY_TXN";
    public static final String INVENTORY_ONHAND = "INVENTORY_ONHAND";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String SUPPLIER = "SUPPLIER";
    public static final String MASTER_DATA = "MASTER_DATA";
    public static final String PRINTER = "PRINTER";
    public static final String PAYMENT_METHOD = "PAYMENT";
    public static final String BENEFIT_POLICY = "BENEFIT_POLICY";
    public static final String DISCOUNT_TYPE = "DISCOUNT_TYPE";
    public static final String RETURN_REASON = "RETURN_REASON";
    public static final String PRODUCTS = "PRODUCTS";
    public static final String STORE = "STORE";
    public static final String FISCAL_BIR = "FISCAL_BIR";
    public static final String SETUP_SALE = "SETUP_SALE";
    public static final String SETUP_INVENTORY = "SETUP_INVENTORY";
    public static final String BIR_TAX = "BIR_TAX";
    public static final String TAX_SUMMARY = "TAX_SUMMARY";
    public static final String POS_PROFIT = "POS_PROFIT";
    public static final String FINANCIAL = "FINANCIAL";

    // Security page/menu permissions. These codes match the sidebar menu keys.
    public static final String USERS_PAGE = "USERS";
    public static final String ROLES_PERMS_PAGE = "ROLES_PERMS";
    public static final String AUDIT_PAGE = "AUDIT";

    // Optional action-level permissions kept for compatibility.
    public static final String USER_WRITE = "USER_WRITE";
    public static final String ROLE_WRITE = "ROLE_WRITE";

    public static List<String> allPermissionCodes() {
        return List.of(DASH, SALES, SHIFT, STOCKIN, ORDER, CHARTS, MAINTENANCE, STATUS, INVENTORY, INVENTORY_TXN, INVENTORY_ONHAND, CUSTOMER, SUPPLIER, MASTER_DATA, PRINTER, PAYMENT_METHOD, BENEFIT_POLICY, DISCOUNT_TYPE, RETURN_REASON, PRODUCTS, STORE, FISCAL_BIR, SETUP_SALE, SETUP_INVENTORY,
                USERS_PAGE, ROLES_PERMS_PAGE, AUDIT_PAGE,
                USER_WRITE, ROLE_WRITE,BIR_TAX,TAX_SUMMARY,POS_PROFIT,FINANCIAL
        );
    }
}
