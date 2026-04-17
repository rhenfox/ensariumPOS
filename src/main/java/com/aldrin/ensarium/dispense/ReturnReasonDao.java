package com.aldrin.ensarium.dispense;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReturnReasonDao {
    public List<ReturnReasonRef> findAll(Connection conn) throws Exception {
        List<ReturnReasonRef> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, code, name FROM return_reason ORDER BY name")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReturnReasonRef r = new ReturnReasonRef();
                    r.id = rs.getInt("id");
                    r.code = rs.getString("code");
                    r.name = rs.getString("name");
                    out.add(r);
                }
            }
        }
        return out;
    }
}
