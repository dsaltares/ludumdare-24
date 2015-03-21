package com.siondream.engine;

import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;

public abstract class State implements GestureDetector.GestureListener,
									   InputProcessor,
									   Disposable {
	
	protected static final Logger m_logger = Game.getLogger();
	protected static final StateManager m_stateManager = Game.getStateManager();
	protected static final AssetManager m_assetManager = Game.getAssetManager();
	protected static final EntityManager m_entityManager = Game.getEntityManager();
	protected static final Settings m_settings = Game.getSettings();
	protected static final TweenManager m_tweenManager = Game.getTweenManager();
	protected static final SpriteBatch m_batch = Game.getSpriteBatch();
	
	protected GestureDetector m_gestureDetector = null;
	
	protected boolean m_loaded = false;
	protected boolean m_active = false;
	protected boolean m_paused = false;
	protected String m_name = null;
	
	public State(String name) {
		m_name = name;
		
		int halfTapSquareSize = m_settings.getInt("halTapSquareSize", 20);
		float tapCountInterval = m_settings.getFloat("tapCountInterval", 0.4f);
		float longPressDuration = m_settings.getFloat("longPressDuration", 2.0f);
		float maxFlingDelay = m_settings.getFloat("maxFlingDelay", 0.15f);
		
		m_gestureDetector = new GestureDetector(halfTapSquareSize,
												tapCountInterval,
												longPressDuration,
												maxFlingDelay,
												this);
	}
	
	public String getName() {
		return m_name;
	}

	// Memory management
	
	public void load() {
		if (!m_loaded) {
			m_loaded = true;
		}
	}
	
	@Override
	public void dispose() {
		if (m_loaded) {
			m_loaded = false;
		}
	}
	
	public boolean isLoaded() {
		return m_loaded;
	}
	
	public void finishLoading() {
		m_entityManager.fetchAssets();
	}
	
	
	// Life cycle and game loop
	
	public boolean isActive() {
		return m_active;
	}
	
	public void setActive(boolean active) {
		m_active = active;
		
		if (m_active) {
			m_logger.info(m_name + ": is now active");
		}
		else {
			m_logger.info(m_name + ": is now inactive");
		}
	}
	
	public void pause() {
		m_paused = true;
	}
	
	public void resume() {
		m_paused = false;
	}
	
	public void update(float deltaT) {
		
	}
	
	
	// Events
	
	public GestureDetector getGestureDetector() {
		return m_gestureDetector;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		return true;
	}
	
	@Override
	public boolean keyTyped(char character) {
		return true;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		return true;
	}
	
	@Override
	public boolean scrolled(int amount) {
		return true;
	}
	
	@Override
    public boolean touchDown(int x, int y, int pointer, int button) {
    	return true;
    }
    
	@Override
    public boolean touchDragged(int x, int y, int pointer) {
		return true;
    }
    
	@Override
    public boolean touchUp(int x, int y, int pointer, int button) {
		return true;
    }
    
	@Override
	public boolean touchDown(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tap(int x, int y, int count) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean longPress(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pan(int x, int y, int deltaX, int deltaY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}
    
	@Override
    public boolean zoom(float originalDistance, float currentDistance) {
		return true;
    }
	
	@Override
	public boolean pinch(Vector2 initialFirstPointer,
						 Vector2 initialSecondPointer,
						 Vector2 firstPointer,
						 Vector2 secondPointer) {
		return false;
	}
	
	public void onEvent(Object sender, int type, Object data) {}
	
	@Override
	public boolean equals(Object o) {
		// We won't have two different entities with the same name
		
		if (o == null) {
	    	return false;
	    }
	    
	    if (o == this) {
	    	return true;
	    }
	    
	    if (!(o instanceof State)) {
	    	return false;
	    }
	    
	    State state = (State)o;
	    
	    return m_name.equals(state.m_name);
	}
	
	@Override
	public int hashCode() {
		return m_name.hashCode();
	}
	
	@Override
	public String toString() {
		return "State (" + m_name + ")";
	}
}
