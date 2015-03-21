package com.siondream.engine;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class Settings {
	private static final Logger m_logger = Game.getLogger();
	
	private String m_settingsFile;
	
	private HashMap<String, String> m_strings = new HashMap<String, String>();
	private HashMap<String, Float> m_floats = new HashMap<String, Float>();
	private HashMap<String, Integer> m_ints = new HashMap<String, Integer>();
	private HashMap<String, Boolean> m_booleans = new HashMap<String, Boolean>();
	private HashMap<String, Vector3> m_vectors = new HashMap<String, Vector3>();
	
	public Settings() {
		this("data/settings.xml");
	}
	
	public Settings(String settings) {
		m_settingsFile = settings;
		loadSettings();
	}
	
	public String getString(String key) {
		return getString(key, "");
	}
	
	public String getString(String key, String defaultValue) {
		String string = m_strings.get(key);
		
		if (string != null) {
			return string;
		}
		
		return defaultValue;
	}
	
	public float getFloat(String key) {
		return getFloat(key, 0.0f);
	}
	
	public float getFloat(String key, float defaultValue) {
		Float f = m_floats.get(key);
		
		if (f != null) {
			return f;
		}
		
		return defaultValue;
	}
	
	public int getInt(String key) {
		return getInt(key, 0);
	}
	
	public int getInt(String key, int defaultValue) {
		Integer i = m_ints.get(key);
		
		if (i != null) {
			return i;
		}
		
		return defaultValue;
	}
	
	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		Boolean b = m_booleans.get(key);
		
		if (b != null) {
			return b;
		}
		
		return defaultValue;
	}
	
	public Vector3 getVector(String key) {
		return getVector(key, Vector3.Zero.cpy());
	}
	
	public Vector3 getVector(String key, Vector3 defaultValue) {
		Vector3 v = m_vectors.get(key);
		
		if (v != null) {
			return new Vector3(v);
		}
		
		return defaultValue;
	}
	
	public void setString(String key, String value) {
		m_strings.put(key, value);
	}
	
	public void setFloat(String key, float value) {
		m_floats.put(key, value);
	}
	
	public void setInt(String key, int value) {
		m_ints.put(key, value);
	}
	
	public void setBoolean(String key, boolean value) {
		m_booleans.put(key, value);
	}
	
	public void setVector(String key, Vector3 value) {
		m_vectors.put(key, new Vector3(value));
	}
	
	public String getFile() {
		return m_settingsFile;
	}
	
	public void setFile(String settingsFile) {
		m_settingsFile = settingsFile;
	}
	
	public void loadSettings() {
		loadSettings(true);
	}
	
	public void loadSettings(boolean tryExternal) {
		m_logger.info("Settings: loading file " + m_settingsFile);
		
		try {
			FileHandle fileHandle = null;
			
			if (tryExternal &&
				Gdx.app.getType() != Application.ApplicationType.WebGL &&
				Gdx.files.external(m_settingsFile).exists()) {
				fileHandle = Gdx.files.external(m_settingsFile);
				m_logger.info("Settings: loading as external file");
			}
			else {
				fileHandle = Gdx.files.internal(m_settingsFile);
				m_logger.info("Settings: loading as internal file");
			}
			
			XmlReader reader = new XmlReader();
			Element root = reader.parse(fileHandle);

			// Load strings
			m_strings.clear();
			Array<Element> stringNodes = root.getChildrenByName("string");
			
			for (int i = 0; i < stringNodes.size; ++i) {
				Element stringNode = stringNodes.get(i);
				String key = stringNode.getAttribute("key");
				String value = stringNode.getAttribute("value");
				m_strings.put(key, value);
				m_logger.info("Settings: loaded string " + key + " = " + value);
			}
			
			// Load floats
			m_floats.clear();
			Array<Element> floatNodes = root.getChildrenByName("float");
			
			for (int i = 0; i < floatNodes.size; ++i) {
				Element floatNode = floatNodes.get(i);
				String key = floatNode.getAttribute("key");
				Float value = Float.parseFloat(floatNode.getAttribute("value"));
				m_floats.put(key, value);
				m_logger.info("Settings: loaded float " + key + " = " + value);
			}
			
			// Load ints
			m_ints.clear();
			Array<Element> intNodes = root.getChildrenByName("int");
			
			for (int i = 0; i < intNodes.size; ++i) {
				Element intNode = intNodes.get(i);
				String key = intNode.getAttribute("key");
				Integer value = Integer.parseInt(intNode.getAttribute("value"));
				m_ints.put(key, value);
				m_logger.info("Settings: loaded int " + key + " = " + value);
			}
			
			// Load booleans
			m_booleans.clear();
			Array<Element> boolNodes = root.getChildrenByName("bool");
			
			for (int i = 0; i < boolNodes.size; ++i) {
				Element boolNode = boolNodes.get(i);
				String key = boolNode.getAttribute("key");
				Boolean value = Boolean.parseBoolean(boolNode.getAttribute("value"));
				m_booleans.put(key, value);
				m_logger.info("Settings: loaded boolean " + key + " = " + value);
			}
			
			// Load vectors
			m_vectors.clear();
			Array<Element> vectorNodes = root.getChildrenByName("vector");
			
			for (int i = 0; i < vectorNodes.size; ++i) {
				Element vectorNode = vectorNodes.get(i);
				String key = vectorNode.getAttribute("key");
				Float x = Float.parseFloat(vectorNode.getAttribute("x"));
				Float y = Float.parseFloat(vectorNode.getAttribute("y"));
				Float z = Float.parseFloat(vectorNode.getAttribute("z"));
				m_vectors.put(key, new Vector3(x, y, z));
				m_logger.info("Settings: loaded vector " + key + " = (" + x + ", " + y + ", " + z + ")");
			}
			
			m_logger.info("Settings: successfully finished loading settings");
		}
		catch (Exception e) {
			m_logger.error("Settings: error loading file: " + m_settingsFile + " " + e.getMessage());
		}
	}
	
	public void saveSettings() {
		if (Gdx.app.getType() != Application.ApplicationType.WebGL) {
			m_logger.info("Settings: saving file " + m_settingsFile);
			
			try {
				StringWriter writer = new StringWriter();
				XmlWriter xml = new XmlWriter(writer);
				
				// Create root
				xml = xml.element("settings");
				
				// Create string nodes
				for (Entry<String, String> entry : m_strings.entrySet()) {
					xml = xml.element("string");
					xml.attribute("key", entry.getKey());
					xml.attribute("value", entry.getValue());
					xml = xml.pop();
				}
				
				// Create float nodes
				for (Entry<String, Float> entry : m_floats.entrySet()) {
					xml = xml.element("float");
					xml.attribute("key", entry.getKey());
					xml.attribute("value", entry.getValue());
					xml = xml.pop();
				}
				
				// Create int nodes
				for (Entry<String, Integer> entry : m_ints.entrySet()) {
					xml = xml.element("int");
					xml.attribute("key", entry.getKey());
					xml.attribute("value", entry.getValue());
					xml = xml.pop();
				}
				
				// Create boolean nodes
				for (Entry<String, Boolean> entry : m_booleans.entrySet()) {
					xml = xml.element("bool");
					xml.attribute("key", entry.getKey());
					xml.attribute("value", entry.getValue());
					xml = xml.pop();
				}
				
				// Create vector nodes
				for (Entry<String, Vector3> entry : m_vectors.entrySet()) {
					xml = xml.element("vector");
					xml.attribute("key", entry.getKey());
					Vector3 vector = entry.getValue();
					xml.attribute("x", vector.x);
					xml.attribute("y", vector.y);
					xml.attribute("z", vector.z);
					xml = xml.pop();
				}
				
				xml = xml.pop();
	
				Gdx.files.external(m_settingsFile).writeString(writer.toString(), true);
				m_logger.info("Settings: successfully saved");
			}
			catch (Exception e) {
				m_logger.error("Settings: error saving file " + m_settingsFile);
			}
		}
		else {
			m_logger.error("Settings: saving feature not supported in HTML5");
		}
	}
}
