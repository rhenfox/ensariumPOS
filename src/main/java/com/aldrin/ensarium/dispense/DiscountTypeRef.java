package com.aldrin.ensarium.dispense;

import java.io.Serializable;

public class DiscountTypeRef implements Serializable {
    public Integer id;
    public String code;
    public String name;
    public String kind;
    public String appliesTo;
    public boolean active = true;

    public boolean isPercent() {
        return "PERCENT".equalsIgnoreCase(kind);
    }

    public boolean isAmount() {
        return "AMOUNT".equalsIgnoreCase(kind);
    }

    @Override
    public String toString() {
        String n = name == null ? "" : name;
        String c = code == null || code.isBlank() ? "" : " [" + code + "]";
        String k = kind == null || kind.isBlank() ? "" : " - " + kind;
        return n + c + k;
    }
}
