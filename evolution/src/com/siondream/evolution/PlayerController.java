package com.siondream.evolution;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.engine.AnimationComponent;
import com.siondream.engine.Component;
import com.siondream.engine.Entity;
import com.siondream.engine.EntityManager;
import com.siondream.engine.Game;
import com.siondream.engine.IDGenerator;
import com.siondream.engine.PhysicsComponent;
import com.siondream.engine.Settings;
import com.siondream.engine.StateManager;


public class PlayerController extends Component {

	// Constants
	private static final int m_physicsComponentID = IDGenerator.getID("PhysicsComponent");
	private static final int m_animComponentID = IDGenerator.getID("AnimationComponent");
	private static final int m_idle = IDGenerator.getID("idle");
	private static final int m_walk = IDGenerator.getID("walk");
	private static final int m_jump = IDGenerator.getID("jump");
	private static final int m_erase = IDGenerator.getID("erase");
	private static final int m_footID = IDGenerator.getID("foot");
	
	// System
	private static final Logger m_logger = Game.getLogger();
	private static final Settings m_settings = Game.getSettings();
	private static final World m_world = Game.getWorld();
	private static final StateManager m_stateManager = Game.getStateManager();
	private static final EntityManager m_entityManager = Game.getEntityManager();
	private static final AssetManager m_assetManager = Game.getAssetManager();
	
	private Body m_body;
	private Fixture m_mainFix;
	private Fixture m_sensorFix;
	private AnimationComponent m_animComponent;
	private float m_regularFriction = 0.8f;
	
	// Settings
	private final float m_maxSpeed = m_settings.getFloat("cavemanMaxSpeed", 7.0f);
	private final float m_walkImpulse = m_settings.getFloat("cavemanWalkImpulse", 4.0f);
	private final float m_jumpImpulse = m_settings.getFloat("cavemanJumpImpulse", 20.0f);
	private final float m_throwTime = m_settings.getFloat("cavemanThrowTime", 1.0f);
	private final Vector3 m_throwOffset = m_settings.getVector("cavemanThrowOffset", Vector3.Zero);
	private final Vector3 m_throwLinear = m_settings.getVector("cavemanThrowLinear", Vector3.Zero);
	private final float m_throwAngular = m_settings.getFloat("cavemanThrowAngular", 1.0f);
	private final int m_initialAmmo = m_settings.getInt("cavemanAmmo", 0);
	
	// State
	private float m_throwCounter = 0;
	private int m_footContacts = 0;
	private int m_ammo = m_initialAmmo;
	
	private Sound m_throwSFX;
	private Sound m_jumpSFX;
	private Sound m_dieSFX;
	private Sound m_killSFX;
	private Sound m_collectSFX;
	private Sound m_landSFX;

	
	public PlayerController(Entity entity) {
		super(entity, "PlayerController", 3);
		
		m_entity.addListener(Entity.EntityBatched, this);
		m_entity.addListener(StateGame.BeginContact, this);
		m_entity.addListener(StateGame.EndContact, this);
		
		m_assetManager.load("data/rock.wav", Sound.class);
		m_assetManager.load("data/collect.wav", Sound.class);
		m_assetManager.load("data/die.wav", Sound.class);
		m_assetManager.load("data/jump.wav", Sound.class);
		m_assetManager.load("data/kill.wav", Sound.class);
		m_assetManager.load("data/land.wav", Sound.class);
	}

	@Override
	public void dispose() {
		m_entity.removeListener(Entity.EntityBatched, this);
		m_entity.removeListener(StateGame.BeginContact, this);
		m_entity.removeListener(StateGame.EndContact, this);
		
		m_assetManager.unload("data/rock.wav");
		m_assetManager.unload("data/collect.wav");
		m_assetManager.unload("data/die.wav");
		m_assetManager.unload("data/jump.wav");
		m_assetManager.unload("data/kill.wav");
		m_assetManager.unload("data/land.wav");
		
		m_throwSFX = null;
		m_collectSFX = null;
		m_dieSFX = null;
		m_jumpSFX = null;
		m_killSFX = null;
		m_landSFX = null;
		
		m_animComponent = null;
		m_body = null;
		m_mainFix = null;
		m_sensorFix = null;
		
		reset();
	}

	@Override
	public void update(float deltaT) {
		if (m_body != null) {
			
			Vector2 vel = m_body.getLinearVelocity();
			Vector2 pos = m_body.getPosition();
			int state = m_entity.getState();
			
			// COMMON UPDATE CODE
			
			// Cap X speed
			if (Math.abs(vel.x) > m_maxSpeed) {
				vel.x = Math.signum(vel.x) * m_maxSpeed;
				m_body.setLinearVelocity(vel);
			}
			
			// Apply left impulse, but only if max velocity is not reached yet
			if(Gdx.input.isKeyPressed(Keys.LEFT) && vel.x > -m_maxSpeed) {
				m_body.applyLinearImpulse(-m_walkImpulse, 0.0f, pos.x, pos.y);
			}
			else if (Gdx.input.isKeyPressed(Keys.RIGHT) && vel.x < m_maxSpeed) {
				m_body.applyLinearImpulse(m_walkImpulse, 0.0f, pos.x, pos.y);
			}
			
			if (m_footContacts > 0 && state != m_jump && Gdx.input.isKeyPressed(Keys.UP)) {
				pos.y -= 0.1f;
				m_body.applyLinearImpulse(0.0f, -m_jumpImpulse - Math.abs(vel.x) * 2.0f, pos.x, pos.y);
				long id = m_jumpSFX.play();
				m_jumpSFX.setVolume(id, 0.3f);
			}
			
			if (vel.x < -0.5f) {
				m_animComponent.flip(true, true);
			}
			else if (vel.x > 0.5f) {
				m_animComponent.flip(false, true);
			}
			
			
			
			m_throwCounter -= deltaT;
			
			if (m_footContacts > 0) {
				m_mainFix.setFriction(m_regularFriction);
				m_sensorFix.setFriction(m_regularFriction);
			}
			else {
				m_mainFix.setFriction(0.2f);
				m_sensorFix.setFriction(0.2f);
			}
			
			
			// STATE SPECIFIC UPDATE CODE			
			
			if (state == m_idle) {
				// Start moving
				if (Math.abs(vel.x) > 0.2f) {
					m_entity.setState(m_walk);
				}
			}
			else if (state == m_walk) {
				// Stop
				if (Math.abs(vel.x) < 0.2f) {
					m_entity.setState(m_idle);
				}
			}
		}
	}

	@Override
	public void reset() {
		m_footContacts = 0;
		m_throwCounter = 0.0f;
		m_ammo = 0;
		m_ammo = m_initialAmmo;
	}
	
	@Override
	public void onMessage(Component sender, int type, Object data) {
		if (type == Entity.EntityBatched) {
			m_body = ((PhysicsComponent)m_entity.getComponent(m_physicsComponentID)).getBody();
			
			if (m_body != null) {
				m_mainFix = m_body.getFixtureList().get(0);
				m_sensorFix = m_body.getFixtureList().get(1);
			}
		}
		else if (type == StateGame.BeginContact) {
			beginContact((Contact)data);
		}
		else if (type == StateGame.EndContact) {
			endContact((Contact)data);
		}
		else {
			m_logger.error("PlayerControllerComponent: event type not handled " + IDGenerator.getString(type));
		}
	}

	@Override
	public Array<Integer> getDependencies() {
		Array<Integer> dependencies = new Array<Integer>();
		dependencies.add(m_physicsComponentID);
		dependencies.add(m_animComponentID);
		return dependencies;
	}
	
	@Override
	public void fetchAssets() {
		m_body = ((PhysicsComponent)m_entity.getComponent(m_physicsComponentID)).getBody();
		m_mainFix = m_body.getFixtureList().get(0);
		m_sensorFix = m_body.getFixtureList().get(1);
		m_regularFriction = m_mainFix.getFriction();
		m_animComponent = (AnimationComponent)m_entity.getComponent(m_animComponentID);
		
		m_throwSFX = m_assetManager.get("data/rock.wav", Sound.class);
		m_collectSFX = m_assetManager.get("data/collect.wav", Sound.class);
		m_dieSFX = m_assetManager.get("data/die.wav", Sound.class);
		m_jumpSFX = m_assetManager.get("data/jump.wav", Sound.class);
		m_killSFX = m_assetManager.get("data/kill.wav", Sound.class);
		m_landSFX = m_assetManager.get("data/land.wav", Sound.class);
	}
	
	private void beginContact(Contact contact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		Body bodyA = fixA.getBody();
		Body bodyB = fixB.getBody();
		Object dataA = fixA.getUserData();
		Object dataB = fixB.getUserData();
		Object bodyDataA = bodyA.getUserData();
		Object bodyDataB = bodyB.getUserData();
		
		// Foot sensor collision
		Object other = null;
		Fixture otherFixture = null;
		Body otherBody = null;
		Object foot = null;
		
		if (dataA != null && (Integer)(dataA) == m_footID) {
			other = bodyDataB;
			otherFixture = fixB;
			otherBody = bodyB;
			foot = bodyDataA;
		}
		else if (dataB != null && (Integer)(dataB) == m_footID) {
			other = bodyDataA;
			otherFixture = fixA;
			otherBody = bodyA;
			foot = bodyDataB;
		}

		// Hit enemy on top
		if (other != null && other instanceof Entity && ((Entity)other).getType() == StateGame.Enemy) {
			((Entity)other).setState(m_erase);
			m_killSFX.play();
			return;
		}
		else if (foot != null && !otherFixture.isSensor() && !(otherBody.getUserData() instanceof Entity)){
			if (++m_footContacts == 1) {
				m_entity.setState(m_idle);
				m_landSFX.play();
			}
			return;
		}
		
		if (bodyDataA instanceof Entity && bodyDataB instanceof Entity) {
			Entity entityA = (Entity)bodyDataA;
			Entity entityB = (Entity)bodyDataB;
			Entity otherEntity = null;
			
			// A or B is gonna be m_entity, the other is a different entity
			if (entityA == m_entity) {
				otherEntity = entityB;
			}
			else {
				otherEntity = entityA;
			}
			
			// Enemies
			if (otherEntity.getType() == StateGame.Enemy && otherEntity.getState() != m_erase) {
				m_stateManager.onEvent(this, StateGame.OnPlayerDeath, null);
				m_dieSFX.play();
				return;
			}
			
			// Ammo
			if (otherEntity.getType() == StateGame.Ammo && otherEntity.getState() != m_erase) {
				otherEntity.setState(m_erase);
				m_ammo += 1;
				m_logger.info("Ammo: " + m_ammo);
				return;
			}
		}
	}
	
	private void endContact(Contact contact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		Body bodyA = fixA.getBody();
		Body bodyB = fixB.getBody();
		Object dataA = fixA.getUserData();
		Object dataB = fixB.getUserData();
		Object bodyDataA = bodyA.getUserData();
		Object bodyDataB = bodyB.getUserData();
		
		// Foot sensor collision
		Fixture otherFixture = null;
		Body otherBody = null;
		Object foot = null;
		
		if (dataA != null && (Integer)(dataA) == m_footID) {
			otherFixture = fixB;
			otherBody = bodyB;
			foot = bodyDataA;
		}
		else if (dataB != null && (Integer)(dataB) == m_footID) {
			otherFixture = fixA;
			otherBody = bodyA;
			foot = bodyDataB;
		}
		
		if (foot != null && !otherFixture.isSensor() && !(otherBody.getUserData() instanceof Entity)){
			if (--m_footContacts == 0) {
				m_entity.setState(m_jump);
			}
			return;
		}
	}
	
	public void throwItem() {
		if (m_ammo > 0 && m_throwCounter < 0) {
			// Create entity
			Entity item = m_entityManager.obtain();
			item.setType(StateGame.Item);
			PhysicsComponent physics = new PhysicsComponent(item, "data/rock_physics.xml");
			item.addComponent(new AnimationComponent(item, "data/rock.xml"));
			item.addComponent(physics);
			//item.addComponent(new ItemController(item));
			item.batch();
			item.fetchAssets();
			
			boolean flipX = m_animComponent.getFlipX();
			Vector2 entityPos = m_entity.getPosition2D();
			
			if (entityPos == null) {
				m_logger.error("ENTITY POS NULL");
			}
			
			// Set offset position
			item.onMessage(null, PhysicsComponent.DisablePhysics, null);
			item.setPosition(flipX? entityPos.x - m_throwOffset.x : entityPos.x + m_throwOffset.x,
						     entityPos.y + m_throwOffset.y,
						     9.0f);
			item.onMessage(null, PhysicsComponent.EnablePhysics, null);
			
			// State
			item.setState(IDGenerator.getID("idle"));
			
			// Apply impulses
			if (physics.getBody() != null) {
				physics.getBody().applyAngularImpulse(m_throwAngular);
				
				physics.getBody().applyLinearImpulse(flipX? -m_throwLinear.x + m_body.getLinearVelocity().x * 0.2f : m_throwLinear.x + m_body.getLinearVelocity().x * 0.2f,
													 m_throwLinear.y,
													 0.0f,
													 0.0f);
			}
			
			StateGame.m_items.add(item);
			
			
			// Reset counter
			m_throwCounter = m_throwTime;
			
			// Decrease ammo
			--m_ammo;
			
			// Play sound
			m_throwSFX.play();
		}
	}
	
	public int getAmmo() {
		return m_ammo;
	}
}
