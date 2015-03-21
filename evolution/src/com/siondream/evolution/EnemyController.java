package com.siondream.evolution;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.Shape;
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

public class EnemyController extends Component {

	// Constants
	private static final int m_physicsComponentID = IDGenerator.getID("PhysicsComponent");
	private static final int m_animComponentID = IDGenerator.getID("AnimationComponent");
	private static final int m_erase = IDGenerator.getID("erase");

	// System
	private static final Logger m_logger = Game.getLogger();
	private static final Settings m_settings = Game.getSettings();
	private static final World m_world = Game.getWorld();
	private final HeightRayCastCallback m_heightRayCastCallback = new HeightRayCastCallback();
	
	// Settings
	private final float m_maxSpeed = m_settings.getFloat("enemyMaxSpeedX", 6.0f);
	private final float m_impulse = m_settings.getFloat("enemyImpulseX", 4.0f);
	private float m_changeMinTime = m_settings.getFloat("enemyChangeMinTime", 0.5f);
	
	// Data
	private Body m_body;
	private Fixture m_fixture;
	private AnimationComponent m_animComponent;
	private float m_changeTime = m_changeMinTime;
	private boolean m_walkingRight = false;
	private boolean m_turn = true;
	
	public EnemyController(Entity entity) {
		super(entity, "EnemyController", 3);
		
		m_entity.addListener(StateGame.BeginContact, this);
		m_entity.addListener(Entity.EntityBatched, this);
	}
	
	@Override
	public void dispose() {
		m_entity.removeListener(StateGame.BeginContact, this);
		m_entity.removeListener(Entity.EntityBatched, this);
		
		m_animComponent = null;
		m_body = null;
		m_fixture = null;
	}

	@Override
	public void update(float deltaT) {
		Vector2 vel = m_body.getLinearVelocity();
		Vector2 pos = m_body.getPosition();
		int state = m_entity.getState();
		
		// Cap X speed
		if (Math.abs(vel.x) > m_maxSpeed) {
			vel.x = Math.signum(vel.x) * m_maxSpeed;
			m_body.setLinearVelocity(vel);
		}
		
		m_changeTime -= deltaT;
		
		
		
		m_walkingRight = m_animComponent.getFlipX();
		
		// Check if we're about to fall
		float offsetX = m_walkingRight? 1.6f : -1.6f;
		
		Vector2 aheadPosA = new Vector2(pos.x + offsetX, pos.y);
		Vector2 aheadPosB = new Vector2(aheadPosA.x, aheadPosA.y + 20.0f);
		m_turn = true;
		m_world.rayCast(m_heightRayCastCallback, aheadPosA, aheadPosB);
		
		if (m_turn) {
			m_walkingRight = !m_walkingRight;
			m_body.setLinearVelocity(0.0f, m_body.getLinearVelocity().y);
		}
		
		// Turn around when find an obstacle
		if (m_walkingRight && vel.x < 0.5f && m_changeTime < 0.0f) {
			m_walkingRight = false;
			m_changeTime = m_changeMinTime;
		}
		else if (!m_walkingRight && vel.x > -0.5f && m_changeTime < 0.0f) {
			m_walkingRight = true;
			m_changeTime = m_changeMinTime;
		}
		
		
		// Speed control
		if (m_walkingRight && vel.x < m_maxSpeed) {
			m_body.applyLinearImpulse(m_impulse, 0.0f, pos.x, pos.y);
		}
		else if (!m_walkingRight && vel.x > -m_maxSpeed) { 
			m_body.applyLinearImpulse(-m_impulse, 0.0f, pos.x, pos.y);
			
		}
		
		m_animComponent.flip(m_walkingRight, true);
	}

	@Override
	public void reset() {
		m_changeTime = m_changeMinTime;
		m_walkingRight = false;
	}

	@Override
	public void fetchAssets() {
		m_body = ((PhysicsComponent)m_entity.getComponent(m_physicsComponentID)).getBody();
		m_fixture = m_body.getFixtureList().get(0);
		m_animComponent = ((AnimationComponent)m_entity.getComponent(m_animComponentID));
	}
	
	@Override
	public void onMessage(Component sender, int type, Object data) {
		if (type == Entity.EntityBatched) {
//			m_body = ((PhysicsComponent)m_entity.getComponent(m_physicsComponentID)).getBody();
//			m_fixture = m_body.getFixtureList().get(0);
//			m_animComponent = ((AnimationComponent)m_entity.getComponent(m_animComponentID));
		}
		else if (type == StateGame.BeginContact) {
			beginContact((Contact)data);
		}
		else {
			m_logger.error("EnemyController: event type not handled " + IDGenerator.getString(type));
		}
	}

	@Override
	public Array<Integer> getDependencies() {
		Array<Integer> dependencies = new Array<Integer>();
		dependencies.add(m_physicsComponentID);
		dependencies.add(m_animComponentID);
		return dependencies;
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
		
		// Enemies against items
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
			if (otherEntity.getType() == StateGame.Item) {
				m_entity.setState(m_erase);
				otherEntity.setState(m_erase);
				return;
			}
		}
	}
	
	public class HeightRayCastCallback implements RayCastCallback {

		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			if (fixture.getBody() == m_body) {
				return -1;
			}
			
			float enemyGroundY = m_body.getPosition().y + 2.5f;
			
			// Check whether the point indicates a gap
			if (enemyGroundY - point.y > -0.5f) {
				m_turn = false;
				return 0;
			}
			
			return -1;
		}
		
	}
}
