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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.imglib2.type.numeric.RealType;
import net.sourceforge.tess4j.ITesseract;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;

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
public class Tess4JNodeDialog<T extends RealType<T>> extends
		ValueToCellNodeDialog<ImgPlusValue<T>> implements ChangeListener {

	private DialogComponentStringSelection m_languageListComponent;
	private DialogComponentAlternatePathChooser m_pathChooser;

	private final Tess4JNodeSettings m_settings = new Tess4JNodeSettings();
	private final List<String> m_languages = new ArrayList<String>();

	public Tess4JNodeDialog() {
		super(true);
		
		addDialogComponents();
        buildDialog();
	}
	
	@Override
	public void addDialogComponents() {
		updateLanguages();

		m_languageListComponent = new DialogComponentStringSelection(m_settings.languageModel(),
				"Language", m_languages);

		m_pathChooser = new DialogComponentAlternatePathChooser(m_settings.pathModel());

		m_settings.pathModel().addChangeListener(this);

		addDialogComponent(m_pathChooser);
		addDialogComponent(m_languageListComponent);
		
		addDialogComponent(new DialogComponentBoolean(m_settings.deskewModel(), "Deskew input images"));
		addDialogComponent(new DialogComponentStringIndexSelection(m_settings.pageSegModeModel(), "Page Segmentation Mode", ITesseract.PageSegMode.m_valueNames));
		addDialogComponent(new DialogComponentStringIndexSelection(m_settings.ocrEngineModeModel(), "OCR Engine Mode", ITesseract.OcrEngineMode.m_valueNames));
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
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		if (!updateLanguages()) { 
			throw new InvalidSettingsException("No tesseract language (.tessdata) files found in selected path.");
		}
		
		super.saveAdditionalSettingsTo(settings);
	}
}
