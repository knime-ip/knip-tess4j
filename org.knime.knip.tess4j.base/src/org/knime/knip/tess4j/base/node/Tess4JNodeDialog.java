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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.tess4j.base.node.ui.TessConfigTable;

import net.imglib2.type.numeric.RealType;
import net.sourceforge.tess4j.ITesseract;

/**
 * Tess4JNodeDialog
 * 
 * Node Dialog of the Tess4J Node.
 * 
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:jonathan.hale@uni-konstanz.de">Jonathan Hale</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael
 *         Zinsmaier</a>
 */
public class Tess4JNodeDialog<T extends RealType<T>> extends ValueToCellNodeDialog<ImgPlusValue<T>>
		implements ChangeListener {

	private DialogComponentStringSelection m_languageListComponent;
	private DialogComponentAlternatePathChooser m_pathChooser;

	private final Tess4JNodeSettings m_settings = new Tess4JNodeSettings();
	private final List<String> m_languages = new ArrayList<String>();

	private final List<DialogComponent> m_dialogComponents = new ArrayList<DialogComponent>();

	final TessConfigTable m_tessConfigTable = new TessConfigTable(m_settings.tessAdvancedConfigModel());

	public Tess4JNodeDialog() {
		super(true);

		createOptionsTab();
		createAdvancedConfigTab();
		buildDialog();
	}

	/**
	 * Create a tab which contains basic tesseract settings.
	 */
	private void createOptionsTab() {
		final JPanel contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());

		final JPanel preprocessingPane = new JPanel();
		preprocessingPane.setBorder(BorderFactory.createTitledBorder("Preprocessing"));

		final JPanel recogPane = new JPanel(new GridBagLayout());
		recogPane.setBorder(BorderFactory.createTitledBorder("Recognition Configuration"));

		final DialogComponentBoolean deskewComp = new DialogComponentBoolean(m_settings.deskewModel(),
				"Deskew input images");
		final DialogComponentStringIndexSelection pageSegComp = new DialogComponentStringIndexSelection(
				m_settings.pageSegModeModel(), "Page Segmentation Mode", ITesseract.PageSegMode.m_valueNames);
		final DialogComponentStringIndexSelection ocrModeComp = new DialogComponentStringIndexSelection(
				m_settings.ocrEngineModeModel(), "OCR Engine Mode", ITesseract.OcrEngineMode.m_valueNames);

		final int ANCHOR = GridBagConstraints.FIRST_LINE_START;
		final int FILL = GridBagConstraints.HORIZONTAL;

		final Insets insets = new Insets(0, 4, 0, 4);
		final GridBagConstraints gbc_deskew = new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, ANCHOR, FILL, insets, 0, 0);
		final GridBagConstraints gbc_pathChooser = new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, ANCHOR, FILL, insets, 0,
				0);
		final GridBagConstraints gbc_recog = new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, ANCHOR, FILL, insets, 0, 0);

		final GridBagConstraints gbc_language = new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, ANCHOR, FILL, insets, 0,
				0);
		final GridBagConstraints gbc_pageSet = new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, ANCHOR, FILL, insets, 0, 0);
		final GridBagConstraints gbc_ocrEngine = new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, ANCHOR, FILL, insets, 0,
				0);

		updateLanguages();
		m_languageListComponent = new DialogComponentStringSelection(m_settings.languageModel(), "Language",
				m_languages);

		m_pathChooser = new DialogComponentAlternatePathChooser("Tessdata Path", m_settings.pathModel());

		m_settings.pathModel().addChangeListener(this);

		preprocessingPane.add(deskewComp.getComponentPanel());
		contentPane.add(preprocessingPane, gbc_deskew);

		contentPane.add(m_pathChooser.getComponentPanel(), gbc_pathChooser);

		recogPane.add(m_languageListComponent.getComponentPanel(), gbc_language);
		recogPane.add(pageSegComp.getComponentPanel(), gbc_pageSet);
		recogPane.add(ocrModeComp.getComponentPanel(), gbc_ocrEngine);
		contentPane.add(recogPane, gbc_recog);

		addTab("Settings", contentPane);

		// add dialog components to list
		m_dialogComponents.add(m_pathChooser);
		m_dialogComponents.add(m_languageListComponent);
		m_dialogComponents.add(pageSegComp);
		m_dialogComponents.add(ocrModeComp);
		m_dialogComponents.add(deskewComp);
	}

	/**
	 * Create a tab which contains a table to manually set tesseract config
	 * key-value pairs.
	 */
	private void createAdvancedConfigTab() {
		final JPanel contents = new JPanel(new BorderLayout());

		/* add button */
		final JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener((evt) -> m_tessConfigTable.model().addEmptyConfigEntry());

		final JButton btnDel = new JButton("Remove");
		btnDel.addActionListener(
				(evt) -> m_tessConfigTable.model().removeConfigEntry(m_tessConfigTable.table().getSelectedRow()));

		contents.add(m_tessConfigTable.getComponentPanel(), BorderLayout.CENTER);

		final JPanel labelPanel = new JPanel(new GridBagLayout());
		labelPanel.add(new JLabel("Tesseract configuration for advanced users."),
				new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.FIRST_LINE_START,
						GridBagConstraints.HORIZONTAL, new Insets(3, 3, 6, 3), 0, 0));
		contents.add(labelPanel, BorderLayout.NORTH);

		final JPanel buttons = new JPanel(new GridBagLayout());
		buttons.add(btnAdd, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.FIRST_LINE_START,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		buttons.add(btnDel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.FIRST_LINE_START,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		buttons.add(new JPanel(), new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.FIRST_LINE_START,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0)); /* filler */

		contents.add(buttons, BorderLayout.EAST);

		addTab("Advanced Config", contents);

		m_dialogComponents.add(m_tessConfigTable);
	}

	@Override
	public void stateChanged(final ChangeEvent evt) {
		if (evt.getSource().equals(m_settings.pathModel())) {
			updateLanguages();

			// null tries to keep previous selection.
			m_languageListComponent.replaceListItems(m_languages, null);
		}
	}

	/**
	 * Searches currently selected m_pathModel path for .tessdata files
	 * 
	 * @return true if one or more .tessdata files were found, false otherwise
	 */
	private boolean updateLanguages() {
		m_languages.clear();

		final File file = new File(m_settings.getTessdataPath());

		if (!file.exists()) {
			m_languages.add("Invalid path");
			return false;
		}

		for (final File f : file.listFiles()) {
			if (!f.isDirectory()) {
				final String langFilename = f.getName();

				if (!langFilename.endsWith(".traineddata")) {
					continue;
				}

				m_languages.add(langFilename.substring(0, langFilename.length() - 12));
			}
		}

		if (m_languages.isEmpty()) {
			m_languages.add("No language files found");
			return false;
		}

		Collections.sort(m_languages);

		return true;
	}

	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		if (!updateLanguages()) {
			throw new InvalidSettingsException("No tesseract language (.tessdata) files found in selected path.");
		}

		for (DialogComponent comp : m_dialogComponents) {
			comp.saveSettingsTo(settings);
		}

		super.saveAdditionalSettingsTo(settings);
	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
			throws NotConfigurableException {
		super.loadAdditionalSettingsFrom(settings, specs);

		for (DialogComponent comp : m_dialogComponents) {
			comp.loadSettingsFrom(settings, specs);
		}
	}

	@Override
	public void addDialogComponents() {
	}
}
