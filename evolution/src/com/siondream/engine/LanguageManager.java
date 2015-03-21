package com.siondream.engine;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class LanguageManager {
	private static final String DEFAULT_LANGUAGE = "en_UK";
	
	private String m_languagesFile = null;
	private HashMap<String, String> m_language = null;
	private String m_languageName = null;
	
	private static final Logger m_logger = Game.getLogger();
	private static final PlatformResolver m_platformResolver = Game.getPlatformResolver();
	
	public LanguageManager() {
		this("data/languages.xml", "");
	}
	
	public LanguageManager(String languagesFile, String languagesName) {		
		// Languages file
		m_languagesFile = languagesFile;
		
		// Create language map
		m_language = new HashMap<String, String>();
		
		// Default language (system language)
		m_languageName = languagesName;
		
		if (m_languageName.equals("") && m_platformResolver != null) {
			m_languageName = m_platformResolver.getDefaultLanguage();
		}
		
		//  Try to load selected language, if it fails, load default one
		if (!loadLanguage(m_languageName)) {
			loadLanguage(DEFAULT_LANGUAGE);
		}
	}
	
	public String getLanguagesFile() {
		return m_languagesFile;
	}
	
	public void setLanguagesFile(String languagesFile) {
		m_logger.info("LanguageManager: setting languages file to " + languagesFile);
		m_languagesFile = languagesFile;
	}
	
	public String getLanguage() {
		return m_languageName;
	}
	
	public boolean loadLanguage() {
		return loadLanguage(m_languageName);
	}
	
	public boolean loadLanguage(String languageName) {
		m_logger.info("LanguageManager: loading " + languageName);
		try {
			// Parse xml document
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(m_languagesFile).read());
			
			Array<Element> languages = root.getChildrenByName("language");
			
			// Iterate over languages, trying to find the selected one
			for (int i = 0; i < languages.size; ++i) {
				Element language = languages.get(i);
				
				if (language.getAttribute("name").equals(languageName)) {
					// Clear the previous language
					m_language.clear();
					Element languageElement = (Element)language;
					Array<Element> strings = languageElement.getChildrenByName("string");
					
					// Load all the strings for that language
					for (int j = 0; j < strings.size; ++j) {
						Element stringNode = strings.get(j);
						String key = stringNode.getAttribute("key");
						String value = stringNode.getAttribute("value");
						value = value.replace("<br />", "\n");
						m_language.put(key, value);
						m_logger.info("LanguageManager: loading key " + key);
					}
					
					m_languageName = languageName;
					
					m_logger.info("LanguageManager: " + languageName + " language sucessfully loaded");
					
					return true;
				}
			}
		}
		catch (Exception e) {
			m_logger.error("LanguageManager: error loading. File: " + m_languagesFile + " language: " + languageName);
			return false;
		}
		
		m_logger.error("LanguageManager: couldn´t load " + languageName + " language");
		
		return false;
	}

	public String getString(String key) {
		if (m_language != null) {
			// Look for string in selected language
			String string = m_language.get(key);
			
			if (string != null) {
				return string;
			}
		}
	
		// Key not found, return the key itself
		m_logger.error("LanguageManager: string " + key + " not found");
		return key;
	}

	// TODO: Not compatible with HTML5
	public String getString(String key, Object... args) {
		//return String.format(getString(key), args);
		return null;
	}
}
