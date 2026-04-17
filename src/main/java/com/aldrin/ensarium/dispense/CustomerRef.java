package com.aldrin.ensarium.dispense;

import java.io.Serializable;

public class CustomerRef implements Serializable {
    public Long customerId;
    public String customerNo;
    public String fullName;
    public String tinNo;
    public String address;
    public boolean senior;
    public boolean pwd;
    public boolean vatExempt;

    public String benefitLabel() {
        if (senior) return "Senior Citizen";
        if (pwd) return "PWD";
        return "Regular";
    }

    @Override
    public String toString() {
        return fullName == null ? "" : fullName;
    }
}
