package com.siondream.engine;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class TileSet {
	private TiledMap m_map = null;
	private String m_name = "";
	private String m_textureFile = "";
	private Texture m_texture = null;
	private Array<TextureRegion> m_tiles = new Array<TextureRegion>();
	private HashMap<String, String> m_properties = new HashMap<String, String>();
	private int m_width = 0;
	private int m_height = 0;
	private int m_firstGID = 0;
	private int m_numTiles = 0;
	
	public TileSet(TiledMap map, String name, String textureFile, int tileWidth, int tileHeight, int firstGID) {
		this(map, name, textureFile, null, tileWidth, tileHeight, firstGID);
	}
	
	public TileSet(TiledMap map,
				   String name,
				   String textureFile,
				   Texture texture,
				   int tileWidth,
				   int tileHeight,
				   int firstGID) {
		
		m_map = map;
		m_name = name;
		m_textureFile = textureFile;
		m_texture = texture;
		m_width = tileWidth;
		m_height = tileHeight;
		m_firstGID = firstGID;
		m_numTiles = 0;
		
		loadRegions();
	}
	
	public String getName() {
		return m_name;
	}
	
	public String getTextureFileName() {
		return m_textureFile;
	}
	
	public TextureRegion getTile(int gid) {
		int i = gid - m_firstGID;
		
		if (i < 0 || i >= m_numTiles) {
			return null;
		}
		
		return m_tiles.get(i);
	}
	
	public int getNumTiles() {
		return m_numTiles;
	}
	
	public int getFirstGID() {
		return m_firstGID;
	}
	
	public int getTileWidth() {
		return m_width;
	}
	
	public int getTileHeight() {
		return m_height;
	}
	
	public void setTexture(Texture texture) {
		m_texture = texture;
		m_map.setModified();
		loadRegions();
	}
	
	public String getProperty(String name) {
		return m_properties.get(name);
	}
	
	public void setProperty(String name, String value) {
		m_properties.put(name, value);
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) {
	    	return false;
	    }
	    
	    if (other == this) {
	    	return true;
	    }
	    
	    if (!(other instanceof TileSet)) {
	    	return false;
	    }
	    
	    TileSet tileset = (TileSet)other;
	    
	    return m_name.equals(tileset.getName());
	}
	
	public void loadRegions() {
		m_tiles.clear();
		
		if (m_texture != null) {
			int columns = m_texture.getWidth() / m_width;
			int rows = m_texture.getHeight() / m_height;
			m_numTiles = columns * rows;
			
			for (int y = 0; y < rows; ++y) {
				for (int x = 0; x < columns; ++x) {
					TextureRegion region = new TextureRegion(m_texture, x * m_width + 2 * x , y * m_height + 2 * y, m_width, m_height);
					region.flip(false, true);
					m_tiles.add(region);
				}
			}
		}
	}
}
