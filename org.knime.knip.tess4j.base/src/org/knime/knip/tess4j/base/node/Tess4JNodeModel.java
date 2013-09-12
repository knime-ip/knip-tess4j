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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.vietocr.ImageHelper;

import org.eclipse.core.runtime.FileLocator;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.core.awt.Real2GreyRenderer;

import com.recognition.software.jdeskew.ImageDeskew;

/**
 * Tess4JNodeModel
 * 
 * Node Model of the Tess4J Node.
 * 
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:jonathan.hale@uni-konstanz.de">Jonathan Hale</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael
 *         Zinsmaier</a>
 */
public class Tess4JNodeModel<T extends RealType<T>> extends
		ValueToCellNodeModel<ImgPlusValue<T>, StringCell> {

	private SettingsModelString m_languageModel = createTessLanguageModel();
	private SettingsModelOptionalString m_pathModel = createTessdataPathModel();

	private final double MINIMUM_DESKEW_THRESHOLD = 0.05d;

	@Override
	protected StringCell compute(ImgPlusValue<T> cellValue) throws Exception {
		Tesseract instance = Tesseract.getInstance(); // JNA Interface Mapping

		String path = null;
		if (m_pathModel.isActive()) {
			path = m_pathModel.getStringValue();
		} else {
			path = Tess4JNodeModel
					.getEclipsePath("platform:/plugin/org.knime.knip.tess4j.base/tessdata/");
		}

		instance.setDatapath(path);
		instance.setLanguage(m_languageModel.getStringValue());

		String result = "";

		try {
			Img<T> img = cellValue.getImgPlus();
			Real2GreyRenderer<T> renderer = new Real2GreyRenderer<T>();

			BufferedImage bi = (BufferedImage) renderer.render(img, 0, 1,
					new long[img.numDimensions()]).image();

			ImageDeskew id = new ImageDeskew(bi);
			double imageSkewAngle = id.getSkewAngle(); // determine skew angle
			if ((imageSkewAngle > MINIMUM_DESKEW_THRESHOLD || imageSkewAngle < -(MINIMUM_DESKEW_THRESHOLD))) {
				bi = ImageHelper.rotateImage(bi, -imageSkewAngle); // deskew
																	// image
			}

			result = instance.doOCR(bi);

		} catch (Exception e) {
			this.getLogger().error("Execute failed: Exception was thrown.");
			e.printStackTrace();
		}

		return new StringCell(result);
	}

	@Override
	protected void addSettingsModels(List<SettingsModel> settingsModels) {

	}

	/**
	 * Creates a SetingsModel for the Tesseract Language
	 * 
	 * @return
	 */
	public static SettingsModelString createTessLanguageModel() {
		return new SettingsModelString("TessLanguage", "eng");
	}

	/**
	 * Creates a SettingsModel for the Tesseract Datapath
	 * 
	 * @return
	 */
	public static SettingsModelOptionalString createTessdataPathModel() {
		return new SettingsModelOptionalString("TessdataPath", "", false);
	}

	/**
	 * Helper Function to resolve platform urls
	 * 
	 * @param platformurl
	 * @return
	 */
	public static String getEclipsePath(String platformurl) {
		try {
			URL url = new URL(platformurl);
			File dir = new File(FileLocator.resolve(url).getFile());
			return dir.getAbsolutePath();
		} catch (IOException e) {
			return null;
		}
	}
}
