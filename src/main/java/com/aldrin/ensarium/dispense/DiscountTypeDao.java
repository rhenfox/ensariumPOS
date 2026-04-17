package com.aldrin.ensarium.dispense;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DiscountTypeDao {

    public List<DiscountTypeRef> listActive(Connection conn) throws Exception {
        List<DiscountTypeRef> out = new ArrayList<>();
        String sql = """
            SELECT id, code, name, kind, applies_to, active
              FROM discount_type
             WHERE active = 1
             ORDER BY name, code
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DiscountTypeRef d = new DiscountTypeRef();
                d.id = rs.getInt("id");
                d.code = rs.getString("code");
                d.name = rs.getString("name");
                d.kind = rs.getString("kind");
                d.appliesTo = rs.getString("applies_to");
                d.active = rs.getInt("active") == 1;
                out.add(d);
            }
        }
        return out;
    }

    public DiscountTypeRef findById(Connection conn, Integer id) throws Exception {
        if (id == null) return null;
        String sql = "SELECT id, code, name, kind, applies_to, active FROM discount_type WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                DiscountTypeRef d = new DiscountTypeRef();
                d.id = rs.getInt("id");
                d.code = rs.getString("code");
                d.name = rs.getString("name");
                d.kind = rs.getString("kind");
                d.appliesTo = rs.getString("applies_to");
                d.active = rs.getInt("active") == 1;
                return d;
            }
        }
    }
}
