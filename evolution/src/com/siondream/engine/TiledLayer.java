package com.siondream.engine;

import java.util.HashMap;

public class TiledLayer {
	private String m_name;
	private HashMap<String, String> m_properties = new HashMap<String, String>(); 
	private int[][] m_tiles;
	private int m_width;
	private int m_height;
	
	public TiledLayer(String name, int width, int height, int[][] tiles) {
		m_name = name;
		m_width = width;
		m_height = height;
		m_tiles = tiles;
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
	
	public int getTile(int column, int row) {
		if (column < 0 || column >= m_width || row < 0 || row >= m_height) {
			return -1;
		}
		
		return m_tiles[row][column];
	}
	
	public void setTile(int x, int y, int tile) {
		if (x >= 0 && x < m_width && y >= 0 && y < m_height) {
			m_tiles[y][x] = tile;
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
	    
	    if (other instanceof TiledLayer) {
	    	TiledLayer layer = (TiledLayer)other;
	 	    return m_name.equals(layer.getName());
	    }
	    else if (other instanceof String) {
	    	String string = (String)other;
	    	return m_name.equals(string);
	    }
	   
	    return false;
	}
}
