package com.aldrin.ensarium.dispense;

public class PaymentMethodRef {
    public int id;
    public String code;
    public String name;

    @Override
    public String toString() {
        return name;
    }
}
