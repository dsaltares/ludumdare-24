package com.siondream.engine;

import java.util.HashMap;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;

public class Entity implements Comparable<Entity>, Disposable {
	
	// TODO move entity events into constant static class
	public static final int EntityMoved = IDGenerator.getID("EntityMoved");
	public static final int EntityScaled = IDGenerator.getID("EntityScaled");
	public static final int EntityRotated = IDGenerator.getID("EntityRotated");
	public static final int EntityStateChanged = IDGenerator.getID("EntityStateChanged");
	public static final int EntityBatched = IDGenerator.getID("EntityBatched");
	public static final int Empty = IDGenerator.getID("empty");
	
	private Logger m_logger = Game.getLogger();
	private int m_id;
	private int m_type;
	private String m_name;
	private Vector3 m_position;
	private Vector2 m_position2D;
	private float m_rotation;
	private float m_scale;
	private int m_state;
	private boolean m_batched = false;
	
	private static int m_maxComponents = Game.getSettings().getInt("maxComponents", 10);
	private Array<Component> m_components = new Array<Component>(m_maxComponents);
	private HashMap<Integer, Array<Component>> m_listeners = new HashMap<Integer, Array<Component>>(); // TODO alternative to hashmap?
	
	Entity(int id) {
		m_logger = Game.getLogger();
		m_id = id;
		m_type = Empty;
		m_name = "";
		m_position = Vector3.Zero.cpy();
		m_position2D = Vector2.Zero.cpy();
		m_rotation = 0.0f;
		m_scale = 1.0f;
		m_state = IDGenerator.getID("idle");
		m_batched = false;
	}
	
	public int getID() {
		return m_id;
	}
	
	public int getType() {
		return m_type;
	}
	
	public void setType(int type) {
		m_type = type;
	}
	
	public String getName() {
		return m_name;
	}
	
	public void setName(String n) {
		m_name = n;
	}
	
	public Vector3 getPosition() {
		return m_position;
	}
	
	public Vector2 getPosition2D() {
		return m_position2D;
	}
	
	public void setPosition(Vector3 p) { 
		setPosition(p.x, p.y, p.z);
	}
	
	public void setPosition(Vector2 p) {
		setPosition(p.x, p.y, m_position.z);
	}
	
	public void setPosition(float x, float y, float z) {
		m_position.x = x;
		m_position.y = y;
		m_position.z = z;
		m_position2D.x = x;
		m_position2D.y = y;
		onMessage(null, EntityMoved, null);
	}
	
	public float getScale() {
		return m_scale;
	}
	
	public void setScale(float s) {
		m_scale = s;
		onMessage(null, EntityScaled, null);
	}
	
	public float getRotation() {
		return m_rotation;
	}
	
	public void setRotation(float rot) {
		m_rotation = rot;
		m_rotation = m_rotation % 360.0f;
		onMessage(null, EntityRotated, null);
	}
	
	public int getState() {
		return m_state;
	}
	
	public void setState(int state) {
		m_state = state;
		onMessage(null, EntityStateChanged, null);
	}

	public void update(float deltaT) {
		if (m_batched) {
			for (int i = 0; i < m_components.size; ++i) {
				Component component = m_components.get(i);
				
				if (component != null) {
					component.update(deltaT);	
				}
			}
		}
		else {
			m_logger.info("Entity update: " + toString() + " not batched");
		}
	}
	
	public void reset() {
		if (m_batched) {
			for (int i = 0; i < m_components.size; ++i) {
				m_components.get(i).reset();
			}
		}
		else {
			m_logger.info("Entity reset: " + toString() + " not batched");
		}
	}
	
	public void addListener(int type, Component component) {
		Array<Component> components = m_listeners.get(type);
		
		if (components == null) {
			components = new Array<Component>();
			m_listeners.put(type, components);
		}
		
		components.add(component);
	}
	
	public void removeListener(int type, Component component) {
		Array<Component> components = m_listeners.get(type);
		
		if (components == null) {
			return;
		}
		
		if (!components.removeValue(component, true)) {
			m_logger.info(toString() + " failed to remove listener " + IDGenerator.getString(type));
		}
	}
	
	public void onMessage(Component sender, int type, Object data) {
		if (m_batched) {
			Array<Component> components = m_listeners.get(type);
			
			if (components == null) {
				if (m_type == IDGenerator.getID("item"))
				{
					m_logger.error("Entity on message: no listeners in " + toString() + " for event " + IDGenerator.getString(type));
				}
				return;
			}
			
			for (int i = 0; i < components.size; ++i) {
				if (m_type == IDGenerator.getID("item")) {
					m_logger.error(toString() + " sending event of type " + IDGenerator.getString(type) +  " to " + components.get(i).toString());
				}
				components.get(i).onMessage(sender, type, data);
			}
		}
		else {
			m_logger.error("Entity on message: " + toString() + " not batched");
		}
	}
	
	public void addComponent(Component component) {
		m_components.add(component);
	}

	public Component getComponent(int type) {
		for (int i = 0; i < m_components.size; ++i) {
			Component component = m_components.get(i);
			
			if (component.getType() == type) {
				return component;
			}
		}
		
		m_logger.info(toString() + " component " + IDGenerator.getString(type) + " not found");
		
		return null;
	}
	
	public void batch() {
		m_batched = true;
		
		// Check dependencies for every component
		for (int i = 0; i < m_components.size; ++i) {
			Component component = m_components.get(i);
			
			if (!checkDependencies(component.getDependencies())) {
				m_logger.error(toString() + ": dependency error in " + component.getName());
				m_batched = false;
			}
		}
		
		// If we meet dependencies, sort components by priority
		if (m_batched) {
			m_components.sort();
		}
		
		onMessage(null, EntityBatched, null);
	}
	
	public void fetchAssets() {
		if (m_batched) {
			// Tell components to fetch their assets
			for (int i = 0; i < m_components.size; ++i) {
				m_components.get(i).fetchAssets();
			}
		}
		else {
			m_logger.info("Entity fetch assets: " + toString() + " not batched");
		}
	}
	
	@Override
	public void dispose() {
		// Clean attributes for reuse
		m_type = Empty;
		m_name = "";
		m_position.x = m_position.y = m_position.z = 0.0f;
		m_position2D.x = m_position2D.y = 0.0f;
		m_rotation = 0.0f;
		m_scale = 1.0f;
		m_state = IDGenerator.getID("idle"); // TODO constants system
		m_batched = false;
		
		// Clean components
		for (int i = 0; i < m_components.size; ++i) {
			m_components.get(i).dispose();
		}
		
		// Remove list of components
		m_components.clear();
		m_listeners.clear();
	}
	
	@Override
	public boolean equals(Object o) {
		// We won't have two different entities with the same id
		
		if (o == null) {
	    	return false;
	    }
	    
	    if (o == this) {
	    	return true;
	    }
	    
	    if (!(o instanceof Entity)) {
	    	return false;
	    }
	    
	    Entity entity = (Entity)o;
	    
	    return m_id == entity.m_id;
	}
	
	@Override
	public int hashCode() {
		// id in entities guarantees uniqueness
		return m_id;
	}
	
	@Override
	public String toString() {
		return "Entity (id: " + m_id + " type: " + IDGenerator.getString(m_type) + " name: " + m_name + ")";
	}

	@Override
	public int compareTo(Entity o) {
		// Comparing by position along z eases z-sorting for rendering
		
		float result = m_position.z - o.m_position.z;
		
		if (result < 0) {
			return 1;
		}
		else if (result > 0) {
			return -1;
		}
		else {
			return 0;
		}
	}
	
	private boolean checkDependencies(Array<Integer> dependencies) {
		for (int i = 0; i < dependencies.size; ++i) {
			if (getComponent(dependencies.get(i)) == null) {
				return false;
			}
		}
		
		return true;
	}
}
