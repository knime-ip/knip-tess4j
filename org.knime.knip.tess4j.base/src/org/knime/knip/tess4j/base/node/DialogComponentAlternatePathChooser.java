/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * --------------------------------------------------------------------- *
 *
 */
package org.knime.knip.tess4j.base.node;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.port.PortObjectSpec;

/**
 * DialogComponentAlternatePathChooser
 * 
 * Gives the user the option to choose and internal path or a external. When
 * using the external path, the user must choose one via a FileChooser
 * 
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:jonathan.hale@uni-konstanz.de">Jonathan Hale</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael
 *         Zinsmaier</a>
 */
public class DialogComponentAlternatePathChooser extends DialogComponent
		implements ActionListener {

	private SettingsModelOptionalString m_settingsModel;

	private JFileChooser m_fileChooser;

	private JTextField m_textField;
	private JButton m_button;

	private JRadioButton m_rb1;
	private JRadioButton m_rb2;

	private JPanel m_contents;

	/**
	 * Constructor with NodeModel
	 * 
	 * @param model
	 *            the Node model to be used
	 */
	public DialogComponentAlternatePathChooser(SettingsModelOptionalString model) {
		super(model);

		m_settingsModel = model;

		m_contents = new JPanel(new GridBagLayout());

		GridBagConstraints gbc_rb1 = new GridBagConstraints(0, 0, 2, 1, 1.0,
				0.0, GridBagConstraints.PAGE_START,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
		GridBagConstraints gbc_rb2 = new GridBagConstraints(0, 1, 2, 1, 1.0,
				0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
		GridBagConstraints gbc_tf = new GridBagConstraints(0, 2, 1, 1, 1.0,
				0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
		GridBagConstraints gbc_btn = new GridBagConstraints(1, 2, 1, 1, 1.0,
				0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 5, 0, 0), 0, 0);

		m_fileChooser = new JFileChooser();
		m_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		m_textField = new JTextField();
		m_textField.setEditable(false);
		m_textField.setText(m_settingsModel.getStringValue());

		m_button = new JButton("...");
		m_button.setActionCommand("open");

		m_rb1 = new JRadioButton("Use Internal");
		m_rb1.setSelected(!m_settingsModel.isActive());
		m_rb1.setActionCommand("intern");

		m_rb2 = new JRadioButton("Use External");
		m_rb2.setSelected(m_settingsModel.isActive());
		m_rb2.setActionCommand("extern");

		m_rb1.addActionListener(this);
		m_rb2.addActionListener(this);
		m_button.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		group.add(m_rb1);
		group.add(m_rb2);

		TitledBorder border = BorderFactory.createTitledBorder("Tessdata Path");
		m_contents.setBorder(border);

		m_contents.add(m_rb1, gbc_rb1);
		m_contents.add(m_rb2, gbc_rb2);
		m_contents.add(m_textField, gbc_tf);
		m_contents.add(m_button, gbc_btn);

		m_textField.setEnabled(m_settingsModel.isActive());
		m_button.setEnabled(m_settingsModel.isActive());
	}

	@Override
	public JPanel getComponentPanel() {
		return m_contents;
	}

	@Override
	protected void updateComponent() {

	}

	@Override
	protected void validateSettingsBeforeSave() throws InvalidSettingsException {
		if (m_settingsModel.getStringValue().isEmpty()
				&& m_settingsModel.isActive()) {
			throw new InvalidSettingsException("Path cannot be empty.");
		}
	}

	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs)
			throws NotConfigurableException {

	}

	@Override
	protected void setEnabledComponents(boolean enabled) {

	}

	@Override
	public void setToolTipText(String text) {

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("intern")) {
			m_textField.setEnabled(false);
			m_button.setEnabled(false);

			m_settingsModel.setIsActive(false);
		} else if (arg0.getActionCommand().equals("extern")) {
			m_textField.setEnabled(true);
			m_button.setEnabled(true);

			m_settingsModel.setIsActive(true);
		} else if (arg0.getActionCommand().equals("open")) {

			int i = m_fileChooser.showOpenDialog(m_contents);

			if (i == JFileChooser.APPROVE_OPTION) {
				m_settingsModel.setStringValue(m_fileChooser.getSelectedFile()
						.getAbsolutePath());

				m_textField.setText(m_settingsModel.getStringValue());
			}
		}
	}
}
