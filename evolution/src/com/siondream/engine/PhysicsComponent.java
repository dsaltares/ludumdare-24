package com.siondream.engine;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;

public class PhysicsComponent extends Component {

	public final static int EnablePhysics = IDGenerator.getID("EnablePhysics");
	public final static int DisablePhysics = IDGenerator.getID("DisablePhysics");
	
	private static Logger m_logger = Game.getLogger();
	
	private String m_file;
	private Body m_body = null;
	private Boolean m_physics = false;
	
	public PhysicsComponent(Entity entity, String file) {
		super(entity, "PhysicsComponent", 4);
		
		m_file = file;
		Game.getAssetManager().load(m_file, PhysicsData.class);
		
		m_entity.addListener(Entity.EntityMoved, this);
		m_entity.addListener(Entity.EntityRotated, this);
		m_entity.addListener(EnablePhysics, this);
		m_entity.addListener(DisablePhysics, this);
	}
	
	@Override
	public void dispose() {
		if (m_body != null) {
			Game.getWorld().destroyBody(m_body);
			m_body = null;
		}
		
		Game.getAssetManager().unload(m_file);
		
		m_entity.removeListener(Entity.EntityMoved, this);
		m_entity.removeListener(Entity.EntityRotated, this);
		m_entity.removeListener(EnablePhysics, this);
		m_entity.removeListener(DisablePhysics, this);
		
		reset();
	}
	
	@Override
	public void fetchAssets() {
		PhysicsData data = Game.getAssetManager().get(m_file, PhysicsData.class);
		
		m_body = Game.getWorld().createBody(data.getBodyDef());
		m_body.setMassData(data.getMassData());
		m_body.setUserData(m_entity);
		
		Array<FixtureDef> fixtureDefs = data.getFixtureDefs();
		Array<Integer> fixtureIds = data.getFixtureIds();
		Array<Filter> filters = data.getFilters();
		
		for (int i = 0; i < fixtureDefs.size && i < filters.size; ++i) {
			Fixture fixture = m_body.createFixture(fixtureDefs.get(i));
			fixture.setUserData(fixtureIds.get(i));
			//fixture.setFilterData(filters.get(i));
		}
	}
	
	@Override
	public void update(float deltaT) {
		
		if (m_type == IDGenerator.getID("item")) {
			m_logger.error("Physis: Updating rock!");
		}
		
		// Set position and rotation on the entity
		if (m_body != null && m_body.isActive()) {
			Vector2 bodyPos = m_body.getPosition();
			m_entity.setPosition(bodyPos.x,
								 bodyPos.y,
								 m_entity.getPosition().z);
			m_entity.setRotation(m_body.getAngle());
		}
	}
	
	@Override
	public void reset() {
		if (m_body != null) {
			m_body.setActive(false);
		}
	}
	
	@Override
	public void onMessage(Component sender, int type, Object data) {
		if (m_entity != null && m_entity.getType() == IDGenerator.getID("item")) {
			m_logger.error(toString() + " event of type " + IDGenerator.getString(type));
		}
		
		if (type == Entity.EntityMoved || type == Entity.EntityRotated) {
			if (m_body != null && !m_body.isActive()) {
				Vector3 entityPos = m_entity.getPosition();
				m_body.setTransform(entityPos.x, entityPos.y, m_entity.getRotation());
			}
		}
		else if (type == EnablePhysics && m_body != null) {
			m_body.setActive(true);
		}
		else if (type == DisablePhysics && m_body != null) {
			m_body.setActive(false);
		}
		else {
			m_logger.error("PhysicsComponent: unknown event " + IDGenerator.getString(type) + " for entity " + m_entity);
		}
	}

	@Override
	public Array<Integer> getDependencies() {
		return new Array<Integer>();
	}

	public Body getBody() {
		return m_body;
	}
}
