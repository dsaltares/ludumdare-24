package com.siondream.engine;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;

public class AnimationComponent extends Component {
	// Vertices size
	private static final int VERTEX_SIZE = 2 + 1 + 2;
	private static final int SPRITE_SIZE = 4 * VERTEX_SIZE;
	
	// Events
	public static final int AnimationFinished = IDGenerator.getID("AnimationFinished");
	
	// Common data
	private static final Logger m_logger = Game.getLogger();
	private static final AssetManager m_assetManager = Game.getAssetManager();
	private static final SpriteBatch m_batch = Game.getSpriteBatch();
	
	// Basic data
	private String m_file = null;
	private AnimationData m_data = null;
	
	// Geometry
	private TextureRegion m_frame = null;
	private final float[] m_vertices = new float[SPRITE_SIZE];
	private Color m_color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
	private Vector2 m_size = new Vector2(0.0f, 0.0f);
	private Vector2 m_origin = Vector2.Zero.cpy();
	private boolean m_dirty = true;
	
	// Cached trasform (to avoid vertex recalculations)
	private Vector3 m_position = new Vector3(Vector3.Zero);
	private float m_scale = 0.0f;
	private float m_rotation = 0.0f;
	
	// Current state
	private Integer m_animationID = 0;
	private Animation m_animation = null;
	private float m_time = 0.0f;
	private boolean m_playing = true;
	private boolean m_flipX = false;
	private boolean m_flipY = true;
	
	// Frustum culling
	protected Frustum m_frustum = Game.getFrustum();
	protected BoundingBox m_bbox = new BoundingBox();
	protected Vector3 m_max = new Vector3(0.0f, 0.0f, 0.0f);
	protected Vector3 m_min = new Vector3(0.0f, 0.0f, 0.0f);
	
	public AnimationComponent(Entity entity, String file) {
		super(entity, "AnimationComponent", 5);
		
		m_file = file;
		m_scale = Game.mpp;
		setColor(m_color);
		
		m_assetManager.load(m_file, AnimationData.class);
	}
	
	public String getName() {
		return m_name;
	}
	
	public Color getColor() {
		return m_color;
	}
	
	public void setColor(Color tint) {
		float color = tint.toFloatBits();
		m_vertices[C1] = color;
		m_vertices[C2] = color;
		m_vertices[C3] = color;
		m_vertices[C4] = color;
	}
	
	public Vector2 getSize() {
		return m_size;
	}
	
	@Override
	public void dispose() {
		//m_assetManager.unload(m_file);
		m_animation = null;
		m_data = null;
		m_file = null;
		reset();
	}

	@Override
	public void update(float deltaT) {
		
		if (m_type == IDGenerator.getID("item")) {
			m_logger.error("Anim: Updating rock!");
		}
		
		if (m_data != null) {
			updateState();
			updateAnimation(deltaT);
			applyTransform();
			computeVertices();
			draw();
		}
		else {
			m_logger.error("AnimationComponent: trying to draw null data " + m_file);
		}
	}
	
	@Override
	public void reset() {
		m_flipX = false;
		m_flipY = true;
	}
	
	@Override
	public void fetchAssets() {
		// Assign resources
		m_data = m_assetManager.get(m_file, AnimationData.class);
		
		// Get entity state
		m_animationID = m_entity.getState();
		
		// Select animation
		m_animation = m_data.getAnimation(m_animationID);
		
		// Set region
		TextureRegion region = m_animation.getKeyFrame(m_time);
		setRegion(region);
	}

	@Override
	public void onMessage(Component sender, int type, Object data) {
		// Doesn´t listen to any messages for now
	}

	@Override
	public Array<Integer> getDependencies() {
		return new Array<Integer>();
	}
	
	private void updateState() {
		// Fetch entity state
		int state = m_entity.getState();
		
		// Check whether we need to change animation or not
		if (state != m_animationID) {
			// Set animation
			m_animationID = state;
			
			// Fetch animation
			m_animation = m_data.getAnimation(m_animationID);
			
			// Restart timer
			m_time = 0.0f;
		}
	}
	
	private void updateAnimation(float deltaT) {
		if (m_animation != null && m_playing) {
			// Update the time
			m_time += deltaT;
			
			// Update the current frame
			setRegion(m_animation.getKeyFrame(m_time));

			if (m_animation.isAnimationFinished(m_time)) {
				// Send event
				m_entity.onMessage(this, AnimationFinished, Integer.valueOf(m_animationID));
			}
		}
	}
	
	private void applyTransform() {
		// Trasform (position, scale, rotation)
		Vector3 position = m_entity.getPosition();
		float rotation = m_entity.getRotation();
		float scale = m_entity.getScale();
		
		if (!m_position.equals(position) || m_rotation != rotation || m_scale != scale) {
			m_position.set(position);
			m_scale = scale * Game.mpp;
			m_rotation = rotation;
			m_dirty = true;
		}
	}
	
	private void draw() {
		// Frustum culling
		m_min.x = m_position.x - m_origin.x;
		m_min.y = m_position.y + m_origin.y;
		m_min.z = m_position.z;
		m_max.x = m_position.x + m_size.x - m_origin.x;
		m_max.y = m_position.y - m_size.y + m_origin.y;
		m_max.z = m_position.z;
		
		m_bbox.set(m_min, m_max);
		
		if (m_frustum.boundsInFrustum(m_bbox)) {
			// Draw current frame
			m_batch.draw(m_data.getTexture(), m_vertices, 0, SPRITE_SIZE);			
		}
	}
	
	public void setRegion (TextureRegion region) {
		m_frame = region;
		
		if (m_frame != null) {			
			// Set UV coordinates in the texture
			m_vertices[U1] = m_frame.getU();
			m_vertices[V1] = m_frame.getV2();
			m_vertices[U2] = m_frame.getU();
			m_vertices[V2] = m_frame.getV();
			m_vertices[U3] = m_frame.getU2();
			m_vertices[V3] = m_frame.getV();
			m_vertices[U4] = m_frame.getU2();
			m_vertices[V4] = m_frame.getV2();
			
			// Flip to use an y-down system
			setFlip();
			
			// Update size
			m_size.x = m_frame.getRegionWidth();
			m_size.y = m_frame.getRegionHeight();
			
			// Update center
			m_origin.x = m_size.x / 2.0f;
			m_origin.y = m_size.y / 2.0f;
		}
	}
	
	private void computeVertices() {
		// If we have recently applied a transform, we need to recompute the vertices
		if (m_dirty) {
			m_dirty = false;
			
			// Apply translation
			float localX = -m_origin.x;
			float localY = -m_origin.y;
			float localX2 = localX + m_size.x;
			float localY2 = localY + m_size.y;
			
			// Apply scale
			if (m_scale != 1) {
				localX *= m_scale;
				localY *= m_scale;
				localX2 *= m_scale;
				localY2 *= m_scale;
			}
			
			// Apply rotation
			if (m_rotation != 0) {
				final float cos = MathUtils.cosDeg(m_rotation);
				final float sin = MathUtils.sinDeg(m_rotation);
				final float localXCos = localX * cos;
				final float localXSin = localX * sin;
				final float localYCos = localY * cos;
				final float localYSin = localY * sin;
				final float localX2Cos = localX2 * cos;
				final float localX2Sin = localX2 * sin;
				final float localY2Cos = localY2 * cos;
				final float localY2Sin = localY2 * sin;

				final float x1 = localXCos - localYSin + m_position.x;
				final float y1 = localYCos + localXSin + m_position.y;
				m_vertices[X1] = x1;
				m_vertices[Y1] = y1;

				final float x2 = localXCos - localY2Sin + m_position.x;
				final float y2 = localY2Cos + localXSin + m_position.y;
				m_vertices[X2] = x2;
				m_vertices[Y2] = y2;

				final float x3 = localX2Cos - localY2Sin + m_position.x;
				final float y3 = localY2Cos + localX2Sin + m_position.y;
				m_vertices[X3] = x3;
				m_vertices[Y3] = y3;

				m_vertices[X4] = x1 + (x3 - x2);
				m_vertices[Y4] = y3 - (y2 - y1);
			}

			else {
				final float x1 = localX + m_position.x;
				final float y1 = localY + m_position.y;
				final float x2 = localX2 + m_position.x;
				final float y2 = localY2 + m_position.y;

				m_vertices[X1] = x1;
				m_vertices[Y1] = y1;

				m_vertices[X2] = x1;
				m_vertices[Y2] = y2;

				m_vertices[X3] = x2;
				m_vertices[Y3] = y2;

				m_vertices[X4] = x2;
				m_vertices[Y4] = y1;
			}
		}
	}
	
	public void flip (boolean x, boolean y) {
		m_flipX = x;
		m_flipY = y;
	}
	
	public boolean getFlipX() {
		return m_flipX;
	}
	
	public boolean getFlipY() {
		return m_flipY;
	}
	
	private void setFlip() {
		m_frame.flip(m_flipX, m_flipY);
		
		if (m_flipX) {
			float u = m_vertices[U1];
			float u2 = m_vertices[U3];
			m_frame.setU(u);
			m_frame.setU2(u2);
			m_vertices[U1] = u2;
			m_vertices[U2] = u2;
			m_vertices[U3] = u;
			m_vertices[U4] = u;
		}
		if (m_flipY) {
			float v = m_vertices[V2];
			float v2 = m_vertices[V1];
			m_frame.setV(v);
			m_frame.setV2(v2);
			m_vertices[V1] = v;
			m_vertices[V2] = v2;
			m_vertices[V3] = v2;
			m_vertices[V4] = v;
		}
	}
	
	// Constant to help with the vertices indices
	static private final int X1 = 0;
	static private final int Y1 = 1;
	static private final int C1 = 2;
	static private final int U1 = 3;
	static private final int V1 = 4;
	static private final int X2 = 5;
	static private final int Y2 = 6;
	static private final int C2 = 7;
	static private final int U2 = 8;
	static private final int V2 = 9;
	static private final int X3 = 10;
	static private final int Y3 = 11;
	static private final int C3 = 12;
	static private final int U3 = 13;
	static private final int V3 = 14;
	static private final int X4 = 15;
	static private final int Y4 = 16;
	static private final int C4 = 17;
	static private final int U4 = 18;
	static private final int V4 = 19;
}
