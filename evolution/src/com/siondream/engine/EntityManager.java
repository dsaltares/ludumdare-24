package com.siondream.engine;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Pool;

public class EntityManager extends Pool<Entity> {

	private static Logger m_logger = Game.getLogger();
	
	private int m_nextID = 1;
	private IntMap<Entity> m_map;
	private Array<Entity> m_sorted;
	
	// Constructors
	public EntityManager() {
		this(Game.getSettings().getInt("entityPoolSize", 100));
	}
	
	public EntityManager(int capacity) {
		super(capacity, capacity);
		
		m_map = new IntMap<Entity>(capacity);
		m_sorted = new Array<Entity>(true, capacity);
		
		m_logger.info("EntityManager: creating entity manager with pool size " + capacity);
	}
	
	// Accesor
	
	public Entity get(int id) {
		return m_map.get(id);
	}
	
	// Update entities
	public void update(float deltaT) {
		m_sorted.sort();
	
		for (int i = 0; i < m_sorted.size; ++i) {
			m_sorted.get(i).update(deltaT);
		}
	}
	
	// Tell entities to fetch their assets
	public void fetchAssets() {
		for (int i = 0; i < m_sorted.size; ++i) {
			m_sorted.get(i).fetchAssets();
		}
	}
	
	// New 
	
	@Override
	public Entity obtain() {
		Entity entity = super.obtain();
		m_map.put(entity.getID(), entity);
		m_sorted.add(entity);
		
		m_logger.info("EntityManager: obtaining entity (id: " + entity.getID() + ")");
		
		return entity;
	}
	
	@Override
	public void free(Entity entity) {
		int id = (entity != null)? entity.getID() : 0;
		
		if (m_map.get(id) == null) {
			return;
		}
		
		m_logger.info("EntityManager: freeing entity (id: " + entity.getID() + ")");
		
		super.free(entity);
		
		entity.dispose();
		
		if ((m_map.remove(entity.getID()) == null) || !m_sorted.removeValue(entity, false)) {
			m_logger.error("EntityManager: entity not found in active containers");
		}
	}
	
	@Override
	public void free(Array<Entity> entities) {
		if (entities == null) {
			return;
		}
		
		for (int i = 0; i < entities.size; ++i) {
			free(entities.get(i));
		}
		
		entities.clear();
	}
	
//	public void freeWithState(int state) {
//		for (int i = 0; i < m_sorted.size; ++i) {
//			Entity entity = m_sorted.get(i);
//			
//			if (entity.getState() == state) {
//				free(entity);
//			}
//		}
//	}
//	
//	public void freeWithType(int type) {
//		for (int i = 0; i < m_sorted.size; ++i) {
//			Entity entity = m_sorted.get(i);
//			
//			if (entity.getType() == type) {
//				free(entity);
//			}
//		}
//	}
	
	@Override
	public void clear() {
		m_logger.info("EntityManager: clearing all entities");
		
		for (int i = 0; i < m_sorted.size; ++i) {
			m_sorted.get(i).dispose();
		}
		
		super.clear();
		m_map.clear();
		m_sorted.clear();
	}
	
	@Override
	protected Entity newObject() {
		return new Entity(m_nextID++);
	}
}
