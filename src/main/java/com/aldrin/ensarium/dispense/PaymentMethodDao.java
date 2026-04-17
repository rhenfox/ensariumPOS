package com.aldrin.ensarium.dispense;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodDao {
    public List<PaymentMethodRef> findAll(Connection conn) throws Exception {
        List<PaymentMethodRef> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, code, name FROM payment_method WHERE active = 1 ORDER BY name")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PaymentMethodRef p = new PaymentMethodRef();
                    p.id = rs.getInt("id");
                    p.code = rs.getString("code");
                    p.name = rs.getString("name");
                    out.add(p);
                }
            }
        }
        return out;
    }
}
