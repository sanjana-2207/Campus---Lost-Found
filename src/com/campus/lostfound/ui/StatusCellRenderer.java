package com.campus.lostfound.ui;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * Colours status cells (Open, Claimed, Pending, Rejected, ...) so a table can be
 * read at a glance during the demo.
 */
public class StatusCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    public StatusCellRenderer() {
        setHorizontalAlignment(SwingConstants.LEFT);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        if (!isSelected) {
            component.setForeground(colourFor(value == null ? "" : value.toString()));
        }
        component.setFont(component.getFont().deriveFont(Font.BOLD));
        return component;
    }

    private Color colourFor(String value) {
        String text = value.toLowerCase();
        if (text.contains("recovered") || text.contains("approved")) {
            return UIStyle.SUCCESS;
        }
        if (text.contains("rejected") || text.contains("closed")) {
            return UIStyle.DANGER;
        }
        if (text.contains("pending") || text.contains("claimed")) {
            return UIStyle.WARNING;
        }
        return UIStyle.ACCENT_DARK;
    }
}
