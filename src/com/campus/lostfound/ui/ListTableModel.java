package com.campus.lostfound.ui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic read-only table model.
 *
 * <p>Instead of writing a new {@link AbstractTableModel} subclass for every
 * screen, each panel supplies column names plus a list of {@code Object[]} rows.
 * This keeps the Swing screens short and consistent.</p>
 */
public class ListTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final String[] columns;
    private final List<Object[]> rows = new ArrayList<Object[]>();

    public ListTableModel(String[] columns, List<Object[]> rows) {
        this.columns = columns;
        setRows(rows);
    }

    public ListTableModel(String[] columns) {
        this(columns, new ArrayList<Object[]>());
    }

    public void setRows(List<Object[]> newRows) {
        rows.clear();
        if (newRows != null) {
            rows.addAll(newRows);
        }
        fireTableDataChanged();
    }

    /** Row backing the given view index, used when a button is pressed. */
    public Object[] getRow(int index) {
        return rows.get(index);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex)[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
