package com.siondream.evolution;


import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.siondream.engine.AnimationComponent;
import com.siondream.engine.AnimationData;
import com.siondream.engine.CameraTweener;
import com.siondream.engine.Entity;
import com.siondream.engine.Game;
import com.siondream.engine.IDGenerator;
import com.siondream.engine.PhysicsComponent;
import com.siondream.engine.PhysicsData;
import com.siondream.engine.Settings;
import com.siondream.engine.State;
import com.siondream.engine.TiledMap;
import com.siondream.engine.TiledObject;
import com.siondream.engine.TiledObjectGroup;

public class StateGame extends State implements ContactListener {

	public enum State { Loading,
						LevelStart,
						LevelCompleted,
						GameOver,
						Running,
						ResetLevel };

	// Events
	public static final int BeginContact = IDGenerator.getID("BeginContact");
	public static final int EndContact = IDGenerator.getID("EndContact");
	public static final int OnPlayerDeath = IDGenerator.getID("onPlayerDeath");
	public static final int LevelFinished = IDGenerator.getID("levelFinish");
	public static final int Fall = IDGenerator.getID("fall");
	
	// Actor types
	public static final int Caveman = IDGenerator.getID("caveman");
	public static final int Enemy = IDGenerator.getID("enemy");
	public static final int Item = IDGenerator.getID("item");
	public static final int Ammo = IDGenerator.getID("ammo");
	
	// States
	private final int m_erase = IDGenerator.getID("erase");
	private final int m_playerControllerID = IDGenerator.getID("PlayerController");
	
	// System
	private static OrthographicCamera m_camera = Game.getCamera();
	private static World m_world = Game.getWorld();
	private static SpriteBatch m_HUDBatch = Game.getHUDSpriteBatch();
	
	// Settings
	private final float m_titleTime = m_settings.getFloat("titleTime", 1.5f);
	private final float m_cameraMaxDistance = m_settings.getFloat("cameraMaxDistance", 5.0f);
	private final float m_cameraTweenTime = m_settings.getFloat("cameraTweenTime", 5.0f);
	private final float m_cameraOffsetY = m_settings.getFloat("cameraOffsetY", -3.0f);
	private final Vector3 m_ammoHUDPos = m_settings.getVector("ammoHUDPos", Vector3.Zero);
	private final Vector3 m_ammoHUDTextPos = m_settings.getVector("ammoHUDTextPos", Vector3.Zero);
	
	// Data
	private State m_state;
	private TiledMap m_map;
	private Entity m_caveman;
	private TextureRegion m_levelStart;
	private TextureRegion m_levelCompleted;
	private TextureRegion m_gameOver;
	private TextureRegion m_ammoHUD;
	private BitmapFont m_HUDFont;
	private float m_titleCounter = m_titleTime;
	private Array<Body> m_bodies = new Array<Body>();
	private Music m_music;
	
	private Array<Entity> m_enemies = new Array<Entity>();
	public static Array<Entity> m_items = new Array<Entity>();
	public Array<Entity> m_ammos = new Array<Entity>();
	
	
	public StateGame() {
		super("StateGame");
		m_state = State.Loading;
		
		m_world.setContactListener(this);
	}
	
	@Override
	public void load() {
		super.load();
		
		m_assetManager.load("data/level1.tmx", TiledMap.class);
		m_assetManager.load("data/enemy.xml", AnimationData.class);
		m_assetManager.load("data/enemy_physics.xml", PhysicsData.class);
		m_assetManager.load("data/rock.xml", AnimationData.class);
		m_assetManager.load("data/rock_physics.xml", PhysicsData.class);
		m_assetManager.load("data/ammo.xml", AnimationData.class);
		m_assetManager.load("data/ammo_physics.xml", PhysicsData.class);
		m_assetManager.load("data/levelstart.png", Texture.class);
		m_assetManager.load("data/levelcompleted.png", Texture.class);
		m_assetManager.load("data/gameover.png", Texture.class);
		m_assetManager.load("data/ammoHUD.png", Texture.class);
		m_assetManager.load("data/music.ogg", Music.class);
		
		BitmapFontLoader.BitmapFontParameter fontParameters = new BitmapFontLoader.BitmapFontParameter();
		fontParameters.flip = true;
		m_assetManager.load("data/HUDFont.fnt", BitmapFont.class, fontParameters);
		
		m_caveman = m_entityManager.obtain();
		m_caveman.setType(Caveman);
		m_caveman.addComponent(new AnimationComponent(m_caveman, "data/caveman.xml"));
		m_caveman.addComponent(new PhysicsComponent(m_caveman, "data/caveman_physics.xml"));
		m_caveman.addComponent(new PlayerController(m_caveman));
		m_caveman.batch();
		
		m_state = State.Loading;
		m_titleCounter = m_titleTime;
	}
	
	@Override
	public void dispose() {
		super.dispose();

		m_assetManager.unload("data/level1.tmx");
		m_assetManager.unload("data/enemy.xml");
		m_assetManager.unload("data/enemy_physics.xml");
		m_assetManager.unload("data/rock.xml");
		m_assetManager.unload("data/rock_physics.xml");
		m_assetManager.unload("data/levelstart.png");
		m_assetManager.unload("data/levelcompleted.png");
		m_assetManager.unload("data/gameover.png");
		m_assetManager.unload("data/ammo.xml");
		m_assetManager.unload("data/ammo_physics.xml");
		m_assetManager.unload("data/ammoHUD.png");
		m_assetManager.unload("data/HUDFont.fnt");
		m_assetManager.unload("data/music.ogg");
		
		m_map = null;
		m_levelStart = null;
		m_levelCompleted = null;
		m_gameOver = null;
		m_HUDFont = null;
		m_ammoHUD = null;
		
		// Delete entities
		m_entityManager.free(m_enemies);
		m_entityManager.free(m_items);
		m_entityManager.free(m_ammos);
		m_entityManager.clear();
		m_caveman = null;
		
		m_enemies.clear();
		m_items.clear();
		m_ammos.clear();
		
		deleteAllBodies();
	}
	
	@Override
	public void finishLoading() {
		super.finishLoading();
		
		m_map = m_assetManager.get("data/level1.tmx", TiledMap.class);
		m_map.setLayerDrawable("Capa 3", false);
		
		m_levelStart = new TextureRegion(m_assetManager.get("data/levelstart.png", Texture.class));
		m_levelCompleted = new TextureRegion(m_assetManager.get("data/levelcompleted.png", Texture.class));
		m_gameOver = new TextureRegion(m_assetManager.get("data/gameover.png", Texture.class));
		m_ammoHUD = new TextureRegion(m_assetManager.get("data/ammoHUD.png", Texture.class));
		m_HUDFont = m_assetManager.get("data/HUDFont.fnt", BitmapFont.class);
		
		m_music = m_assetManager.get("data/music.ogg", Music.class);
		m_music.setLooping(true);
		m_music.setVolume(0.7f);
		m_music.play();
		
		m_levelStart.flip(false, true);
		m_levelCompleted.flip(false, true);
		m_gameOver.flip(false, true);
		m_ammoHUD.flip(false, true);
		
		loadLevelBodies();
		loadLevelEvents();
		createEnemies();
		createAmmo();
		
		m_caveman.onMessage(null, PhysicsComponent.DisablePhysics, null);
		Vector2 startPos = m_map.getGroup("Objects").get("playerStart").getPosition();
		m_caveman.setPosition(startPos.x * Game.mpp, startPos.y * Game.mpp, 11.0f);
		m_caveman.onMessage(null, PhysicsComponent.EnablePhysics, null);
		
		Vector2 cameraPos = m_map.getGroup("Objects").get("cameraPos").getPosition();
		m_camera.position.x = cameraPos.x * Game.mpp;
		m_camera.position.y = cameraPos.y * Game.mpp;
	}
	
	@Override
	public void update(float deltaT) {
		
		switch (m_state) {
		case Loading:
			updateLoading(deltaT);
			break;
		case LevelStart:
			updateLevelStart(deltaT);
			break;
		case LevelCompleted:
			updateLevelCompleted(deltaT);
			break;
		case GameOver:
			updateGameOver(deltaT);
			break;
		case Running:
			updateRunning(deltaT);
			break;
		case ResetLevel:
			updateResetLevel(deltaT);
			break;
		}
	}
	
	public void updateLoading(float deltaT) {
		if (m_assetManager.update()) {
			finishLoading();
			m_state = State.LevelStart;
			m_logger.info("StateMenu: finished loading");
		}
	}
	
	public void updateLevelStart(float deltaT) {
		m_titleCounter -= deltaT;
		
		if (m_titleCounter < 0) {
			m_state = State.Running;
			m_titleCounter = m_titleTime;
		}
		
		m_map.draw();
		
		m_HUDBatch.begin();
		m_HUDBatch.draw(m_levelStart,
						(Game.getVirtualWidth() - m_levelStart.getRegionWidth()) * 0.5f,
						(Game.getVirtualHeight() + m_levelStart.getRegionHeight()) * 0.5f);
		m_HUDBatch.end();
	}
	
	public void updateLevelCompleted(float deltaT) {
		m_titleCounter -= deltaT;
		
		if (m_titleCounter < 0) {
			m_stateManager.changeState("StateMenu");
			m_titleCounter = m_titleTime;
		}
		
		m_map.draw();
		
		m_HUDBatch.begin();
		m_HUDBatch.draw(m_levelCompleted,
						(Game.getVirtualWidth() - m_levelCompleted.getRegionWidth()) * 0.5f,
						(Game.getVirtualHeight() + m_levelCompleted.getRegionHeight()) * 0.5f);
		m_HUDBatch.end();
	}
	
	public void updateGameOver(float deltaT) {
		m_titleCounter -= deltaT;
		
		if (m_titleCounter < 0) {
			m_state = State.ResetLevel;
			m_titleCounter = m_titleTime;
		}
		
		m_map.draw();
		
		m_HUDBatch.begin();
		m_HUDBatch.draw(m_gameOver,
						(Game.getVirtualWidth() - m_gameOver.getRegionWidth()) * 0.5f,
						(Game.getVirtualHeight() + m_gameOver.getRegionHeight()) * 0.5f);
		m_HUDBatch.end();
	}
	
	public void updateRunning(float deltaT) {
		updateCamera();
		
		// Throw object
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			PlayerController controller = (PlayerController)m_caveman.getComponent(IDGenerator.getID("PlayerController"));
			controller.throwItem();
		}
		
		m_map.draw();
		m_batch.begin();
		m_entityManager.update(deltaT);
		m_batch.end();
		
		m_HUDBatch.begin();
		m_HUDBatch.draw(m_ammoHUD, m_ammoHUDPos.x, m_ammoHUDPos.y);
		m_HUDFont.draw(m_HUDBatch,
					   "x  " + ((PlayerController)m_caveman.getComponent(m_playerControllerID)).getAmmo(),
					   m_ammoHUDTextPos.x,
					   m_ammoHUDTextPos.y);
		m_HUDBatch.end();
		
		for (int i = 0; i < m_enemies.size;) {
			Entity enemy = m_enemies.get(i);
			
			if (enemy.getState() == m_erase) {
				m_entityManager.free(enemy);
				m_enemies.removeIndex(i);
			}
			else {
				++i;
			}
		}
		
		for (int i = 0; i < m_items.size;) {
			Entity item = m_items.get(i);
			
			if (item.getState() == m_erase) {
				m_entityManager.free(item);
				m_items.removeIndex(i);
			}
			else {
				++i;
			}
		}
		
		for (int i = 0; i < m_ammos.size;) {
			Entity ammo = m_ammos.get(i);
			
			if (ammo.getState() == m_erase) {
				m_entityManager.free(ammo);
				m_ammos.removeIndex(i);
			}
			else {
				++i;
			}
		}
	}
	
	public void updateResetLevel(float deltaT) {
		resetLevel();
	}

	private void loadLevelBodies() {
		TiledObjectGroup group = m_map.getGroup("Collisions");
		
		Array<TiledObject> objects = group.getObjects();
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 1.0f;
		fixtureDef.restitution = 0.0f;
		
		for (int i = 0; i < objects.size; ++i) {
			TiledObject object = objects.get(i);
			Body body = m_world.createBody(bodyDef);
			m_bodies.add(body);
			body.setTransform(object.getPosition().cpy().mul(Game.mpp), 0.0f);
			fixtureDef.shape = object.getPolygon();
			body.createFixture(fixtureDef);
		}
	}
	
	private void loadLevelEvents() {
		TiledObjectGroup group = m_map.getGroup("Triggers");
		
		Array<TiledObject> objects = group.getObjects();
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.isSensor = true;
		
		for (int i = 0; i < objects.size; ++i) {
			TiledObject object = objects.get(i);
			Body body = m_world.createBody(bodyDef);
			m_bodies.add(body);
			body.setUserData(IDGenerator.getID(object.getName()));
			body.setTransform(object.getPosition().cpy().mul(Game.mpp), 0.0f);
			fixtureDef.shape = object.getPolygon();
			body.createFixture(fixtureDef);
		}
	}
	
	@Override
	public void beginContact(Contact contact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		Object dataA = fixA.getBody().getUserData();
		Object dataB = fixB.getBody().getUserData();
		
		// Player against level finish
		boolean caveman = (dataA != null && dataA instanceof Entity && ((Entity)dataA).getType() == StateGame.Caveman) ||
						  (dataB != null && dataB instanceof Entity && ((Entity)dataB).getType() == StateGame.Caveman);
		
		boolean isLevelFinish = (dataA != null && dataA instanceof Integer && ((Integer)dataA) == StateGame.LevelFinished) ||
							    (dataB != null && dataB instanceof Integer && ((Integer)dataB) == StateGame.LevelFinished);
		
		if (caveman && isLevelFinish) {
			m_state = State.LevelCompleted;
			
			return;
		}
		
		boolean isFall = (dataA != null && dataA instanceof Integer && ((Integer)dataA) == StateGame.Fall) ||
			    		 (dataB != null && dataB instanceof Integer && ((Integer)dataB) == StateGame.Fall);
		
		if (caveman && isFall) {
			m_state = State.GameOver;
			
			return;
		}
		
		if (dataA != null && dataA instanceof Entity) {
			((Entity)dataA).onMessage(null, BeginContact, contact);
		}
		
		if (dataB != null && dataB instanceof Entity) {
			((Entity)dataB).onMessage(null, BeginContact, contact);
		}
	}

	@Override
	public void endContact(Contact contact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		Object dataA = fixA.getBody().getUserData();
		Object dataB = fixB.getBody().getUserData();
		
		if (dataA instanceof Entity) {
			((Entity)dataA).onMessage(null, EndContact, contact);
		}
		
		if (dataB instanceof Entity) {
			((Entity)dataB).onMessage(null, EndContact, contact);
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}
	
	public void createEnemies() {
		Array<TiledObject> objects = m_map.getGroup("Enemies").getObjectsByName("enemy");
		
		for (int i = 0; i < objects.size; ++i) {
			Entity enemy = m_entityManager.obtain();
			enemy.setType(Enemy);
			enemy.addComponent(new AnimationComponent(enemy, "data/enemy.xml"));
			enemy.addComponent(new PhysicsComponent(enemy, "data/enemy_physics.xml"));
			enemy.addComponent(new EnemyController(enemy));
			enemy.batch();
			enemy.fetchAssets();

			
			enemy.onMessage(null, PhysicsComponent.DisablePhysics, null);
			Vector2 enemyPos = objects.get(i).getPosition();
			enemy.setPosition(enemyPos.x * Game.mpp, enemyPos.y * Game.mpp, 12.0f);
			enemy.setState(IDGenerator.getID("walk"));
			enemy.onMessage(null, PhysicsComponent.EnablePhysics, null);
			
			m_enemies.add(enemy);
		}
	}
	
	public void createAmmo() {
		Array<TiledObject> objects = m_map.getGroup("Objects").getObjectsByName("ammo");
		
		for (int i = 0; i < objects.size; ++i) {
			Entity ammo = m_entityManager.obtain();
			ammo.setType(Ammo);
			ammo.addComponent(new AnimationComponent(ammo, "data/ammo.xml"));
			ammo.addComponent(new PhysicsComponent(ammo, "data/ammo_physics.xml"));
			ammo.addComponent(new AmmoController(ammo));
			ammo.batch();
			ammo.fetchAssets();
			
			
			ammo.onMessage(null, PhysicsComponent.DisablePhysics, null);
			Vector2 ammoPos = objects.get(i).getPosition();
			ammo.setPosition(ammoPos.x * Game.mpp, ammoPos.y * Game.mpp, 5.0f);
			ammo.onMessage(null, PhysicsComponent.EnablePhysics, null);
			ammo.reset();
			
			m_ammos.add(ammo);
		}
	}
	
	public void resetLevel() {
		dispose();
		load();
		m_state = State.Loading;
//		Vector2 startPos = m_map.getGroup("Objects").get("playerStart").getPosition();
//		m_caveman.setPosition(startPos.x * Game.mpp, startPos.y * Game.mpp, 11.0f);
//		m_caveman.onMessage(null, PhysicsComponent.EnablePhysics, null);
//		
//		Vector2 cameraPos = m_map.getGroup("Objects").get("cameraPos").getPosition();
//		m_camera.position.x = cameraPos.x * Game.mpp;
//		m_camera.position.y = cameraPos.y * Game.mpp;
//		
//		// Delete entities
//		m_entityManager.freeWithType(Enemy);
//		m_entityManager.freeWithType(Item);
//		m_entityManager.freeWithType(Ammo);
//		
//		deleteAllBodies();
//		
//		loadLevelBodies();
//		loadLevelEvents();
//		createEnemies();
//		createAmmo();
	}
	
	public void updateCamera() {
		Vector2 cavemanPos = new Vector2();
		cavemanPos.x = m_caveman.getPosition().x;
		cavemanPos.y = m_caveman.getPosition().y;
		
		Vector2 cameraPos = new Vector2();
		cameraPos.x = cameraPos.x;
		cameraPos.y = cameraPos.y;
		
		float camWidth = Game.getVirtualWidth() * Game.mpp;
		float camHeight = Game.getVirtualHeight() * Game.mpp;
		float mapWidth = m_map.getWidth() * m_map.getTileWidth() * Game.mpp;
		float mapHeight = m_map.getHeight() * m_map.getTileHeight() * Game.mpp;
		
		float maxX = mapWidth - camWidth * 0.5f;
		float maxY = mapHeight - camHeight * 0.5f;
		
	
		Vector3 destPos = new Vector3();
		
		// Check map bounds
		destPos.x = Math.max(Math.min(cavemanPos.x, maxX), camWidth * 0.5f);
		
		if (Gdx.input.isKeyPressed(Keys.DOWN)) {
			destPos.y = Math.max(Math.min(cavemanPos.y + m_cameraOffsetY + 5.0f, maxY), camHeight * 0.5f);
		}
		else {
			destPos.y = Math.max(Math.min(cavemanPos.y + m_cameraOffsetY, maxY), camHeight * 0.5f);
		}
		
		
		destPos.z = m_camera.position.z;
		
		Tween.to(m_camera, CameraTweener.Position, m_cameraTweenTime).
			  ease(Quad.IN).
			  target(destPos.x, destPos.y, destPos.z).
			  start(m_tweenManager);
	}
	
	@Override
	public void onEvent(Object sender, int type, Object data) {
		if (type == OnPlayerDeath) {
			m_state = State.GameOver;
		}
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.ESCAPE) {
			m_stateManager.changeState("StateMenu");
		}
		return true;
	}
	
	private void deleteAllBodies() {
		for (int i = 0; i < m_bodies.size; ++i) {
			m_world.destroyBody(m_bodies.get(i));
		}
		
		m_bodies.clear();
	}
}
