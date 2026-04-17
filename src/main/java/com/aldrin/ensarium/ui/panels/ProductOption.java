package com.aldrin.ensarium.ui.panels;

//public record ProductOption(long id, String sku, String name) {
//    @Override
//    public String toString() {
//        return sku + " - " + name;
//    }
//}
public record ProductOption(long id, String sku, String name) {
    @Override
    public String toString() {
        return  name;
    }
}