package com.siondream.engine;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class AnimationData {
	private static Logger m_logger = Game.getLogger();
	private static AssetManager m_assetManager = Game.getAssetManager();
	
	final String m_name;
	private Texture m_texture = null;
	private String m_textureName = null;
	private int m_rows = 0;
	private int m_columns = 0;
	private float m_frameDuration = 0.0f;
	private IntMap<Animation> m_animations = new IntMap<Animation>();
	private Animation m_defaultAnimation = null;
	private Integer m_defaultAnimationID = 0;
	
	public AnimationData(String animationFile) {
		m_name = animationFile;
	}
	
	public String getName() {
		return m_name;
	}
	
	public int getRows() {
		return m_rows;
	}
	
	public int getColumns() {
		return m_columns;
	}
	
	public float frameDuration() {
		return m_frameDuration;
	}
	
	public Animation getAnimation(String animationName) {
		return getAnimation(IDGenerator.getID(animationName));
	}
	
	public Animation getAnimation(int animationID) {
		Animation animation = m_animations.get(animationID);
		
		if (animation == null) {
			m_logger.info("Animation: " + IDGenerator.getString(animationID) + " not found returning default");
			return m_defaultAnimation;
		}
		
		return animation;
	}
	
	public String getTextureName() {
		return m_textureName;
	}
	
	public Texture getTexture() {
		return m_texture;
	}
	
	public int getNumAnimations() {
		return m_animations.size;
	}
	
	public Animation getDefaultAnimation() {
		return m_defaultAnimation;
	}
	
	public int getDefaultAnimationID() {
		return m_defaultAnimationID;
	}
	
	public void loadBasicInfo() {
		m_logger.info("AnimationData: loading from file " + m_name);
		
		try {
			// Parse xml document
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(m_name));

			// Get animation attributes
			m_textureName = root.getAttribute("name");
			m_rows = Integer.parseInt(root.getAttribute("rows"));
			m_columns = Integer.parseInt(root.getAttribute("columns"));
			m_frameDuration = Float.parseFloat(root.getAttribute("frameDuration"));
			
		} catch (Exception e) {
			m_logger.error("AnimationData: error loading file " + m_name + " " + e.getMessage());
		}
	}
	
	public void loadAnimations() {
		m_logger.info("AnimationData: loading animations from file " + m_name);
		
		// Retrieve texture
		m_texture = m_assetManager.get(m_textureName, Texture.class);
		
		try {
			// Parse xml document
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(m_name));
			
			// Go over animation nodes
			Array<Element> animationNodes = root.getChildrenByName("animation");
			
			for (int i = 0; i < animationNodes.size; ++i) {
				// Get animation node
				Element animationNode = animationNodes.get(i);
				String name = animationNode.getAttribute("name");
				String frames = animationNode.getAttribute("frames");
				int playMode = getPlayMode(animationNode.get("mode", "normal"));
				Integer id = IDGenerator.getID(name);
				
				Animation animation = new Animation(m_frameDuration, getAnimationFrames(frames), playMode);
				m_animations.put(id, animation);
				
				m_logger.info("AnimationData: " + m_name + " loaded animation " + name);
				
				if (i == 0) {
					m_defaultAnimation = animation;
					m_defaultAnimationID = id;
				}
			}
			
		} catch (Exception e) {
			m_logger.error("AnimationData: error loading file " + m_name + " " + e.getMessage());
		}	
	}
	
	private int getPlayMode(String mode) {
		if (mode.equals("normal")) {
			return Animation.NORMAL; 
		}
		else if (mode.equals("loop")) {
			return Animation.LOOP;
		}
		else if (mode.equals("loop_pingpong")) {
			return Animation.LOOP_PINGPONG;
		}
		else if (mode.equals("loop_random")) {
			return Animation.LOOP_RANDOM;
		}
		else if (mode.equals("loop_reversed")) {
			return Animation.LOOP_REVERSED;
		}
		else if (mode.equals("reversed")) {
			return Animation.REVERSED;
		}
		else {
			return Animation.NORMAL;
		}
	}
	
	private ArrayList<TextureRegion> getAnimationFrames(String frames) {
		ArrayList<TextureRegion> regions = new ArrayList<TextureRegion>();
		
		if (frames != null) {
			String[] framesArray = frames.split(",");
			int numFrames = framesArray.length;
			int width = m_texture.getWidth() / m_columns;
			int height = m_texture.getHeight() / m_rows;
			
			for (int i = 0; i < numFrames; ++i) {
				int frame = Integer.parseInt(framesArray[i]);
				int x = ((frame % m_columns) % m_columns) * width;
				int y = (frame / m_columns) * height;
				
				TextureRegion region = new TextureRegion(m_texture, x, y, width, height);
				region.flip(false, true);
				regions.add(region);
			}
		}
		
		return regions;
	}
}
