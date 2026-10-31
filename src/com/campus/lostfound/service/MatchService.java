package com.campus.lostfound.service;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.dao.JdbcItemDAO;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.MatchResult;

import java.util.List;

/**
 * Matching engine facade.
 *
 * <p>The heavy lifting (pairing every lost report with every found report and
 * scoring the pair) happens inside the SQL view {@code vw_potential_matches},
 * because the database is far better at set operations over thousands of rows
 * than Java loops. This class explains the score in plain English for the UI.</p>
 */
public class MatchService {

    private final ItemDAO itemDAO;

    public MatchService() {
        this(new JdbcItemDAO());
    }

    public MatchService(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

    public List<MatchResult> findMatches() {
        return itemDAO.findPotentialMatches();
    }

    /** Human readable justification shown when a match row is selected. */
    public String explain(MatchResult match) {
        if (match == null) {
            return "Select a match to see why it was suggested.";
        }
        StringBuilder text = new StringBuilder();
        text.append("Same category (").append(match.getCategoryName()).append(")");
        text.append(" and same location (").append(match.getLocationName()).append("). ");
        text.append("The two reports were filed ").append(match.getDaysApart());
        text.append(match.getDaysApart() == 1 ? " day apart. " : " days apart. ");
        text.append("Score ").append(match.getScore()).append("/100 (");
        text.append(match.getConfidence().toLowerCase()).append(" confidence).");
        return text.toString();
    }

    /** Convenience for the details dialog of a single item. */
    public Item findById(int itemId) {
        return itemDAO.findById(itemId);
    }
}
