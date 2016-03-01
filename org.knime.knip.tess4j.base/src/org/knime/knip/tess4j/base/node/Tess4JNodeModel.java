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
import java.nio.ByteBuffer;
import java.util.List;

import org.knime.core.data.def.StringCell;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.core.awt.Real2GreyRenderer;

import com.recognition.software.jdeskew.ImageDeskew;
import com.sun.jna.Pointer;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;
import net.sourceforge.tess4j.util.ImageIOHelper;

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
public class Tess4JNodeModel<T extends RealType<T>> extends ValueToCellNodeModel<ImgPlusValue<T>, StringCell> {

	private final double MINIMUM_DESKEW_THRESHOLD = 0.05d;

	private final Tess4JNodeSettings m_settings = new Tess4JNodeSettings();
	private Tesseract m_tessInstance;

	@Override
	protected void prepareExecute(final ExecutionContext exec) {

		this.setWarningMessage(null);
		exec.setMessage("Preparing execution");

		// JNA interface mapping
		m_tessInstance = new Tesseract();

		// tell tesseract which and language path to use
		m_tessInstance.setDatapath(m_settings.getTessdataPath());
		m_tessInstance.setLanguage(m_settings.getLanguage());
		m_tessInstance.setOcrEngineMode(m_settings.getOcrEngineMode());
		m_tessInstance.setPageSegMode(m_settings.getPageSegMode());
		m_tessInstance.setHocr(false);

		getLogger().debug("Preparing Tess4JNode execution: ");
		getLogger().debug("Tessdata path: " + m_settings.getTessdataPath());
		getLogger().debug("Language: " + m_settings.getLanguage());
		getLogger().debug("OCR Engine Mode: " + m_settings.getOcrEngineMode());
		getLogger().debug("Page Segmentation Mode: " + m_settings.getPageSegMode());

		try {
			m_tessInstance.init();
		} catch (Throwable e) {
			m_tessInstance = null;
			getLogger().error("Error initializing Tesseract.", e);
			throw e;
		}

		getLogger().debug("Initialized tesseract.");

		/* load custom config key-value pairs */
		for (final Pair<String, String> config : m_settings.tessAdvancedConfig()) {
			m_tessInstance.setTessVariable(config.getFirst(), config.getSecond());
		}

		m_tessInstance.setTessVariables();
	}

	private ExecutionContext context;

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		PortObject[] ret = null;

		context = exec;

		try {
			ret = super.execute(inObjects, exec);
		} catch (final Exception e) {
			getLogger().error(e.getMessage(), e);
			throw new TesseractException(e);
		} finally {
			cleanupExecute();
		}

		context = null;

		return ret;
	}

	@Override
	protected StringCell compute(final ImgPlusValue<T> cellValue) throws Exception {
		String result = "";

		getLogger().debug(": >" + cellValue.getImgPlus().getImg().getClass().getName());

		try {
			// the input image
			final Img<T> img = cellValue.getImgPlus();

			// For converting our image to grey values
			final Real2GreyRenderer<T> greyRenderer = new Real2GreyRenderer<T>();

			// Create a BufferedImage from the grey input image
			BufferedImage bi = (BufferedImage) greyRenderer.render(img, 0, 1, new long[img.numDimensions()]).image();

			if (m_settings.useDeskew()) {
				context.setMessage("Deskew");
				// java.lang.IllegalArgumentException: Unknown image type 0
				final ImageDeskew id = new ImageDeskew(bi);
				// determine skew angle
				final double imageSkewAngle = id.getSkewAngle();

				if ((imageSkewAngle > MINIMUM_DESKEW_THRESHOLD || imageSkewAngle < -(MINIMUM_DESKEW_THRESHOLD))) {
					// deskew the image
					bi = ImageHelper.rotateImage(bi, -imageSkewAngle);
				}
			}

			context.setMessage("Recognition");
			// convert image to have a byte buffer
			bi = ImageHelper.convertImageToGrayscale(bi);
			// convert the byte buffer for native use
			ByteBuffer convertImageData = ImageIOHelper.convertImageData(bi);

			// pass the image to tesseract
			final int width = bi.getWidth();
			final int height = bi.getHeight();
			final int bytesPerPixel = bi.getColorModel().getPixelSize() / 8;
			int bytesPerLine = (int) Math.ceil(width * bytesPerPixel);
			m_tessInstance.getAPI().TessBaseAPISetImage(m_tessInstance.getHandle(), convertImageData, width, height,
					bytesPerPixel, bytesPerLine);

			// process and get the result
			Pointer utf8Text = m_tessInstance.getAPI().TessBaseAPIGetUTF8Text(m_tessInstance.getHandle());
			if (utf8Text != null) {
				result = utf8Text.getString(0);
				m_tessInstance.getAPI().TessDeleteText(utf8Text);
			}
		} catch (final Exception e) {
			throw e;
		} finally {
			context.setMessage(null);
		}

		return new StringCell(result);
	}

	protected void cleanupExecute() {
		if (m_tessInstance != null) {
			m_tessInstance.dispose();
		}
	}

	@Override
	protected void addSettingsModels(final List<SettingsModel> settingsModels) {
		m_settings.addSettingsModels(settingsModels);
	}

}
