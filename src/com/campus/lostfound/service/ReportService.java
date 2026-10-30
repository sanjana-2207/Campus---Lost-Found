package com.campus.lostfound.service;

import com.campus.lostfound.dao.ClaimDAO;
import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.dao.JdbcClaimDAO;
import com.campus.lostfound.dao.JdbcItemDAO;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.ClaimStatus;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds the dashboard / report numbers and exports them to CSV.
 *
 * <p>Every figure is produced by a SQL aggregate ({@code GROUP BY}), never by
 * loading all rows into memory - an important DBMS point to mention in the
 * presentation.</p>
 */
public class ReportService {

    private final ItemDAO itemDAO;
    private final ClaimDAO claimDAO;

    public ReportService() {
        this(new JdbcItemDAO(), new JdbcClaimDAO());
    }

    public ReportService(ItemDAO itemDAO, ClaimDAO claimDAO) {
        this.itemDAO = itemDAO;
        this.claimDAO = claimDAO;
    }

    /** Collects every counter required by the Dashboard and Reports screens. */
    public ReportSummary buildSummary() {
        ReportSummary summary = new ReportSummary();

        Map<ItemStatus, Integer> byStatus = itemDAO.countByStatus();
        int total = sum(byStatus.values());

        summary.setByStatus(byStatus);
        summary.setTotalItems(total);
        summary.setTotalLost(itemDAO.countByType(ItemType.LOST));
        summary.setTotalFound(itemDAO.countByType(ItemType.FOUND));
        summary.setTotalOpen(valueOf(byStatus, ItemStatus.OPEN));
        summary.setTotalClaimed(valueOf(byStatus, ItemStatus.CLAIMED));
        summary.setTotalRecovered(valueOf(byStatus, ItemStatus.RECOVERED));

        Map<ClaimStatus, Integer> claimCounts = claimDAO.countByStatus();
        summary.setClaimsByStatus(claimCounts);
        summary.setTotalClaims(sum(claimCounts.values()));
        summary.setPendingClaims(valueOf(claimCounts, ClaimStatus.PENDING));

        summary.setByCategory(itemDAO.countByCategory(null));
        summary.setByLocation(itemDAO.countByLocation());
        summary.setMonthlyLost(itemDAO.countByMonth(ItemType.LOST));
        summary.setMonthlyFound(itemDAO.countByMonth(ItemType.FOUND));

        summary.setRecoveryRate(total == 0 ? 0.0 : (summary.getTotalRecovered() * 100.0) / total);
        return summary;
    }

    public List<Item> allItems() {
        return itemDAO.findAll();
    }

    public List<Claim> allClaims() {
        return claimDAO.findAllDetailed();
    }

    // ==================================================================
    //  CSV export
    // ==================================================================

    /** Writes one CSV row per item report. */
    public void exportItems(File file, List<Item> items) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(),
                StandardCharsets.UTF_8)) {

            writer.write('\uFEFF'); // BOM so Excel opens UTF-8 correctly.
            writer.write("ID,Type,Title,Category,Location,Date,Status,Reporter,Description");
            writer.newLine();

            for (Item item : items) {
                writer.write(String.valueOf(item.getItemId()));
                writer.write(',');
                writer.write(escape(item.getItemType().getLabel()));
                writer.write(',');
                writer.write(escape(item.getTitle()));
                writer.write(',');
                writer.write(escape(item.getCategoryName()));
                writer.write(',');
                writer.write(escape(item.getLocationName()));
                writer.write(',');
                writer.write(item.getEventDate() == null ? "" : item.getEventDate().toString());
                writer.write(',');
                writer.write(escape(item.getStatus().getLabel()));
                writer.write(',');
                writer.write(escape(item.getReporterName()));
                writer.write(',');
                writer.write(escape(item.getDescription()));
                writer.newLine();
            }
        }
    }

    /** Writes the summary counters and the category table. */
    public void exportSummary(File file, ReportSummary summary) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(),
                StandardCharsets.UTF_8)) {

            writer.write('\uFEFF');
            writer.write("Campus Lost and Found - Summary Report");
            writer.newLine();
            writer.write("Metric,Value");
            writer.newLine();
            writer.write("Total reports," + summary.getTotalItems());
            writer.newLine();
            writer.write("Lost reports," + summary.getTotalLost());
            writer.newLine();
            writer.write("Found reports," + summary.getTotalFound());
            writer.newLine();
            writer.write("Open items," + summary.getTotalOpen());
            writer.newLine();
            writer.write("Claimed items," + summary.getTotalClaimed());
            writer.newLine();
            writer.write("Recovered items," + summary.getTotalRecovered());
            writer.newLine();
            writer.write("Total claims," + summary.getTotalClaims());
            writer.newLine();
            writer.write("Pending claims," + summary.getPendingClaims());
            writer.newLine();
            writer.write(String.format(Locale.US, "Recovery rate,%.1f%%",
                    summary.getRecoveryRate()));
            writer.newLine();
            writer.newLine();

            writer.write("Category,Reports");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : summary.getByCategory().entrySet()) {
                writer.write(escape(entry.getKey()) + "," + entry.getValue());
                writer.newLine();
            }
        }
    }

    // ------------------------------------------------------------------

    private int sum(Iterable<Integer> values) {
        int total = 0;
        for (Integer value : values) {
            total += value == null ? 0 : value;
        }
        return total;
    }

    private int valueOf(Map<?, Integer> map, Object key) {
        Integer value = map.get(key);
        return value == null ? 0 : value;
    }

    /** Quotes a CSV field when it contains a comma, quote or newline. */
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
