package com.siondream.engine;

import java.util.HashMap;

import com.badlogic.gdx.utils.Array;

public class TiledObjectGroup {
	private String m_name = "";
	private int m_width = 0;
	private int m_height = 0;
	private HashMap<String, String> m_properties = new HashMap<String, String>();;
	private Array<TiledObject> m_objects = new Array<TiledObject>();;
	
	public TiledObjectGroup(String name, int width, int height) {
		m_name = name;
		m_width = width;
		m_height = height;
	}
	
	public String getName() {
		return m_name;
	}
	
	public int getWidth() {
		return m_width;
	}
	
	public int getHeight() {
		return m_height;
	}
	
	public String getProperty(String name) {
		return m_properties.get(name);
	}
	
	public void setProperty(String name, String value) {
		m_properties.put(name, value);
	}
	
	public Array<TiledObject> getObjects() {
		return m_objects;
	}
	
	public TiledObject get(String name) {
		int numObjects = m_objects.size;
		
		for (int i = 0; i < numObjects; ++i) {
			TiledObject object = m_objects.get(i);
			
			if (object.getName().equals(name)) {
				return object;
			}
		}
		
		return null;
	}
	
	public Array<TiledObject> getObjectsByName(String name) {
		Array<TiledObject> objects = new Array<TiledObject>();
		int numObjects = m_objects.size;
		
		for (int i = 0; i < numObjects; ++i) {
			TiledObject object = m_objects.get(i);
			
			if (object.getName().equals(name)) {
				objects.add(object);
			}
		}
		
		return objects;
	}
	
	public void add(TiledObject object) {
		m_objects.add(object);
	}
	
	public void remove(String name) {
		int numObjects = m_objects.size;
		
		for (int i = 0; i < numObjects; ++i) {
			TiledObject object = m_objects.get(i);
			
			if (object.getName().equals(name)) {
				m_objects.removeIndex(i);
				break;
			}
		}
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) {
	    	return false;
	    }
	    
	    if (other == this) {
	    	return true;
	    }
	    
	    if (!(other instanceof TiledObjectGroup)) {
	    	return false;
	    }
	    
	    TiledObjectGroup group = (TiledObjectGroup)other;
	    
	    return m_name.equals(group.getName());
	}
}
