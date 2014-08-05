/**
 * Copyright @ 2014 Quan Nguyen
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

/**
 * An interface represents common OCR methods.
 */
public interface ITesseract {
	
    String htmlBeginTag = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\""
            + " \"http://www.w3.org/TR/html4/loose.dtd\">\n"
            + "<html>\n<head>\n<title></title>\n"
            + "<meta http-equiv=\"Content-Type\" content=\"text/html;"
            + "charset=utf-8\" />\n<meta name='ocr-system' content='tesseract'/>\n"
            + "</head>\n<body>\n";
    String htmlEndTag = "</body>\n</html>\n";

    
    enum PageSegMode {
		PSM_OSD_ONLY(0),       ///< Orientation and script detection only.
		PSM_AUTO_OSD(1),       ///< Automatic page segmentation with orientation and
								///< script detection. (OSD)
		PSM_AUTO_ONLY(2),      ///< Automatic page segmentation, but no OSD, or OCR.
		PSM_AUTO(3),           ///< Fully automatic page segmentation, but no OSD.
		PSM_SINGLE_COLUMN(4),  ///< Assume a single column of text of variable sizes.
		PSM_SINGLE_BLOCK_VERT_TEXT(5),  ///< Assume a single uniform block of vertically
		   						///< aligned text.
		PSM_SINGLE_BLOCK(6),   ///< Assume a single uniform block of text. (Default.)
		PSM_SINGLE_LINE(7),    ///< Treat the image as a single text line.
		PSM_SINGLE_WORD(8),    ///< Treat the image as a single word.
		PSM_CIRCLE_WORD(9),    ///< Treat the image as a single word in a circle.
		PSM_SINGLE_CHAR(10),    ///< Treat the image as a single character.
		PSM_SPARSE_TEXT(11),    ///< Find as much text as possible in no particular order.
		PSM_SPARSE_TEXT_OSD(12),  ///< Sparse text with orientation and script det.
		
		PSM_COUNT(13);           ///< Number of enum entries.
		  
		public int m_mode;
		  
		PageSegMode(final int m) {
			m_mode = m;
		}
		
		public final static String[] m_valueNames = {
				"OSD Only",
				"Auto Pageseg and OSD",
				
				"Auto Pageseg Only",
				"Full Auto Pageseg",
				"Single Column",
				"Single Vert Block",
				
				"Single Block",
				"Single Line",
				"Single Word",
				"Circle Word",
				"Single Char",
				"Sparse Text",
				"Sparse Test with OSD"
		};
  	};
  	
  	
  	enum OcrEngineMode {
		OEM_TESSERACT_ONLY(0),           // Run Tesseract only - fastest
		OEM_CUBE_ONLY(1),                // Run Cube only - better accuracy, but slower
		OEM_TESSERACT_CUBE_COMBINED(2),  // Run both and combine results - best accuracy
		OEM_DEFAULT(3);                  // Specify this mode when calling init_*(),
		  	                                // to indicate that any of the above modes
		  	                                // should be automatically inferred from the
		  	                                // variables in the language-specific config,
		  	                                // command-line configs, or if not specified
		  	                                // in any of the above should be set to the
		  	                                // default OEM_TESSERACT_ONLY.
		public int m_mode;
				 
		OcrEngineMode(final int m) {
			m_mode = m;
		}
		
		public final static String[] m_valueNames = {
			"Tesseract Only",
			"Cube Only",
			"Tesseract And Cube",
			"Default"
		};
  	};

    /**
     * Performs OCR operation.
     *
     * @param bi a buffered image
     * @return the recognized text
     * @throws TesseractException
     */
    String doOCR(BufferedImage bi) throws TesseractException;

    /**
     * Performs OCR operation.
     *
     * @param bi a buffered image
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or <code>null</code> indicates
     * the whole image.
     * @return the recognized text
     * @throws TesseractException
     */
    String doOCR(BufferedImage bi, Rectangle rect) throws TesseractException;

    /**
     * Performs OCR operation. Use <code>SetImage</code>, (optionally)
     * <code>SetRectangle</code>, and one or more of the <code>Get*Text</code>
     * functions.
     *
     * @param xsize width of image
     * @param ysize height of image
     * @param buf pixel data
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or <code>null</code> indicates
     * the whole image.
     * @param bpp bits per pixel, represents the bit depth of the image, with 1
     * for binary bitmap, 8 for gray, and 24 for color RGB.
     * @return the recognized text
     * @throws TesseractException
     */
    String doOCR(int xsize, int ysize, ByteBuffer buf, Rectangle rect, int bpp) throws TesseractException;

    /**
     * Sets tessdata path.
     *
     * @param datapath the tessdata path to set
     */
    void setDatapath(String datapath);

    /**
     * Sets language for OCR.
     *
     * @param language the language code, which follows ISO 639-3 standard.
     */
    void setLanguage(String language);

    /**
     * Sets OCR engine mode.
     *
     * @param ocrEngineMode the OcrEngineMode to set
     */
    void setOcrEngineMode(int ocrEngineMode);

    /**
     * Sets page segmentation mode.
     *
     * @param mode the page segmentation mode to set
     */
    void setPageSegMode(int mode);

    /**
     * Sets the value of Tesseract's internal parameter.
     *
     * @param key variable name, e.g., <code>tessedit_create_hocr</code>,
     * <code>tessedit_char_whitelist</code>, etc.
     * @param value value for corresponding variable, e.g., "1", "0",
     * "0123456789", etc.
     */
    void setTessVariable(String key, String value);
}
