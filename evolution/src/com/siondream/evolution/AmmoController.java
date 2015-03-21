package com.siondream.evolution;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.engine.AnimationComponent;
import com.siondream.engine.Component;
import com.siondream.engine.Entity;
import com.siondream.engine.EntityTweener;
import com.siondream.engine.Game;
import com.siondream.engine.IDGenerator;
import com.siondream.engine.PhysicsComponent;
import com.siondream.engine.Settings;


public class AmmoController extends Component {

	// Constants
	private static final int m_physicsComponentID = IDGenerator.getID("PhysicsComponent");
	private static final int m_animComponentID = IDGenerator.getID("AnimationComponent");

	// System
	private static final Logger m_logger = Game.getLogger();
	private static final Settings m_settings = Game.getSettings();
	private static final TweenManager m_tweenManager = Game.getTweenManager();
	
	// Settings
	private final float m_changeHeight = m_settings.getFloat("ammoChangeHeight", 2.0f);
	private final float m_phaseTime = m_settings.getFloat("ammoPhaseTime", 1.0f);
	
	// Data
	private Body m_body;
	private Fixture m_fixture;
	private AnimationComponent m_animComponent;
	
	public AmmoController(Entity entity) {
		super(entity, "AmmoController", 3);
		
		m_entity.addListener(Entity.EntityBatched, this);
	}
	
	@Override
	public void dispose() {
		m_entity.removeListener(Entity.EntityBatched, this);
		
		m_tweenManager.killTarget(m_entity);
		
		m_animComponent = null;
		m_body = null;
		m_fixture = null;
	}

	@Override
	public void update(float deltaT) {
	
	}

	@Override
	public void reset() {
		Vector3 pos = m_entity.getPosition();
		
		Tween.to(m_entity, EntityTweener.Position, m_phaseTime)
			 .ease(Quad.INOUT)
			 .target(pos.x, pos.y - m_changeHeight, pos.z)
			 .repeatYoyo(Tween.INFINITY, 0.1f)
			 .start(m_tweenManager);
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
}
