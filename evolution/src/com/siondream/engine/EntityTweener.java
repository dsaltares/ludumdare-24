package com.siondream.engine;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Logger;

import aurelienribon.tweenengine.TweenAccessor;

public class EntityTweener implements TweenAccessor<Entity>{

	public static final int Position = 1;
	public static final int Scale = 2;
	public static final int Rotation = 3;
	
	private static Logger m_logger = Game.getLogger();
	
	public EntityTweener() {}
	
	@Override
	public int getValues(Entity entity, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case Position:
			Vector3 position = entity.getPosition();
			returnValues[0] = position.x;
			returnValues[1] = position.y;
			returnValues[2] = position.z;
			return 3;
		case Scale:
			returnValues[0] = entity.getScale();
			return 1;
		case Rotation:
			returnValues[0] = entity.getRotation();
			return 1;
		default:
			m_logger.error("Entity: invalid tweentype " + tweenType);
			return 0;
		}
	}

	@Override
	public void setValues(Entity entity, int tweenType, float[] newValues) {
		switch (tweenType) {
		case Position:
			entity.onMessage(null, PhysicsComponent.DisablePhysics, null);
			entity.setPosition(newValues[0], newValues[1], newValues[2]);
			entity.onMessage(null, PhysicsComponent.EnablePhysics, null);
			break;
		case Scale:
			entity.setScale(newValues[0]);
			break;
		case Rotation:
			entity.setRotation(newValues[0]);
			break;
		default:
			m_logger.error("Entity: invalid tweentype " + tweenType);
			break;
		}
	}
}
