package com.siondream.engine;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public abstract class Component implements Disposable,
										   Comparable<Component> {
	
	protected Entity m_entity;
	protected String m_name;
	protected int m_type;
	protected int m_priority;
	
	public Component(String name, int priority) {
		this(null, name, priority);
	}
	
	public Component(Entity entity, String name, int priority) {
		m_entity = entity;
		m_name = name;
		m_type = IDGenerator.getID(m_name);
		m_priority = priority;
	}

	public String getName() {
		return m_name;
	}
	
	public int getType() {
		return m_type;
	}
	
	public abstract void update(float deltaT);
	
	public abstract void reset();
	
	public abstract void onMessage(Component sender, int type, Object data);
	
	public abstract Array<Integer> getDependencies();
	
	public void fetchAssets() {}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
	    	return false;
	    }
	    
	    if (o == this) {
	    	return true;
	    }
	    
	    if (!(o instanceof Component)) {
	    	return false;
	    }

	    Component component = (Component)o;
	    
	    return m_type == component.m_type &&
	    	   m_entity == component.m_entity;
	}
	
	@Override
	public String toString() {
		return "Component (" + m_name + ")";
	}
	
	@Override
	public int hashCode() {
		return m_type;
	}
	
	@Override
	public int compareTo(Component o) {
		// We compare components by priority
		float result = m_priority - o.m_priority;
		
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
}
