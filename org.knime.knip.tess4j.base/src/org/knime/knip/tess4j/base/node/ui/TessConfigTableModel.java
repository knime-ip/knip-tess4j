package org.knime.knip.tess4j.base.node.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.knime.core.util.Pair;
import org.knime.knip.tess4j.base.node.Tess4JNodeSettings;

/**
 * Table model which stores tesseract key value config pairs.
 * 
 * @author Jonathan Hale
 */
public class TessConfigTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 42L;

	private final ArrayList<Pair<String, String>> m_contents = new ArrayList<>();

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return m_contents.size();
	}

	@Override
	public String getColumnName(int column) {
		return (column == 0) ? "Key" : "Value";
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final Pair<String, String> pair = m_contents.get(rowIndex);
		return (columnIndex == 0) ? pair.getFirst() : pair.getSecond();
	}

	/**
	 * @return List which contains the values stored in the table.
	 */
	public List<Pair<String, String>> contents() {
		return m_contents;
	}

	/**
	 * Create a new config entry with key <code>""</code> and value
	 * <code>""</code>.
	 */
	public void addEmptyConfigEntry() {
		final int index = m_contents.size();

		m_contents.add(new Pair<>("", ""));

		this.fireTableRowsInserted(index, index);
	}

	/**
	 * Remove config entry at the given row index.
	 * 
	 * @param index
	 *            the row index to remove a value at
	 */
	public void removeConfigEntry(int index) {
		if (index < 0)
			return;
		m_contents.remove(index);
		this.fireTableRowsDeleted(index, index);
	}

	@Override
	public void setValueAt(final Object aValue, int rowIndex, int columnIndex) {
		if (!(aValue instanceof String)) {
			return;
		}

		final String stringValue = (String) aValue;

		/*
		 * Pair is immutable, so we need to create a new one if the value
		 * changes
		 */
		if (columnIndex == 0) {
			final Pair<String, String> existing = m_contents.get(rowIndex);
			m_contents.set(rowIndex, new Pair<>(stringValue.replace(' ', '_'), existing.getSecond()));
		} else if (columnIndex == 1) {
			final Pair<String, String> existing = m_contents.get(rowIndex);
			m_contents.set(rowIndex, new Pair<>(existing.getFirst(), stringValue));
		} else {
			throw new IndexOutOfBoundsException("Column index out of bounds.");
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

}
