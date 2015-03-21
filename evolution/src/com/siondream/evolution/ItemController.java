package com.siondream.evolution;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.engine.AnimationComponent;
import com.siondream.engine.Component;
import com.siondream.engine.Entity;
import com.siondream.engine.Game;
import com.siondream.engine.IDGenerator;
import com.siondream.engine.PhysicsComponent;
import com.siondream.engine.Settings;

public class ItemController extends Component {
	
	// Constants
	private static final int m_erase = IDGenerator.getID("erase");
	private static final Settings m_settings = Game.getSettings();
	private static final Logger m_logger = Game.getLogger();
	
	// Settings
	private static final float m_lifeTime = m_settings.getFloat("itemLifeTime", 3.0f);
	
	
	// State
	private float m_lifeCounter;
	
	public ItemController(Entity entity) {
		super(entity, "ItemControllerComponent", 3);
		
		m_entity.addListener(StateGame.BeginContact, this);
		m_entity.addListener(StateGame.EndContact, this);
		
		reset();
	}

	@Override
	public void dispose() {
		m_entity.removeListener(StateGame.BeginContact, this);
		m_entity.removeListener(StateGame.EndContact, this);
		
		reset();
	}

	@Override
	public void update(float deltaT) {
		m_logger.error("ItemController: update");
		if (m_lifeCounter < 0.0f) {
			m_entity.setState(m_erase);
		}
		
		m_lifeCounter -= deltaT;
	}

	@Override
	public void reset() {
		m_lifeCounter = m_lifeTime;
	}

	@Override
	public void onMessage(Component sender, int type, Object data) {
		
	}

	@Override
	public Array<Integer> getDependencies() {
		return new Array<Integer>();
	}
	
	@Override
	public void fetchAssets() {
	}
}
