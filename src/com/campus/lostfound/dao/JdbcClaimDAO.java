package com.campus.lostfound.dao;

import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.ClaimStatus;
import com.campus.lostfound.model.FoundItem;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.LostItem;
import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation of {@link ClaimDAO}.
 *
 * <p>The methods that receive a {@link Connection} deliberately do <b>not</b>
 * close it. The connection, and therefore the transaction, belongs to
 * {@code ClaimService}; this DAO only contributes SQL statements to it. That
 * separation is what makes the approve / reject / recover workflows atomic.</p>
 */
public class JdbcClaimDAO extends AbstractJdbcDAO<Claim> implements ClaimDAO {

    private static final String BASE_SELECT =
            "SELECT cl.claim_id, cl.item_id, cl.claimant_id, cl.proof, cl.status, "
          + "       cl.submitted_at, cl.verified_by, cl.verified_at, cl.remarks, "
          + "       i.title AS item_title, i.item_type, "
          + "       u.full_name AS claimant_name, u.email AS claimant_email, "
          + "       u.phone AS claimant_phone, u.role AS claimant_role, "
          + "       v.full_name AS verifier_name "
          + "FROM claims cl "
          + "JOIN items i ON i.item_id  = cl.item_id "
          + "JOIN users u ON u.user_id  = cl.claimant_id "
          + "LEFT JOIN users v ON v.user_id = cl.verified_by ";

    @Override
    public Claim findById(int id) {
        String sql = BASE_SELECT + "WHERE cl.claim_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException ex) {
            throw wrap("Could not load claim #" + id, ex);
        }
    }

    @Override
    public List<Claim> findAll() {
        return findAllDetailed();
    }

    @Override
    public List<Claim> findAllDetailed() {
        String sql = BASE_SELECT + "ORDER BY cl.submitted_at DESC, cl.claim_id DESC";
        List<Claim> claims = new ArrayList<Claim>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                claims.add(mapRow(rs));
            }
            return claims;
        } catch (SQLException ex) {
            throw wrap("Could not load claims", ex);
        }
    }

    @Override
    public List<Claim> findByItem(int itemId) {
        return selectList(BASE_SELECT + "WHERE cl.item_id = ? "
                        + "ORDER BY cl.submitted_at DESC",
                itemId);
    }

    @Override
    public List<Claim> findByClaimant(int claimantId) {
        return selectList(BASE_SELECT + "WHERE cl.claimant_id = ? "
                        + "ORDER BY cl.submitted_at DESC",
                claimantId);
    }

    @Override
    public boolean existsFor(int itemId, int claimantId) {
        String sql = "SELECT COUNT(*) FROM claims WHERE item_id = ? AND claimant_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);
            statement.setInt(2, claimantId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw wrap("Could not check the existing claim", ex);
        }
    }

    @Override
    public int insert(Claim claim) {
        String sql = "INSERT INTO claims (item_id, claimant_id, proof, status) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, claim.getItem().getItemId());
            statement.setInt(2, claim.getClaimant().getUserId());
            statement.setString(3, claim.getProof());
            statement.setString(4, ClaimStatus.PENDING.name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    claim.setClaimId(keys.getInt(1));
                }
            }
            return claim.getClaimId();

        } catch (SQLException ex) {
            throw wrap("Could not submit the claim", ex);
        }
    }

    @Override
    public boolean update(Claim claim) {
        String sql = "UPDATE claims SET proof = ?, status = ? WHERE claim_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, claim.getProof());
            statement.setString(2, claim.getStatus().name());
            statement.setInt(3, claim.getClaimId());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw wrap("Could not update the claim", ex);
        }
    }

    @Override
    public boolean updateStatus(Connection connection, int claimId, ClaimStatus status,
                                Integer verifierId, String remarks) {
        boolean withVerifier = verifierId != null;
        String sql = withVerifier
                ? "UPDATE claims SET status = ?, verified_by = ?, verified_at = NOW(), "
                  + "remarks = ? WHERE claim_id = ?"
                : "UPDATE claims SET status = ?, verified_at = NOW(), remarks = ? "
                  + "WHERE claim_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, status.name());
            if (withVerifier) {
                statement.setInt(index++, verifierId);
            }
            if (remarks == null) {
                statement.setNull(index++, Types.VARCHAR);
            } else {
                statement.setString(index++, remarks);
            }
            statement.setInt(index, claimId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw wrap("Could not update the claim status", ex);
        }
    }

    @Override
    public int rejectOtherPending(Connection connection, int itemId, int approvedClaimId,
                                  int verifierId, String remarks) {
        String sql = "UPDATE claims SET status = 'REJECTED', verified_by = ?, "
                   + "verified_at = NOW(), remarks = ? "
                   + "WHERE item_id = ? AND claim_id <> ? AND status = 'PENDING'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, verifierId);
            statement.setString(2, remarks);
            statement.setInt(3, itemId);
            statement.setInt(4, approvedClaimId);
            return statement.executeUpdate();
        } catch (SQLException ex) {
            throw wrap("Could not close the competing claims", ex);
        }
    }

    @Override
    public Map<ClaimStatus, Integer> countByStatus() {
        Map<ClaimStatus, Integer> counts = new EnumMap<ClaimStatus, Integer>(ClaimStatus.class);
        for (ClaimStatus status : ClaimStatus.values()) {
            counts.put(status, 0);
        }

        String sql = "SELECT status, COUNT(*) AS total FROM claims GROUP BY status";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                counts.put(ClaimStatus.valueOf(rs.getString("status")), rs.getInt("total"));
            }
            return counts;
        } catch (SQLException ex) {
            throw wrap("Could not count claims", ex);
        }
    }

    @Override
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM claims WHERE status = 'PENDING'";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw wrap("Could not count the pending claims", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM claims WHERE claim_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw wrap("Could not delete the claim", ex);
        }
    }

    // ------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------

    private List<Claim> selectList(String sql, int parameter) {
        List<Claim> claims = new ArrayList<Claim>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, parameter);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    claims.add(mapRow(rs));
                }
            }
            return claims;
        } catch (SQLException ex) {
            throw wrap("Could not load claims", ex);
        }
    }

    private Claim mapRow(ResultSet rs) throws SQLException {
        Claim claim = new Claim();
        claim.setClaimId(rs.getInt("claim_id"));

        ItemType type = ItemType.valueOf(rs.getString("item_type"));
        Item item = type == ItemType.LOST ? new LostItem() : new FoundItem();
        item.setItemId(rs.getInt("item_id"));
        item.setTitle(rs.getString("item_title"));
        claim.setItem(item);

        User claimant = new User();
        claimant.setUserId(rs.getInt("claimant_id"));
        claimant.setFullName(rs.getString("claimant_name"));
        claimant.setEmail(rs.getString("claimant_email"));
        claimant.setPhone(rs.getString("claimant_phone"));
        claimant.setRole(Role.valueOf(rs.getString("claimant_role")));
        claim.setClaimant(claimant);

        claim.setProof(rs.getString("proof"));
        claim.setStatus(ClaimStatus.valueOf(rs.getString("status")));
        claim.setSubmittedAt(rs.getTimestamp("submitted_at"));
        claim.setVerifiedAt(rs.getTimestamp("verified_at"));
        claim.setRemarks(rs.getString("remarks"));

        int verifierId = rs.getInt("verified_by");
        if (!rs.wasNull()) {
            User verifier = new User();
            verifier.setUserId(verifierId);
            verifier.setFullName(rs.getString("verifier_name"));
            claim.setVerifiedBy(verifier);
        }
        return claim;
    }
}
