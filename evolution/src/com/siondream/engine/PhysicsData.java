package com.siondream.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class PhysicsData {
	private Logger m_logger = Game.getLogger();
	private String m_file;
	private BodyDef m_bodyDef = new BodyDef();
	private MassData m_massData = new MassData();
	private Array<FixtureDef> m_fixtureDefs = new Array<FixtureDef>();
	private Array<Filter> m_filters = new Array<Filter>();
	private Array<Integer> m_fixtureIds = new Array<Integer>();
	
	public PhysicsData(String file) {
		m_file = file;
	}
	
	public BodyDef getBodyDef() {
		return m_bodyDef;
	}
	
	public MassData getMassData() {
		return m_massData;
	}
	
	public Array<FixtureDef> getFixtureDefs() {
		return m_fixtureDefs;
	}
	
	public Array<Integer> getFixtureIds() {
		return m_fixtureIds;
	}
	
	public Array<Filter> getFilters() {
		return m_filters;
	}
	
	public void loadData() {
		m_logger.info("PhysicsData: loading from file " + m_file);
		
		try {
			// Parse xml document
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(m_file));
			
			loadBodyDef(root);
			loadMassData(root);
			loadFixtureDefs(root);
			
		} catch (Exception e) {
			m_logger.error("PhysicsData: error loading file " + m_file + " " + e.getMessage());
		}
	}
	
	private void loadBodyDef(Element root) {
		m_bodyDef.bullet = root.getBoolean("bullet", false);
		m_bodyDef.active = root.getBoolean("active", true);
		m_bodyDef.fixedRotation = root.getBoolean("fixedRotation", false);
		m_bodyDef.gravityScale = root.getFloat("gravityScale", 1.0f);
		
		String type = root.get("type", "dynamic");
		
		if (type.equals("dynamic")) {
			m_bodyDef.type = BodyDef.BodyType.DynamicBody;
		}
		else if (type.equals("kynematic")) {
			m_bodyDef.type = BodyDef.BodyType.KinematicBody;
		}
		else if (type.equals("static")) {
			m_bodyDef.type = BodyDef.BodyType.KinematicBody;
		}
		else {
			m_logger.error("PhysicsData: unknown body type " + type);
		}
	}
	
	private void loadMassData(Element root) {
		Element massData = root.getChildByName("massData");
		m_massData.center.x = massData.getFloat("centerX", 0.0f);
		m_massData.center.y = massData.getFloat("centerY", 0.0f);
		m_massData.I = massData.getFloat("i", 0.0f);
		m_massData.mass = massData.getFloat("mass", 1.0f);
	}
	
	private void loadFixtureDefs(Element root) {
		Array<Element> fixtures = root.getChildrenByName("fixture");
		
		for (int i = 0; i < fixtures.size; ++i) {
			Element fixtureElement = fixtures.get(i);
			
			FixtureDef fixtureDef = new FixtureDef();
			
			fixtureDef.density = fixtureElement.getFloat("density", 1.0f);
			fixtureDef.restitution = fixtureElement.getFloat("restitution", 0.0f);
			fixtureDef.friction = fixtureElement.getFloat("friction", 1.0f);
			fixtureDef.isSensor = fixtureElement.getBoolean("isSensor", false);
			fixtureDef.shape = loadShape(fixtureElement);
			
			m_filters.add(loadFilter(fixtureElement));
			m_fixtureIds.add(IDGenerator.getID(fixtureElement.get("id", "")));
			m_fixtureDefs.add(fixtureDef);
		}
	}
	
	private Filter loadFilter(Element root) {
		Element filterElement = root.getChildByName("filter");
		Filter filter = new Filter();
		
		if (filterElement == null) { 
			m_logger.info("PhysicsData: no filter for shape, returning default one");
			return filter;
		}
		
		filter.categoryBits = (short)filterElement.getInt("categoryBits", 0);
		filter.groupIndex = (short)filterElement.getInt("groupIndex", 0);
		filter.maskBits = (short)filterElement.getInt("maskBits", 0);
		
		return filter;
	}
	
	private Shape loadShape(Element root) {
		Shape shape = null;
		Element shapeElement = root.getChildByName("shape");
		
		if (shapeElement == null) {
			return shape;
		}
		
		String type = shapeElement.get("type");

		float x = shapeElement.getFloat("centerX", 0.0f);
		float y = shapeElement.getFloat("centerY", 0.0f);
		
		if (type.equals("circle")) {
			CircleShape circle = new CircleShape();
			circle.setPosition(new Vector2(x, y));
			circle.setRadius(shapeElement.getFloat("radius", 1.0f));
			shape = circle;
		}
		else if (type.equals("polygon")) {
			PolygonShape polygon = new PolygonShape();
			polygon.setAsBox(shapeElement.getFloat("width", 1.0f),
							 shapeElement.getFloat("height", 1.0f),
							 new Vector2(x, y),
							 0.0f);
			shape = polygon;
		}
		else {
			m_logger.error("PhysicsData: shape unknown " + type);
		}
		
		
		return shape;
	}
}
