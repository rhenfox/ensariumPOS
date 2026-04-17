//package com.aldrin.ensarium.order;
//
//import java.util.Objects;
//
//public class SupplierOption {
//
//    private final Long id;
//    private final String name;
//
//    public SupplierOption(Long id, String name) {
//        this.id = id;
//        this.name = name == null ? "" : name;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public String toString() {
////        return name == null || name.isBlank() ? "(No Supplier)" : name;
//        return name;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (!(o instanceof SupplierOption that)) {
//            return false;
//        }
//        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id, name);
//    }
//}

package com.aldrin.ensarium.order;

import java.util.Objects;

public class SupplierOption {
    private final Long id;
    private final String name;

    public SupplierOption(Long id, String name) {
        this.id = id;
        this.name = name == null ? "" : name.trim();
    }

    public Long getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name.isBlank() ? "(No Supplier)" : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierOption that)) return false;

        // compare by id first when available
        if (this.id != null || that.id != null) {
            return Objects.equals(this.id, that.id);
        }

        return this.name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : name.toLowerCase().hashCode();
    }
}
