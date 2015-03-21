package com.siondream.engine;

import java.util.HashMap;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class TiledObject {
	private String m_name = "";
	private String m_type = "";
	private Vector2 m_position;
	private int m_y = 0;
	private int m_width = 0;
	private int m_height = 0;
	private HashMap<String, String> m_properties = new HashMap<String, String>();
	private PolygonShape m_polygon = new PolygonShape();
	
	public TiledObject(String name, String type, int x, int y, int width, int height) {
		this(name, type, x, y, width, height, null);
	}
	
	public TiledObject(String name, String type, int x, int y, int width, int height, Vector2[] polygon) {
		m_name = name;
		m_type = type;
		m_position = new Vector2(x, y);
		m_width = width;
		m_height = height;
		
		if (polygon != null) {
			m_polygon.set(polygon);
		}
		else
		{
			m_polygon = new PolygonShape();
			m_position.x += width * 0.5f;
			m_position.y += height * 0.5f;
			m_polygon.setAsBox(width * Game.mpp * 0.5f, height * Game.mpp * 0.5f);
		}
	}
	
	public String getName() {
		return m_name;
	}
	
	public String getType() {
		return m_type;
	}
	
	public Vector2 getPosition() {
		return m_position;
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
	
	public PolygonShape getPolygon() {
		return m_polygon;
	}
	
	public void setPolygon(Vector2[] polygon) {
		m_polygon.set(polygon);
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) {
	    	return false;
	    }
	    
	    if (other == this) {
	    	return true;
	    }
	    
	    if (!(other instanceof TiledObject)) {
	    	return false;
	    }
	    
	    TiledObject object = (TiledObject)other;
	    
	    return m_name.equals(object.getName());
	}
}
