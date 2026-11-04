package com.campus.lostfound.ui;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

/** Centres item photo thumbnails in the table columns. */
public class PhotoCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    public PhotoCellRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean selected, boolean focused,
                                                   int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, selected, focused, row, column);
        label.setText("");
        label.setIcon(value instanceof javax.swing.Icon
                ? (javax.swing.Icon) value : null);
        return label;
    }
}
