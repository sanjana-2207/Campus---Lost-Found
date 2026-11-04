package com.campus.web.service;

import com.campus.web.model.ClaimStatus;
import com.campus.web.model.Item;
import com.campus.web.model.ItemStatus;
import com.campus.web.model.ItemType;
import com.campus.web.repository.ClaimRepository;
import com.campus.web.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReportService {

    private final ItemRepository items;
    private final ClaimRepository claims;

    public ReportService(ItemRepository items, ClaimRepository claims) {
        this.items = items;
        this.claims = claims;
    }

    public ReportSummary buildSummary() {
        ReportSummary summary = new ReportSummary();

        long total = items.count();
        summary.setTotalItems(total);
        summary.setTotalLost(items.countByType(ItemType.LOST));
        summary.setTotalFound(items.countByType(ItemType.FOUND));
        summary.setTotalRecovered(items.countByStatus(ItemStatus.RECOVERED));
        summary.setTotalClaimed(items.countByStatus(ItemStatus.CLAIMED));
        summary.setTotalOpen(items.countByStatusIn(
                List.of(ItemStatus.OPEN, ItemStatus.MATCHED)));
        summary.setRecoveryRate(total == 0 ? 0.0
                : (summary.getTotalRecovered() * 100.0) / total);

        Map<ItemStatus, Long> byStatus = new LinkedHashMap<>();
        for (ItemStatus status : ItemStatus.values()) {
            byStatus.put(status, 0L);
        }
        for (Object[] row : items.countGroupedByStatus()) {
            byStatus.put((ItemStatus) row[0], ((Number) row[1]).longValue());
        }
        summary.setByStatus(byStatus);

        Map<ClaimStatus, Long> claimsByStatus = new LinkedHashMap<>();
        long totalClaims = 0;
        for (ClaimStatus status : ClaimStatus.values()) {
            long count = claims.countByStatus(status);
            claimsByStatus.put(status, count);
            totalClaims += count;
        }
        summary.setClaimsByStatus(claimsByStatus);
        summary.setTotalClaims(totalClaims);
        summary.setPendingClaims(claims.countByStatus(ClaimStatus.PENDING));

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Object[] row : items.countGroupedByCategory()) {
            byCategory.put((String) row[0], ((Number) row[1]).longValue());
        }
        summary.setByCategory(byCategory);

        Map<String, Map<String, Long>> monthly = new LinkedHashMap<>();
        for (Object[] row : items.countGroupedByMonthAndType()) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            ItemType type = (ItemType) row[2];
            long count = ((Number) row[3]).longValue();
            String key = String.format(Locale.ROOT, "%02d-%04d", month, year);
            Map<String, Long> values = monthly.computeIfAbsent(key,
                    k -> new LinkedHashMap<>());
            values.put(type.getLabel(), count);
        }
        summary.setMonthly(monthly);
        return summary;
    }

    public String exportItemsCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Type,Title,Category,Location,Date,Status,Reporter,Description\n");
        for (Item item : items.findAll()) {
            csv.append(item.getId()).append(',')
                    .append(escape(item.getTypeLabel())).append(',')
                    .append(escape(item.getTitle())).append(',')
                    .append(escape(item.getCategoryName())).append(',')
                    .append(escape(item.getLocationName())).append(',')
                    .append(escape(item.getEventDateLabel())).append(',')
                    .append(escape(item.getStatusLabel())).append(',')
                    .append(escape(item.getReporterName())).append(',')
                    .append(escape(item.getDescription())).append('\n');
        }
        return csv.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.replace("\"", "\"\"");
        if (cleaned.contains(",") || cleaned.contains("\n") || cleaned.contains("\"")) {
            return "\"" + cleaned + "\"";
        }
        return cleaned;
    }
}
