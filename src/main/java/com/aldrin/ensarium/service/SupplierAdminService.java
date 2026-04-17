package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.SupplierRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SupplierAdminService {
    private final AuditService auditService = new AuditService();

    public List<SupplierRow> listSuppliers() {
        String sql = """
                SELECT id, supplier_no, name, phone, email, address, active, created_at
                FROM supplier
                ORDER BY name, id
                """;
        List<SupplierRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new SupplierRow(
                        rs.getLong("id"),
                        rs.getString("supplier_no"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getInt("active") == 1,
                        rs.getTimestamp("created_at")
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load suppliers", ex);
        }
    }

    public long createSupplier(Integer actorUserId, String supplierNo, String name, String phone, String email, String address, boolean active) {
        String sql = """
                INSERT INTO supplier(supplier_no, name, phone, email, address, active)
                VALUES(?,?,?,?,?,?)
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, supplierNo, name, phone, email, address, active);
            ps.executeUpdate();
            long id = generatedLongId(ps);
            auditService.log(actorUserId, "SUPPLIER_CREATE", "Created supplier: " + name);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create supplier", ex);
        }
    }

    public void updateSupplier(Integer actorUserId, long id, String supplierNo, String name, String phone, String email, String address, boolean active) {
        String sql = """
                UPDATE supplier
                   SET supplier_no=?, name=?, phone=?, email=?, address=?, active=?
                 WHERE id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, supplierNo, name, phone, email, address, active);
            ps.setLong(7, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "SUPPLIER_UPDATE", "Updated supplier id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update supplier", ex);
        }
    }

    public void deleteSupplier(Integer actorUserId, long id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM supplier WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "SUPPLIER_DELETE", "Deleted supplier id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete supplier", ex);
        }
    }

    private void bind(PreparedStatement ps, String supplierNo, String name, String phone, String email, String address, boolean active) throws Exception {
        ps.setString(1, blankToNull(supplierNo));
        ps.setString(2, name);
        ps.setString(3, blankToNull(phone));
        ps.setString(4, blankToNull(email));
        ps.setString(5, blankToNull(address));
        ps.setInt(6, active ? 1 : 0);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private long generatedLongId(PreparedStatement ps) throws Exception {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getLong(1);
            throw new IllegalStateException("Unable to get generated id");
        }
    }
}
