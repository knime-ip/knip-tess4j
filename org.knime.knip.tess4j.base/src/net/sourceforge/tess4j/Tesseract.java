/**
 * Copyright @ 2012 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sourceforge.tess4j;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.tess4j.util.Utils;

import com.sun.jna.Pointer;

/**
 * An object layer on top of
 * <code>TessAPI</code>, provides character recognition support for common image
 * formats, and multi-page TIFF images beyond the uncompressed, binary TIFF
 * format supported by Tesseract OCR engine. The extended capabilities are
 * provided by the
 * <code>Java Advanced Imaging Image I/O Tools</code>. <br /><br /> Support for
 * PDF documents is available through
 * <code>Ghost4J</code>, a
 * <code>JNA</code> wrapper for
 * <code>GPL Ghostscript</code>, which should be installed and included in
 * system path. <br /><br /> Any program that uses the library will need to
 * ensure that the required libraries (the
 * <code>.jar</code> files for
 * <code>jna</code>,
 * <code>jai-imageio</code>, and
 * <code>ghost4j</code>) are in its compile and run-time
 * <code>classpath</code>.
 */
public class Tesseract implements ITesseract {

    private static Tesseract instance;
    private String language = "eng";
    private String datapath = "./";
    private int psm = TessAPI.TessPageSegMode.PSM_AUTO;
    private boolean hocr;
    private int pageNum;
    private int ocrEngineMode = TessAPI.TessOcrEngineMode.OEM_DEFAULT;
    private final Properties prop = new Properties();
    
    private TessAPI api;
    private TessAPI.TessBaseAPI handle;
    
    private final static Logger logger = Logger.getLogger(Tesseract.class.getName());

    /**
     * Private constructor.
     */
    private Tesseract() {
        System.setProperty("jna.encoding", "UTF8");
    }

    /**
     * Gets an instance of the class library.
     *
     * @return instance
     */
    public static synchronized Tesseract getInstance() {
        if (instance == null) {
            instance = new Tesseract();
        }

        return instance;
    }

    /**
     * Sets tessdata path.
     *
     * @param datapath the tessdata path to set
     */
    @Override
	public void setDatapath(final String datapath) {
        this.datapath = datapath;
    }

    /**
     * Sets language for OCR.
     *
     * @param language the language code, which follows ISO 639-3 standard.
     */
    @Override
	public void setLanguage(final String language) {
        this.language = language;
    }

    /**
     * Sets OCR engine mode.
     *
     * @param ocrEngineMode the OcrEngineMode to set
     */
    @Override
	public void setOcrEngineMode(final int ocrEngineMode) {
        this.ocrEngineMode = ocrEngineMode;
    }

    /**
     * Sets page segmentation mode.
     *
     * @param mode the page segmentation mode to set
     */
    @Override
	public void setPageSegMode(final int mode) {
        this.psm = mode;
    }

    /**
     * Enables hocr output.
     *
     * @param hocr to enable or disable hocr output
     */
    public void setHocr(final boolean hocr) {
        this.hocr = hocr;
        prop.setProperty("tessedit_create_hocr", hocr ? "1" : "0");
    }

    /**
     * Set the value of Tesseract's internal parameter.
     *
     * @param key variable name, e.g.,
     * <code>tessedit_create_hocr</code>,
     * <code>tessedit_char_whitelist</code>, etc.
     * @param value value for corresponding variable, e.g., "1", "0",
     * "0123456789", etc.
     */
    @Override
	public void setTessVariable(final String key, final String value) {
        prop.setProperty(key, value);
    }

    /**
     * Performs OCR operation.
     *
     * @param bi a buffered image
     * @return the recognized text
     * @throws TesseractException
     */
    @Override
	public String doOCR(final BufferedImage bi) throws TesseractException {
        return doOCR(bi, null);
    }

    /**
     * Performs OCR operation. Use
     * <code>SetImage</code>, (optionally)
     * <code>SetRectangle</code>, and one or more of the
     * <code>Get*Text</code> functions.
     *
     * @param xsize width of image
     * @param ysize height of image
     * @param buf pixel data
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or
     * <code>null</code> indicates the whole image.
     * @param bpp bits per pixel, represents the bit depth of the image, with 1
     * for binary bitmap, 8 for gray, and 24 for color RGB.
     * @return the recognized text
     * @throws TesseractException
     */
    @Override
	public String doOCR(final int xsize, final int ysize, final ByteBuffer buf, final Rectangle rect, final int bpp) throws TesseractException {
        init();
        setTessVariables();

        try {
            setImage(xsize, ysize, buf, rect, bpp);
            return getOCRText();
        } catch (final Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new TesseractException(e);
        } finally {
            dispose();
        }
    }

    /**
     * Initializes Tesseract engine.
     */
    private void init() {
        pageNum = 0;
        api = TessAPI.INSTANCE;
        handle = api.TessBaseAPICreate();
        api.TessBaseAPIInit2(handle, datapath, language, ocrEngineMode);
        api.TessBaseAPISetPageSegMode(handle, psm);
    }
    
    /**
     * Sets Tesseract's internal parameters.
     */
    private void setTessVariables() {
        final Enumeration<?> em = prop.propertyNames();
        while (em.hasMoreElements()) {
            final String key = (String) em.nextElement();
            api.TessBaseAPISetVariable(handle, key, prop.getProperty(key));
        }
    }
    
    /**
     * Sets image to be processed.
     * 
     * @param xsize width of image
     * @param ysize height of image
     * @param buf pixel data
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or
     * <code>null</code> indicates the whole image.
     * @param bpp bits per pixel, represents the bit depth of the image, with 1
     * for binary bitmap, 8 for gray, and 24 for color RGB.
     */
    private void setImage(final int xsize, final int ysize, final ByteBuffer buf, final Rectangle rect, final int bpp) {
        final int bytespp = bpp / 8;
        final int bytespl = (int) Math.ceil(xsize * bpp / 8.0);
        api.TessBaseAPISetImage(handle, buf, xsize, ysize, bytespp, bytespl);

        if (rect != null && !rect.isEmpty()) {
            api.TessBaseAPISetRectangle(handle, rect.x, rect.y, rect.width, rect.height);
        }
    }
    
    /**
     * Gets recognized text.
     * 
     * @return the recognized text
     */
    private String getOCRText() {
        final Pointer utf8Text = hocr ? api.TessBaseAPIGetHOCRText(handle, pageNum - 1) : api.TessBaseAPIGetUTF8Text(handle);
        final String str = utf8Text.getString(0);
        api.TessDeleteText(utf8Text);
        return str;
    }
    
    /**
     * Releases all of the native resources used by this instance.
     */    
    private void dispose() {
        api.TessBaseAPIDelete(handle);
    }

	@Override
	public String doOCR(final BufferedImage bi, final Rectangle rect)
			throws TesseractException {
		return doOCR(bi.getWidth(), bi.getHeight(), Utils.convertImageData(bi), rect, bi.getColorModel().getPixelSize());
	}
}
