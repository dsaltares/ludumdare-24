package com.siondream.engine;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class TiledMap implements Disposable {
	// Rendering and auxiliar elements
	private static final Logger m_logger = Game.getLogger();
	private static final OrthographicCamera m_camera = Game.getCamera();
	private static final Frustum m_frustum = Game.getFrustum();
	private static final Settings m_settings = Game.getSettings();
	
	// Map properties
	private String m_name = "";
	private int m_height = 0;
	private int m_width = 0;
	private int m_tileWidth = 0;
	private int m_tileHeight = 0;
	private Array<TiledLayer> m_layers = new Array<TiledLayer>();;
	private Array<TiledObjectGroup> m_groups = new Array<TiledObjectGroup>();;
	private Array<TileSet> m_tilesets = new Array<TileSet>();;
	private HashMap<String, String> m_properties = new HashMap<String, String>();;
	private boolean m_managed = false;
	
	// Cache
	private SpriteCache m_cache = null;
	private boolean m_modified = true;
	private int m_blockWidth = 0;
	private int m_blockHeight = 0;
	private int m_numBlocksX = 0;
	private int m_numBlocksY = 0;
	private int m_tilesPerBlockX = 0;
	private int m_tilesPerBlockY = 0;
	private int[][][] m_cacheIDs = null;
	private Vector3 m_max = new Vector3(0.0f, 0.0f, 100.0f);;
	private Vector3 m_min = new Vector3(0.0f, 0.0f, 100.0f);;
	private BoundingBox m_blockBox = new BoundingBox();
	
	// Other rendering data
	private boolean[] m_drawLayer = null;
	private Matrix4 m_matrix = new Matrix4();;
	private float m_rotation = 0;
	private float m_scale = Game.mpp;
	private Vector3 m_center;
	
	public TiledMap(String tmxFile) {
		this(tmxFile, false);
	}
	
	public TiledMap(String tmxFile, boolean managed) {
		m_name = tmxFile;
		m_managed = managed;
		
		// Load map XML
		load();
		
		// By default, a cached block will have exactly the classic screen dimensions
		// Has to me multiple of the tilesize
		m_blockWidth = m_settings.getInt("blockWidth", Game.getVirtualWidth());
		m_blockHeight = m_settings.getInt("blockHeight", Game.getVirtualWidth());

		// It can also be overriden in the map properties
		m_blockWidth = Integer.parseInt(getProperty("blockWidth", Integer.toString(m_blockWidth)));
		m_blockHeight = Integer.parseInt(getProperty("blockHeight", Integer.toString(m_blockHeight)));
		
		// Calculate how many blocks do we need
		int numLayers = m_layers.size;
		m_numBlocksX = (int)Math.ceil(((float)(m_width * m_tileWidth)) / (float)m_blockWidth);
		m_numBlocksY = (int)Math.ceil(((float)(m_height * m_tileHeight)) / (float)m_blockHeight);
		m_tilesPerBlockX = (int)Math.ceil((float)m_blockWidth / (float)m_tileWidth);
		m_tilesPerBlockY = (int)Math.ceil((float)m_blockHeight / (float)m_tileHeight);
		m_cacheIDs = new int[numLayers][m_numBlocksY][m_numBlocksX];
		m_drawLayer = new boolean[numLayers];
		
		for (int z = 0; z < numLayers; ++z) {
			for (int row = 0; row < m_numBlocksY; ++row) {
				for (int column = 0; column < m_numBlocksX; ++column) {
					m_cacheIDs[z][row][column] = -1;
				}
			}
			
			m_drawLayer[z] = true;
		}

		m_center = new Vector3(m_width * m_tileWidth * 0.5f, m_height * m_tileHeight * 0.5f, 0.0f);
		
		// Debug info
		m_logger.info("TiledMap: map width " + m_width);
		m_logger.info("TiledMap: map height " + m_height);
		m_logger.info("TiledMap: tile width " + m_tileWidth);
		m_logger.info("TiledMap: tile height " + m_tileHeight);
		m_logger.info("TiledMap: block width " + m_blockWidth);
		m_logger.info("TiledMap: block height " + m_blockHeight);
		m_logger.info("TiledMap: num blocks X " + m_numBlocksX);
		m_logger.info("TiledMap: num blocks Y " + m_numBlocksY);
		m_logger.info("TiledMap: num tiles per block X " + m_tilesPerBlockX);
		m_logger.info("TiledMap: num tiles per block Y " + m_tilesPerBlockY);
	}
	
	@Override
	public void dispose() {
		if (m_cache != null) {
			m_cache.dispose();
		}
	}

	public String getName() {
		return m_name;
	}
	
	public int getHeight() {
		return m_height;
	}
	
	public int getWidth() {
		return m_width;
	}
	
	public int getTileHeight() {
		return m_tileHeight;
	}
	
	public int getTileWidth() {
		return m_tileWidth;
	}
	
	public float getRotation() {
		return m_rotation;
	}
	
	public void rotate(float f) {
		m_rotation += f;
	}
	
	public Array<TiledLayer> getLayers() {
		return m_layers;
	}
	
	public TiledLayer getLayer(int i) {
		if (i < 0 || i >= m_layers.size) {
			return null;
		}
		
		return m_layers.get(i);
	}
	
	public TiledLayer getLayer(String name) {
		int numLayers = m_layers.size;
		
		for (int i = 0; i < numLayers; ++i) {
			TiledLayer layer = m_layers.get(i);
			
			if (layer.getName().equals(name)) {
				return layer;
			}
		}
		
		return null;
	}
	
	public boolean isLayerDrawable(int layerIndex) {
		if (layerIndex < 0 || layerIndex >= m_layers.size) {
			return false;
		}
		
		return m_drawLayer[layerIndex];
	}
	
	public void setLayerDrawable(int layerIndex, boolean drawable) {
		if (layerIndex < 0 || layerIndex >= m_layers.size) {
			return;
		}
		
		m_drawLayer[layerIndex] = drawable;
	}
	
	public void setLayerDrawable(String name, boolean drawable) {
		int numLayers = m_layers.size;
		
		for (int i = 0; i < numLayers; ++i) {
			if (m_layers.get(i).getName().equals(name)) {
				setLayerDrawable(i, drawable);
				break;
			}
		}
	}
	
	public Array<TileSet> getTileSets() {
		return m_tilesets;
	}
	
	public TileSet getTileset(int i) {
		if (i < 0 || i >= m_tilesets.size) {
			return null;
		}
		
		return m_tilesets.get(i);
	}
	
	public TileSet getTileSet(String name) {
		int numTilesets = m_tilesets.size;
		
		for (int i = 0; i < numTilesets; ++i) {
			TileSet tileset = m_tilesets.get(i);
			
			if (tileset.getName().equals(name)) {
				return tileset;
			}
		}
		
		return null;
	}
	
	public Array<TiledObjectGroup> getGroups() {
		return m_groups;
	}
	
	public TiledObjectGroup getGroup(int i) {
		if (i < 0 || i >= m_groups.size) {
			return null;
		}
		
		return m_groups.get(i);
	}
	
	public TiledObjectGroup getGroup(String name) {
		int numGroups = m_groups.size;
		
		for (int i = 0; i < numGroups; ++i) {
			TiledObjectGroup group = m_groups.get(i);
			
			if (group.getName().equals(name)) {
				return group;
			}
		}
		
		return null;
	}
	
	public String getProperty(String name) {
		return getProperty(name, "");
	}
	
	public String getProperty(String name, String defaultValue) {
		String string = m_properties.get(name);
		
		if (string == null) {
			return defaultValue;
		}
		
		return string;
	}
	
	public void setProperty(String name, String value) {
		m_properties.put(name, value);
	}
	
	public void draw() {
		// If the map has been modified or hasn´t been cached before, cache it
		cache();
		
		// We render the map using the camera and our rotation
		m_matrix.setToTranslation(m_center);
		m_matrix.rotate(Vector3.Z, m_rotation);
		m_matrix.translate(m_center.mul(-1.0f));
		m_matrix.mul(m_camera.combined);
		m_cache.setProjectionMatrix(m_matrix);
		m_center.mul(-1.0f);
		
		m_cache.begin();
		
		int numLayers = m_layers.size;
		
		// Go through all the cached blocks and draw them
		for (int z = 0; z < numLayers; ++z) {
			if (m_drawLayer[z]) {
				for (int row = 0; row < m_numBlocksY; ++row) {
					for (int column = 0; column < m_numBlocksX; ++column) {
						// Set bounding box
						m_min.x = column * m_blockWidth * Game.mpp;
						m_min.y = row * m_blockHeight * Game.mpp;
						m_max.x = (column + 1) * m_blockWidth * Game.mpp;
						m_max.y = (row + 1) * m_blockHeight * Game.mpp;
						m_blockBox.set(m_min, m_max);
						
						// TODO
						// Rotate points to fix frustum culling when rotating
						
						// If the block is in the frustum, draw it (culling)
						if (m_frustum.boundsInFrustum(m_blockBox)) {
							m_cache.draw(m_cacheIDs[z][row][column]);
						}
					}
				}
			}
		}
		
		m_cache.end();
	}
	
	public boolean isModified() {
		return m_modified;
	}
	
	public void setModified() {
		m_modified = true;
	}
	
	public void cache() {
		if (m_modified) {
			
			int numLayers = m_layers.size;
			
			// Alloc memory for all the blocks (1 per potential tile)
			if (m_cache == null) {
				m_cache = new SpriteCache(m_width * m_height * numLayers, false);
				m_logger.info("TiledMap: cache created with size " + m_width * m_height * numLayers);
			}
			
			// Go over all the blocks caching them
			for (int z = 0; z < numLayers; ++z) {
				for (int row = 0; row < m_numBlocksY; ++row) {
					for (int column = 0; column < m_numBlocksX; ++column) {
						
						if (m_cacheIDs[z][row][column] == -1) {
							m_cache.beginCache();
						}
						else {
							m_cache.beginCache(m_cacheIDs[z][row][column]);
						}
						
						addBlock(z, row, column);
						
						m_cacheIDs[z][row][column] = m_cache.endCache();
					}
				}
			}
			
			m_modified = false;
		}
	}
	
	private void addBlock(int z, int row, int column) {
		TiledLayer layer = m_layers.get(z);
		TileSet tileset = null;
		TextureRegion region = null;
		int gid = 0;
		
		for (int x = 0; x < m_tilesPerBlockX; ++x) {
			for (int y = 0; y < m_tilesPerBlockY; ++y) {
				gid = layer.getTile(column * m_tilesPerBlockX + x, row * m_tilesPerBlockY + y);
				tileset = getTilesetForGID(gid);
			
				if (tileset != null) {
					region = tileset.getTile(gid);
					
					if (region != null) {
						m_cache.add(region,
									(column * m_tilesPerBlockX + x) * m_tileWidth * m_scale,
									(row * m_tilesPerBlockY + y) * m_tileHeight * m_scale,
									0.0f,
									0.0f,
									region.getRegionWidth(),
									-region.getRegionHeight(),
									m_scale,
									m_scale,
									0.0f);
					}
				}
			}
		}
	}
	
	private void load() {
		m_logger.info("TiledMap: loading from file " + m_name);
		
		try {
			// Parse xml document
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(m_name));
			
			loadBasicInfo(root);
			loadMapProperties(root);
			loadTileSets(root);
			loadLayers(root);
			loadObjectGroups(root);
			
		} catch (Exception e) {
			m_logger.error("TiledMap: error loading file " + m_name + " " + e.getMessage());
		}
	}
	
	private void loadBasicInfo(Element root) {
		m_width = Integer.parseInt(root.getAttribute("width"));
		m_height = Integer.parseInt(root.getAttribute("height"));
		m_tileWidth = Integer.parseInt(root.getAttribute("tilewidth"));
		m_tileHeight = Integer.parseInt(root.getAttribute("tileheight"));
	}
	
	private void loadMapProperties(Element root) {
		Element propertiesNode = root.getChildByName("properties");
		
		if (propertiesNode != null) {
			Array<Element> propertyNodes = propertiesNode.getChildrenByName("property");
			
			for (int i = 0; i < propertyNodes.size; ++i) {
				Element propertyNode = propertyNodes.get(i);
				String name = propertyNode.getAttribute("name");
				String value = propertyNode.getAttribute("value");
				m_properties.put(name, value);
				m_logger.info("TiledMap: loaded property " + " (" + name + " = " + value + ")");
			}
		}
	}
	
	private void loadTileSets(Element root) {
		Array<Element> tilesetNodes = root.getChildrenByName("tileset");
		
		for (int i = 0; i < tilesetNodes.size; ++i) {
			Element tilesetNode = tilesetNodes.get(i);
			
			String name = tilesetNode.getAttribute("name");
			int firstGID = Integer.parseInt(tilesetNode.getAttribute("firstgid"));
			int tileWidth = Integer.parseInt(tilesetNode.getAttribute("tilewidth"));
			int tileHeight = Integer.parseInt(tilesetNode.getAttribute("tileheight"));
			
			Element imageNode = tilesetNode.getChildByName("image");
			String image = "data/" + imageNode.getAttribute("source");
			
			TileSet tileset = null;
			
			if (m_managed) {
				tileset = new TileSet(this, name, image, tileHeight, tileWidth, firstGID);
			}
			else {
				Texture texture = new Texture(image);
				tileset = new TileSet(this, name, image, texture, tileHeight, tileWidth, firstGID);
			}
			
			m_logger.info("TiledMap: added tileset " + name);
			loadTilesetProperties(tilesetNode, tileset);
			m_tilesets.add(tileset);
		}
	}
	
	private void loadLayers(Element root) {
		Array<Element> layerNodes = root.getChildrenByName("layer");
		
		for (int i = 0; i < layerNodes.size; ++i) {
			Element layerNode = layerNodes.get(i);
			
			String name = layerNode.getAttribute("name");
			int width = Integer.parseInt(layerNode.getAttribute("width"));
			int height = Integer.parseInt(layerNode.getAttribute("height"));
			int[][] tiles = loadTiles(layerNode, width, height);
			
			TiledLayer layer = new TiledLayer(name, width, height, tiles);
			m_logger.info("TiledMap: added layer " + name);
			loadLayerProperties(layerNode, layer);
			m_layers.add(layer);
		}
	}
	
	private int[][] loadTiles(Element layerNode, int width, int height) {
		int[][] tiles = new int[height][width];
		
		Array<Element> tileNodes = layerNode.getChildrenByNameRecursively("tile");
		
		for (int n = 0; n < tileNodes.size; ++n) {
			int column = (n % width) % width;
			int row = n / width;
			tiles[row][column] = Integer.parseInt(tileNodes.get(n).getAttribute("gid"));
		}
		
		return tiles;
	}
	
	private void loadObjectGroups(Element root) {
		Array<Element> groupNodes = root.getChildrenByName("objectgroup");
		
		for (int i = 0; i < groupNodes.size; ++i) {
			Element groupNode = groupNodes.get(i);
			String name = groupNode.getAttribute("name");
			int width = Integer.parseInt(groupNode.getAttribute("width"));
			int height = Integer.parseInt(groupNode.getAttribute("height"));
			
			TiledObjectGroup group = new TiledObjectGroup(name, width, height);
			m_logger.info("TiledMap: added object group " + name);
			loadObjectGroupProperties(groupNode, group);
			m_groups.add(group);
			
			Array<Element> objectNodes = groupNode.getChildrenByName("object");
			
			for (int j = 0; j < objectNodes.size; ++j) {
				Element objectNode = objectNodes.get(j);
				TiledObject object = loadObject(objectNode);
				
				if (object != null) {
					group.add(object);
				}
			}
		}
	}
	
	private TiledObject loadObject(Element objectNode) {
		String name = objectNode.getAttribute("name", "");
		String type = objectNode.getAttribute("type", "");
		
		int x = Integer.parseInt(objectNode.getAttribute("x", "0"));
		int y = Integer.parseInt(objectNode.getAttribute("y", "0"));
		int width = Integer.parseInt(objectNode.getAttribute("width", "0"));
		int height = Integer.parseInt(objectNode.getAttribute("height", "0"));
		Vector2[] polygon = loadPolygon(objectNode);
		
		TiledObject object;
		
		if (polygon == null) {
			object = new TiledObject(name, type, x, y, width, height);
		}
		else {
			object = new TiledObject(name, type, x, y, width, height, polygon);
		}
		
		
		m_logger.info("TiledMap: added object " + name);
		loadObjectProperties(objectNode, object);
		
		return object;
	}
	
	private Vector2[] loadPolygon(Element objectNode) {
		Element polygonNode = objectNode.getChildByName("polygon");
		
		if (polygonNode == null) {
			return null;
		}
		
		String pointsString = polygonNode.getAttribute("points");
		String[] positions = pointsString.split(" ");
		Vector2[] polygon = new Vector2[positions.length];
		
		for (int i = 0; i < positions.length; ++i) {
			String[] pointString = positions[i].split(",");
			polygon[i] = new Vector2();
			polygon[i].x = Integer.parseInt(pointString[0]) * Game.mpp;
			polygon[i].y = Integer.parseInt(pointString[1]) * Game.mpp;
			m_logger.info("TiledMap: adding point to polygon (" + polygon[i].x + ", " + polygon[i].y + ")");
		}
		
		return polygon;
	}
	
	private TileSet getTilesetForGID(int gid) {
		int numTilesets = m_tilesets.size;
		
		for (int i = 0; i < numTilesets; ++i) {
			TileSet tileset = m_tilesets.get(i);
			int firstGID = tileset.getFirstGID();
			
			if (gid >= firstGID && gid < firstGID + tileset.getNumTiles()) {
				return tileset;
			}
		}
		
		return null;
	}
	
	private void loadTilesetProperties(Element tilesetNode, TileSet tileset) {
		Array<Element> propertyNodes = tilesetNode.getChildrenByName("property");
		
		for (int i = 0; i < propertyNodes.size; ++i) {
			Element propertyNode = propertyNodes.get(i);
			String name = propertyNode.getAttribute("name");
			String value = propertyNode.getAttribute("value");
			tileset.setProperty(name, value);
			m_logger.info("TiledMap: added attribute to tileset " + tileset.getName() + " (" + name + " = " + value + ")");
		}
	}
	
	private void loadLayerProperties(Element layerNode, TiledLayer layer) {
		Array<Element> propertyNodes = layerNode.getChildrenByName("property");
		
		for (int i = 0; i < propertyNodes.size; ++i) {
			Element propertyNode = propertyNodes.get(i);
			String name = propertyNode.getAttribute("name");
			String value = propertyNode.getAttribute("value");
			layer.setProperty(name, value);
			m_logger.info("TiledMap: added attribute to layer " + layer.getName() + " (" + name + " = " + value + ")");
		}
	}
	
	private void loadObjectGroupProperties(Element groupNode, TiledObjectGroup group) {
		Array<Element> propertyNodes = groupNode.getChildrenByName("property");
		
		for (int i = 0; i < propertyNodes.size; ++i) {
			Element propertyNode = propertyNodes.get(i);
			String name = propertyNode.getAttribute("name");
			String value = propertyNode.getAttribute("value");
			group.setProperty(name, value);
			m_logger.info("TiledMap: added attribute to group " + group.getName() + " (" + name + " = " + value + ")");
		}
	}
	
	private void loadObjectProperties(Element objectNode, TiledObject object) {
		Array<Element> propertyNodes = objectNode.getChildrenByName("property");
		
		for (int i = 0; i < propertyNodes.size; ++i) {
			Element propertyNode = propertyNodes.get(i);
			String name = propertyNode.getAttribute("name");
			String value = propertyNode.getAttribute("value");
			object.setProperty(name, value);
			m_logger.info("TiledMap: added attribute to object " + object.getName() + " (" + name + " = " + value + ")");
		}
	}
}
