package com.aldrin.ensarium.benefit;

public class LookupOption {

    private final long id;
    private final String label;

    public LookupOption(long id, String label) {
        this.id = id;
        this.label = label;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return label;
    }
}
