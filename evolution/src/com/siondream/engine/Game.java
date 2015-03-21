package com.siondream.engine;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Logger;

public class Game implements ApplicationListener {

	protected static Logger m_logger;
	protected static Settings m_settings;
	protected static LanguageManager m_languageManager;
	protected static StateManager m_stateManager;
	protected static AssetManager m_assetManager;
	protected static SpriteBatch m_batch;
	protected static SpriteBatch m_HUDBatch;
	protected static EntityManager m_entityManager;
	protected static Frustum m_frustum;
	protected static TweenManager m_tweenManager;
	protected static PlatformResolver m_platformResolver;
	protected static FPSLogger m_fps;
	
	protected static OrthographicCamera m_camera;
	protected static OrthographicCamera m_HUDCamera;
	protected static int m_virtualWidth;
	protected static int m_virtualHeight;
	private float m_aspectRatio;
	protected Rectangle m_viewport = new Rectangle(0, 0, 0, 0);
	protected Matrix4 m_inv;
	
	// Physics
	public static float ppm;
	public static float mpp;
	protected static World m_world;
	protected int m_velocityIterations;
	protected int m_positionIterations;
	private Box2DDebugRenderer m_box2DRenderer;

	@Override
	public void create() {		
		m_logger = new Logger("siondream", Logger.DEBUG);
		
		m_settings = new Settings();
		
		m_languageManager = new LanguageManager();
		
		// Asset Manager
		m_assetManager = new AssetManager();
		m_assetManager.setLoader(AnimationData.class, new AnimationLoader(new InternalFileHandleResolver()));
		m_assetManager.setLoader(TiledMap.class, new TiledMapLoader(new InternalFileHandleResolver()));
		m_assetManager.setLoader(PhysicsData.class, new PhysicsDataLoader(new InternalFileHandleResolver()));
		
		m_stateManager = new StateManager();
		
		m_batch = new SpriteBatch();
		m_HUDBatch = new SpriteBatch();
		
		// Box2D World
		m_velocityIterations = m_settings.getInt("velocityIterations", 6);
		m_positionIterations = m_settings.getInt("positionIterations", 2);
		final Vector3 gravity = m_settings.getVector("gravity", Vector3.Zero);
		m_world = new World(new Vector2(gravity.x, gravity.y),
							m_settings.getBoolean("doSleep", true));
		
		m_box2DRenderer = new Box2DDebugRenderer(m_settings.getBoolean("drawBodies", false),
												 m_settings.getBoolean("drawJoints", false),
												 m_settings.getBoolean("drawAABBs", false),
												 m_settings.getBoolean("drawInactiveBodies", false));
		
		// Relationship between pixels and world units, default is 1
		ppm = m_settings.getFloat("ppm", 1.0f);
		mpp = 1.0f / ppm;
		
		// Camera
		m_virtualWidth = m_settings.getInt("virtualWidth");
		m_virtualHeight = m_settings.getInt("virtualHeight");
		m_aspectRatio = m_settings.getFloat("aspectRatio");
		m_camera = new OrthographicCamera(m_virtualWidth * mpp, m_virtualHeight * mpp);
		m_camera.setToOrtho(true, m_virtualWidth * mpp, m_virtualHeight * mpp);
		m_camera.zoom = 1.0f;
		m_camera.position.x = 0.0f;
		m_camera.position.y = 0.0f;
		m_frustum = new Frustum();
		
		// Hud camera
		m_HUDCamera = new OrthographicCamera(m_virtualWidth, m_virtualHeight);
		m_HUDCamera.setToOrtho(true, m_virtualWidth, m_virtualHeight);
		
		// Tween engine
		m_tweenManager = new TweenManager();
		Tween.registerAccessor(Entity.class, new EntityTweener());
		Tween.registerAccessor(OrthographicCamera.class, new CameraTweener());
		
		m_entityManager = new EntityManager();
		
		// By default, no platform resolver
		m_platformResolver = null;
		
		m_fps = new FPSLogger();
	}

	@Override
	public void dispose() {
		m_batch.dispose();
		m_HUDBatch.dispose();
	}

	@Override
	public void render() {		
		float deltaT = Gdx.graphics.getDeltaTime();
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_BLEND);
		
		m_camera.update();
		m_inv = m_camera.combined.cpy();
		m_frustum.update(m_inv.inv());
		
		m_HUDCamera.update();
		
		Gdx.gl.glViewport((int) m_viewport.x,
						  (int) m_viewport.y,
						  (int) m_viewport.width,
						  (int) m_viewport.height);
		
		m_batch.setProjectionMatrix(m_camera.combined);
		m_HUDBatch.setProjectionMatrix(m_HUDCamera.combined);
		m_tweenManager.update(deltaT);
		m_world.step(deltaT, m_velocityIterations, m_positionIterations);
		//m_stateManager.update(deltaT);
		m_box2DRenderer.render(m_world, m_camera.combined);
	}

	@Override
	public void resize(int width, int height) {
		// Calculate new aspect ratio
        float aspectRatio = (float)width / (float)height;
        
        float scale = 1.0f;
        
        // Calculate the scale we need to apply and the possible crop
        if(aspectRatio > m_aspectRatio)
        {
            scale = (float)height / (float)m_virtualHeight;
            m_viewport.x = (width - m_virtualWidth * scale) * 0.5f;
        }
        else if(aspectRatio < m_aspectRatio)
        {
            scale = (float)width / (float)m_virtualWidth;
            m_viewport.y = (height - m_virtualHeight * scale) * 0.5f;
        }
        else
        {
            scale = (float)width/(float)m_virtualWidth;
        }
        
        // New witdh and  height
        m_viewport.width = (float)m_virtualWidth * scale;
        m_viewport.height = (float)m_virtualHeight * scale;
	}

	@Override
	public void pause() {
		//m_stateManager.pause();
	}

	@Override
	public void resume() {
		//m_stateManager.resume();
	}
	
	public static LanguageManager getLanguagesManager() {
		return m_languageManager;
	}
	
	public static Logger getLogger() {
		return m_logger;
	}
	
	public static StateManager getStateManager() {
		return m_stateManager;
	}
	
	public static Settings getSettings() {
		return m_settings;
	}
	
	public static AssetManager getAssetManager() {
		return m_assetManager;
	}
	
	public static SpriteBatch getSpriteBatch() {
		return m_batch;
	}
	
	public static SpriteBatch getHUDSpriteBatch() {
		return m_HUDBatch;
	}
	
	public static OrthographicCamera getCamera() {
		return m_camera;
	}
	
	public static OrthographicCamera getHUDCamera() {
		return m_HUDCamera;
	}
	
	public static Frustum getFrustum() {
		return m_frustum;
	}
	
	public static TweenManager getTweenManager() {
		return m_tweenManager;
	}
	
	public static int getVirtualWidth() {
		return m_virtualWidth;
	}
	
	public static int getVirtualHeight() {
		return m_virtualHeight;
	}
	
	public static PlatformResolver getPlatformResolver() {
		return m_platformResolver;
	}
	
	public static EntityManager getEntityManager() {
		return m_entityManager;
	}
	
	public static World getWorld() {
		return m_world;
	}
	
	public static void setPlatformResolver(PlatformResolver platformResolver) {
		m_platformResolver = platformResolver;
	}
}
