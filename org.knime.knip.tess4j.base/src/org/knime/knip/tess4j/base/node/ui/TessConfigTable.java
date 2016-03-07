package org.knime.knip.tess4j.base.node.ui;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.util.Pair;
import org.knime.knip.tess4j.base.node.Tess4JNodeSettings;

/**
 * Table containing key-value config pairs for tesseract.
 * 
 * @author Jonathan Hale
 *
 */
public class TessConfigTable extends DialogComponent {

	final TessConfigTableModel m_model = new TessConfigTableModel();
	final JTable m_table = new JTable(m_model);
	final SettingsModelStringArray m_settingsModel;

	public TessConfigTable(final SettingsModelStringArray model) {
		super(model);
		m_settingsModel = model;

		/* add table to DialogComponent content pane */
		final JScrollPane tableScrollPane = new JScrollPane(m_table);
		m_table.setFillsViewportHeight(true);

		final JPanel panel = this.getComponentPanel();
		panel.setLayout(new BorderLayout());
		panel.add(tableScrollPane, BorderLayout.CENTER);
		panel.add(m_table.getTableHeader(), BorderLayout.PAGE_START);

		/* setup column cell editors */
		final DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
		editor.setClickCountToStart(1);

		final TableColumn keyColumn = m_table.getColumnModel().getColumn(0);
		keyColumn.setCellEditor(editor);

		final TableColumn valColumn = m_table.getColumnModel().getColumn(1);
		valColumn.setCellEditor(editor);
	}

	@Override
	protected void updateComponent() {
		m_model.contents().clear();
		m_model.contents().addAll(Tess4JNodeSettings.toTessConfigPairs(m_settingsModel.getStringArrayValue()));
	}

	@Override
	protected void validateSettingsBeforeSave() throws InvalidSettingsException {
		for (Iterator<Pair<String, String>> iterator = m_model.contents().iterator(); iterator.hasNext();) {
			Pair<String, String> pair = iterator.next();

			if (pair.getFirst().isEmpty() || pair.getSecond().isEmpty()) {
				throw new InvalidSettingsException("Table cannot contain empty keys/values.");
			}
		}
		m_settingsModel.setStringArrayValue(Tess4JNodeSettings.toStringArray(m_model.contents()));
	}

	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs) throws NotConfigurableException {
		// nothing to do
	}

	@Override
	protected void setEnabledComponents(boolean enabled) {
		m_table.setEnabled(enabled);
	}

	@Override
	public void setToolTipText(String text) {
		m_table.setToolTipText(text);
	}

	/**
	 * @return the underlying table model
	 */
	public TessConfigTableModel model() {
		return m_model;
	}

	public JTable table() {
		return m_table;
	}
}
