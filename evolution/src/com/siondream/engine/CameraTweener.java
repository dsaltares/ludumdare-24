package com.siondream.engine;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Logger;

import aurelienribon.tweenengine.TweenAccessor;

public class CameraTweener implements TweenAccessor<OrthographicCamera>{

	public static final int Position = 1;
	public static final int Zoom = 2;
	
	private static Logger m_logger = Game.getLogger();
	
	@Override
	public int getValues(OrthographicCamera camera, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case Position:
			returnValues[0] = camera.position.x;
			returnValues[1] = camera.position.y;
			returnValues[2] = camera.position.z;
			return 3;
		case Zoom:
			returnValues[0] = camera.zoom;
			return 1;
		default:
			m_logger.error("Camera: invalid tweentype " + tweenType);
			return 0;
		}
	}

	@Override
	public void setValues(OrthographicCamera camera, int tweenType, float[] newValues) {
		switch (tweenType) {
		case Position:
			camera.position.x = newValues[0];
			camera.position.y = newValues[1];
			camera.position.z = newValues[2];
			break;
		case Zoom:
			camera.zoom = newValues[0];
			break;
		default:
			m_logger.error("Camera: invalid tweentype " + tweenType);
			break;
		}
	}

}
