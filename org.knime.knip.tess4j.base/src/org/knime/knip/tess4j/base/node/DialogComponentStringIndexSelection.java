package org.knime.knip.tess4j.base.node;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.port.PortObjectSpec;

/**
 * DialogComponent for assigning an index of a String list to a SettingsModelInteger.
 * The String list is represented in a {@link JComboBox}.
 * @author <a href="mailto:jonathan.hale@uni-konstanz.de">Jonathan Hale</a>
 */
public class DialogComponentStringIndexSelection extends DialogComponent implements ItemListener {

	private JPanel m_componentPanel;
	private JComboBox<String> m_comboBox;
	private JLabel m_label;
	
	private SettingsModelInteger m_model;
	
	/**
	 * Constructor
	 * @param model SettingsModel to modify.
	 * @param label Label for this DialogComponent.
	 * @param values Contents String array.
	 */
	public DialogComponentStringIndexSelection(SettingsModelInteger model, String label, String[] values) {
		super(model);
		m_model = model;
		
		m_componentPanel = new JPanel(new GridLayout(2, 1));
		
		m_comboBox = new JComboBox<String>(values);
		m_comboBox.addItemListener(this);
		
		m_label = new JLabel(label);
		
		m_componentPanel.add(m_label);
		m_componentPanel.add(m_comboBox);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JPanel getComponentPanel() {
		return m_componentPanel;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateComponent() {
		m_comboBox.setSelectedIndex(m_model.getIntValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettingsBeforeSave() throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs)
			throws NotConfigurableException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setEnabledComponents(boolean enabled) {
		m_comboBox.setEnabled(enabled);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setToolTipText(String text) {
		m_comboBox.setToolTipText(text);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == m_comboBox) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				m_model.setIntValue(m_comboBox.getSelectedIndex());
			}
		}
	}

}
