package org.knime.knip.tess4j.base.node;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.util.Pair;

import net.sourceforge.tess4j.ITesseract;

/**
 * Settings of the Tess4JNode.
 * 
 * @author Jonathan Hale (University of Konstanz)
 */
public class Tess4JNodeSettings {

	private final SettingsModelString m_languageModel = createTessLanguageModel();
	private final SettingsModelOptionalString m_pathModel = createTessdataPathModel();
	private final SettingsModelInteger m_pageSegMode = createTessPageSegModeModel();
	private final SettingsModelInteger m_ocrEngineMode = createTessOcrEngineModeModel();
	private final SettingsModelBoolean m_deskewModel = createTessDeskewModel();
	private final SettingsModelStringArray m_advancedConfig = createTessAdvancedConfigModel();

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
	 * Creates a SettingsModel for Tesseract Page Segmentation Mode
	 * 
	 * @return
	 */
	public static SettingsModelInteger createTessPageSegModeModel() {
		return new SettingsModelInteger("PageSegMode", ITesseract.PageSegMode.PSM_AUTO_ONLY.m_mode);
	}

	/**
	 * Creates a SettingsModel for Tesseract OCR Engine Mode
	 * 
	 * @return
	 */
	public static SettingsModelInteger createTessOcrEngineModeModel() {
		return new SettingsModelInteger("OcrEngineMode", ITesseract.OcrEngineMode.OEM_DEFAULT.m_mode);
	}

	/**
	 * Creates a SettingsModel for using deskew
	 * 
	 * @return
	 */
	public static SettingsModelBoolean createTessDeskewModel() {
		return new SettingsModelBoolean("Deskew", true);
	}

	/**
	 * Creates a SettingsModel for the advanced tesseract config key-value pair
	 * list
	 * 
	 * @return
	 */
	public static SettingsModelStringArray createTessAdvancedConfigModel() {
		return new SettingsModelStringArray("TessConfig", new String[] {});
	}

	/**
	 * Add settings to settingsModels.
	 * 
	 * @param settingsModels
	 */
	public void addSettingsModels(final List<SettingsModel> settingsModels) {
		settingsModels.add(m_languageModel);
		settingsModels.add(m_pathModel);
		settingsModels.add(m_pageSegMode);
		settingsModels.add(m_ocrEngineMode);
		settingsModels.add(m_deskewModel);
		settingsModels.add(m_advancedConfig);
	}

	/**
	 * @return language {@link SettingsModel}.
	 */
	public SettingsModelString languageModel() {
		return m_languageModel;
	}

	/**
	 * @return {@link SettingsModel} for the tessdata path.
	 */
	public SettingsModelOptionalString pathModel() {
		return m_pathModel;
	}

	/**
	 * @return {@link SettingsModel} for the Page Segmentation Mode.
	 */
	public SettingsModelInteger pageSegModeModel() {
		return m_pageSegMode;
	}

	/**
	 * @return {@link SettingsModel} for the OCR Engine to use.
	 */
	public SettingsModelInteger ocrEngineModeModel() {
		return m_ocrEngineMode;
	}

	/**
	 * @return {@link SettingsModel} for whether to use deskew.
	 */
	public SettingsModelBoolean deskewModel() {
		return m_deskewModel;
	}

	/**
	 * @return {@link SettingsModel} for advanced tesseract config key-value
	 *         pairs.
	 */
	public SettingsModelStringArray tessAdvancedConfigModel() {
		return m_advancedConfig;
	}

	/**
	 * @return the language to use for OCR.
	 */
	public String getLanguage() {
		return languageModel().getStringValue();
	}

	/**
	 * @return Path to tesseract language data. (tessdata folder)
	 */
	public String getTessdataPath() {
		if (pathModel().isActive()) {
			// User defined language path
			return pathModel().getStringValue();
		}
		// Our built-in language path
		return Tess4JNodeSettings.getEclipsePath("platform:/plugin/org.knime.knip.tess4j.base/tessdata/");
	}

	/**
	 * @return Integer for the Page Segmentation Mode to use.
	 */
	public int getPageSegMode() {
		return pageSegModeModel().getIntValue();
	}

	/**
	 * @return Integer for the OCR engine to use.
	 */
	public int getOcrEngineMode() {
		return ocrEngineModeModel().getIntValue();
	}

	/**
	 * @return the language to use for OCR.
	 */
	public boolean useDeskew() {
		return deskewModel().getBooleanValue();
	}

	/**
	 * @return the tesseract configuration key-value pairs
	 */
	public List<Pair<String, String>> tessAdvancedConfig() {
		return toTessConfigPairs(m_advancedConfig.getStringArrayValue());
	}

	/**
	 * Turn an array of strings into a ArrayList of String pairs by splitting at
	 * the first ' '.
	 * 
	 * @param strings
	 *            array to split up
	 * @return the array list containing key-value pairs
	 */
	public static ArrayList<Pair<String, String>> toTessConfigPairs(final String[] strings) {
		final ArrayList<Pair<String, String>> list = new ArrayList<>();

		for (String line : strings) {
			final int index = line.indexOf(' ');
			list.add(new Pair<String, String>(line.substring(0, index), line.substring(index + 1)));
		}

		return list;
	}

	/**
	 * Set the key-value pairs for advanced configuration.
	 * 
	 * @param config
	 *            the key-value pairs
	 */
	public void setTessAdvancedConfig(final List<Pair<String, String>> config) {
		final ArrayList<String> list = new ArrayList<>();

		for (Pair<String, String> pair : config) {
			list.add(pair.getFirst() + " " + pair.getSecond());
		}

		m_advancedConfig.setStringArrayValue(list.toArray(new String[list.size()]));
	}

	/**
	 * Helper Function to resolve platform urls
	 * 
	 * @param platformurl
	 * @return
	 */
	public static String getEclipsePath(final String platformurl) {
		try {
			final URL url = new URL(platformurl);
			final File dir = new File(FileLocator.resolve(url).getFile());
			return dir.getAbsolutePath();
		} catch (final IOException e) {
			return null;
		}
	}

}
