package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.LookupOption;
import com.aldrin.ensarium.model.ReceiptSeriesRow;
import com.aldrin.ensarium.model.StoreFiscalProfileRow;
import com.aldrin.ensarium.model.TaxpayerProfileRow;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FiscalBirService {
    private final AuditService auditService = new AuditService();

    public List<TaxpayerProfileRow> listTaxpayerProfiles() {
        String sql = """
                SELECT id, registered_name, trade_name, tin_no, head_office_address,
                       vat_registration_type, active, created_at
                FROM taxpayer_profile
                ORDER BY registered_name, id
                """;
        List<TaxpayerProfileRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new TaxpayerProfileRow(
                        rs.getInt("id"),
                        rs.getString("registered_name"),
                        rs.getString("trade_name"),
                        rs.getString("tin_no"),
                        rs.getString("head_office_address"),
                        rs.getString("vat_registration_type"),
                        rs.getInt("active") == 1,
                        rs.getTimestamp("created_at")
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load taxpayer profiles", ex);
        }
    }

    public int createTaxpayerProfile(Integer actorUserId,
                                     String registeredName,
                                     String tradeName,
                                     String tinNo,
                                     String headOfficeAddress,
                                     String vatRegistrationType,
                                     boolean active) {
        String sql = "INSERT INTO taxpayer_profile(registered_name, trade_name, tin_no, head_office_address, vat_registration_type, active) VALUES(?,?,?,?,?,?)";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, trim(registeredName));
            ps.setString(2, nullable(tradeName));
            ps.setString(3, trim(tinNo));
            ps.setString(4, trim(headOfficeAddress));
            ps.setString(5, trim(vatRegistrationType));
            ps.setInt(6, active ? 1 : 0);
            ps.executeUpdate();
            int id = generatedId(ps);
            auditService.log(actorUserId, "TAXPAYER_CREATE", "Created taxpayer profile id=" + id);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create taxpayer profile", ex);
        }
    }

    public void updateTaxpayerProfile(Integer actorUserId,
                                      int id,
                                      String registeredName,
                                      String tradeName,
                                      String tinNo,
                                      String headOfficeAddress,
                                      String vatRegistrationType,
                                      boolean active) {
        String sql = "UPDATE taxpayer_profile SET registered_name=?, trade_name=?, tin_no=?, head_office_address=?, vat_registration_type=?, active=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trim(registeredName));
            ps.setString(2, nullable(tradeName));
            ps.setString(3, trim(tinNo));
            ps.setString(4, trim(headOfficeAddress));
            ps.setString(5, trim(vatRegistrationType));
            ps.setInt(6, active ? 1 : 0);
            ps.setInt(7, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "TAXPAYER_UPDATE", "Updated taxpayer profile id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update taxpayer profile", ex);
        }
    }

    public void deleteTaxpayerProfile(Integer actorUserId, int id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM taxpayer_profile WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "TAXPAYER_DELETE", "Deleted taxpayer profile id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete taxpayer profile", ex);
        }
    }

    public List<StoreFiscalProfileRow> listStoreFiscalProfiles() {
        String sql = """
                SELECT sfp.store_id, s.code AS store_code, s.name AS store_name,
                       sfp.taxpayer_profile_id, tp.registered_name AS taxpayer_registered_name,
                       sfp.branch_code, sfp.registered_business_address,
                       sfp.pos_vendor_name, sfp.pos_vendor_tin_no, sfp.pos_vendor_address,
                       sfp.supplier_accreditation_no, sfp.accreditation_issued_at, sfp.accreditation_valid_until,
                       sfp.bir_permit_to_use_no, sfp.permit_to_use_issued_at,
                       sfp.atp_no, sfp.atp_issued_at, sfp.active, sfp.updated_at
                FROM store_fiscal_profile sfp
                JOIN store s ON s.id = sfp.store_id
                JOIN taxpayer_profile tp ON tp.id = sfp.taxpayer_profile_id
                ORDER BY s.code
                """;
        List<StoreFiscalProfileRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new StoreFiscalProfileRow(
                        rs.getInt("store_id"),
                        rs.getString("store_code"),
                        rs.getString("store_name"),
                        rs.getInt("taxpayer_profile_id"),
                        rs.getString("taxpayer_registered_name"),
                        rs.getString("branch_code"),
                        rs.getString("registered_business_address"),
                        rs.getString("pos_vendor_name"),
                        rs.getString("pos_vendor_tin_no"),
                        rs.getString("pos_vendor_address"),
                        rs.getString("supplier_accreditation_no"),
                        rs.getDate("accreditation_issued_at"),
                        rs.getDate("accreditation_valid_until"),
                        rs.getString("bir_permit_to_use_no"),
                        rs.getDate("permit_to_use_issued_at"),
                        rs.getString("atp_no"),
                        rs.getDate("atp_issued_at"),
                        rs.getInt("active") == 1,
                        rs.getTimestamp("updated_at")
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load store fiscal profiles", ex);
        }
    }

    public void createStoreFiscalProfile(Integer actorUserId,
                                         int storeId,
                                         int taxpayerProfileId,
                                         String branchCode,
                                         String registeredBusinessAddress,
                                         String posVendorName,
                                         String posVendorTinNo,
                                         String posVendorAddress,
                                         String supplierAccreditationNo,
                                         Date accreditationIssuedAt,
                                         Date accreditationValidUntil,
                                         String birPermitToUseNo,
                                         Date permitToUseIssuedAt,
                                         String atpNo,
                                         Date atpIssuedAt,
                                         boolean active) {
        String sql = """
                INSERT INTO store_fiscal_profile(
                    store_id, taxpayer_profile_id, branch_code, registered_business_address,
                    pos_vendor_name, pos_vendor_tin_no, pos_vendor_address,
                    supplier_accreditation_no, accreditation_issued_at, accreditation_valid_until,
                    bir_permit_to_use_no, permit_to_use_issued_at, atp_no, atp_issued_at, active
                ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindStoreFiscal(ps, storeId, taxpayerProfileId, branchCode, registeredBusinessAddress, posVendorName, posVendorTinNo,
                    posVendorAddress, supplierAccreditationNo, accreditationIssuedAt, accreditationValidUntil,
                    birPermitToUseNo, permitToUseIssuedAt, atpNo, atpIssuedAt, active);
            ps.executeUpdate();
            auditService.log(actorUserId, "STORE_FISCAL_CREATE", "Created store fiscal profile store_id=" + storeId);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create store fiscal profile", ex);
        }
    }

    public void updateStoreFiscalProfile(Integer actorUserId,
                                         int storeId,
                                         int taxpayerProfileId,
                                         String branchCode,
                                         String registeredBusinessAddress,
                                         String posVendorName,
                                         String posVendorTinNo,
                                         String posVendorAddress,
                                         String supplierAccreditationNo,
                                         Date accreditationIssuedAt,
                                         Date accreditationValidUntil,
                                         String birPermitToUseNo,
                                         Date permitToUseIssuedAt,
                                         String atpNo,
                                         Date atpIssuedAt,
                                         boolean active) {
        String sql = """
                UPDATE store_fiscal_profile
                   SET taxpayer_profile_id=?, branch_code=?, registered_business_address=?,
                       pos_vendor_name=?, pos_vendor_tin_no=?, pos_vendor_address=?,
                       supplier_accreditation_no=?, accreditation_issued_at=?, accreditation_valid_until=?,
                       bir_permit_to_use_no=?, permit_to_use_issued_at=?, atp_no=?, atp_issued_at=?,
                       active=?, updated_at=CURRENT_TIMESTAMP
                 WHERE store_id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, taxpayerProfileId);
            ps.setString(2, trim(branchCode));
            ps.setString(3, trim(registeredBusinessAddress));
            ps.setString(4, nullable(posVendorName));
            ps.setString(5, nullable(posVendorTinNo));
            ps.setString(6, nullable(posVendorAddress));
            ps.setString(7, nullable(supplierAccreditationNo));
            ps.setDate(8, accreditationIssuedAt);
            ps.setDate(9, accreditationValidUntil);
            ps.setString(10, nullable(birPermitToUseNo));
            ps.setDate(11, permitToUseIssuedAt);
            ps.setString(12, nullable(atpNo));
            ps.setDate(13, atpIssuedAt);
            ps.setInt(14, active ? 1 : 0);
            ps.setInt(15, storeId);
            ps.executeUpdate();
            auditService.log(actorUserId, "STORE_FISCAL_UPDATE", "Updated store fiscal profile store_id=" + storeId);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update store fiscal profile", ex);
        }
    }

    public void deleteStoreFiscalProfile(Integer actorUserId, int storeId) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM store_fiscal_profile WHERE store_id=?")) {
            ps.setInt(1, storeId);
            ps.executeUpdate();
            auditService.log(actorUserId, "STORE_FISCAL_DELETE", "Deleted store fiscal profile store_id=" + storeId);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete store fiscal profile", ex);
        }
    }

    public List<ReceiptSeriesRow> listReceiptSeries() {
        String sql = """
                SELECT tfs.id, tfs.terminal_id, t.code AS terminal_code, s.code AS store_code, s.name AS store_name,
                       tfs.doc_type, tfs.prefix, tfs.serial_from, tfs.serial_to, tfs.next_serial,
                       tfs.active, tfs.created_at
                FROM terminal_fiscal_series tfs
                JOIN terminal t ON t.id = tfs.terminal_id
                JOIN store s ON s.id = t.store_id
                ORDER BY s.code, t.code, tfs.doc_type, tfs.serial_from
                """;
        List<ReceiptSeriesRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new ReceiptSeriesRow(
                        rs.getLong("id"),
                        rs.getInt("terminal_id"),
                        rs.getString("terminal_code"),
                        rs.getString("store_code"),
                        rs.getString("store_name"),
                        rs.getString("doc_type"),
                        rs.getString("prefix"),
                        rs.getLong("serial_from"),
                        rs.getLong("serial_to"),
                        rs.getLong("next_serial"),
                        rs.getInt("active") == 1,
                        rs.getTimestamp("created_at")
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load receipt series", ex);
        }
    }

    public long createReceiptSeries(Integer actorUserId,
                                    int terminalId,
                                    String docType,
                                    String prefix,
                                    long serialFrom,
                                    long serialTo,
                                    long nextSerial,
                                    boolean active) {
        String sql = "INSERT INTO terminal_fiscal_series(terminal_id, doc_type, prefix, serial_from, serial_to, next_serial, active) VALUES(?,?,?,?,?,?,?)";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindReceiptSeries(ps, terminalId, docType, prefix, serialFrom, serialTo, nextSerial, active);
            ps.executeUpdate();
            long id = generatedLongId(ps);
            auditService.log(actorUserId, "RECEIPT_SERIES_CREATE", "Created receipt series id=" + id);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create receipt series", ex);
        }
    }

    public void updateReceiptSeries(Integer actorUserId,
                                    long id,
                                    int terminalId,
                                    String docType,
                                    String prefix,
                                    long serialFrom,
                                    long serialTo,
                                    long nextSerial,
                                    boolean active) {
        String sql = "UPDATE terminal_fiscal_series SET terminal_id=?, doc_type=?, prefix=?, serial_from=?, serial_to=?, next_serial=?, active=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindReceiptSeries(ps, terminalId, docType, prefix, serialFrom, serialTo, nextSerial, active);
            ps.setLong(8, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "RECEIPT_SERIES_UPDATE", "Updated receipt series id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update receipt series", ex);
        }
    }

    public void deleteReceiptSeries(Integer actorUserId, long id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM terminal_fiscal_series WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "RECEIPT_SERIES_DELETE", "Deleted receipt series id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete receipt series", ex);
        }
    }

    public List<LookupOption> taxpayerOptions() {
        return lookupOptions("SELECT id, registered_name FROM taxpayer_profile WHERE active = 1 ORDER BY registered_name");
    }

    public List<LookupOption> storeOptions() {
        return lookupOptions("SELECT id, code || ' - ' || name AS label FROM store WHERE active = 1 ORDER BY code");
    }

    public List<LookupOption> terminalOptions() {
        return lookupOptions("""
                SELECT t.id, s.code || ' / ' || t.code || COALESCE(' - ' || t.name, '') AS label
                FROM terminal t
                JOIN store s ON s.id = t.store_id
                WHERE t.active = 1 AND s.active = 1
                ORDER BY s.code, t.code
                """);
    }

    private List<LookupOption> lookupOptions(String sql) {
        List<LookupOption> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new LookupOption(rs.getInt(1), rs.getString(2)));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load lookup options", ex);
        }
    }

    private void bindStoreFiscal(PreparedStatement ps,
                                 int storeId,
                                 int taxpayerProfileId,
                                 String branchCode,
                                 String registeredBusinessAddress,
                                 String posVendorName,
                                 String posVendorTinNo,
                                 String posVendorAddress,
                                 String supplierAccreditationNo,
                                 Date accreditationIssuedAt,
                                 Date accreditationValidUntil,
                                 String birPermitToUseNo,
                                 Date permitToUseIssuedAt,
                                 String atpNo,
                                 Date atpIssuedAt,
                                 boolean active) throws Exception {
        ps.setInt(1, storeId);
        ps.setInt(2, taxpayerProfileId);
        ps.setString(3, trim(branchCode));
        ps.setString(4, trim(registeredBusinessAddress));
        ps.setString(5, nullable(posVendorName));
        ps.setString(6, nullable(posVendorTinNo));
        ps.setString(7, nullable(posVendorAddress));
        ps.setString(8, nullable(supplierAccreditationNo));
        ps.setDate(9, accreditationIssuedAt);
        ps.setDate(10, accreditationValidUntil);
        ps.setString(11, nullable(birPermitToUseNo));
        ps.setDate(12, permitToUseIssuedAt);
        ps.setString(13, nullable(atpNo));
        ps.setDate(14, atpIssuedAt);
        ps.setInt(15, active ? 1 : 0);
    }

    private void bindReceiptSeries(PreparedStatement ps,
                                   int terminalId,
                                   String docType,
                                   String prefix,
                                   long serialFrom,
                                   long serialTo,
                                   long nextSerial,
                                   boolean active) throws Exception {
        ps.setInt(1, terminalId);
        ps.setString(2, trim(docType));
        ps.setString(3, nullable(prefix));
        ps.setLong(4, serialFrom);
        ps.setLong(5, serialTo);
        ps.setLong(6, nextSerial);
        ps.setInt(7, active ? 1 : 0);
    }

    private static String trim(String value) { return value == null ? "" : value.trim(); }
    private static String nullable(String value) { String v = trim(value); return v.isBlank() ? null : v; }

    private static int generatedId(PreparedStatement ps) throws Exception {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new IllegalStateException("Generated key not found.");
    }

    private static long generatedLongId(PreparedStatement ps) throws Exception {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getLong(1);
        }
        throw new IllegalStateException("Generated key not found.");
    }
}
