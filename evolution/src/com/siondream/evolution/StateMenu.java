package com.siondream.evolution;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.siondream.engine.AnimationComponent;
import com.siondream.engine.AnimationData;
import com.siondream.engine.Game;
import com.siondream.engine.PhysicsComponent;
import com.siondream.engine.PhysicsData;
import com.siondream.engine.State;
import com.siondream.engine.TiledMap;

public class StateMenu extends State {

	public enum State { Loading,
						Running };
	
	// Stystem
	private static final SpriteBatch m_batch = Game.getHUDSpriteBatch();
	
	// Settings
	private final float m_minMenuTime = Game.getSettings().getFloat("minMenuTime", 1.0f);
	
	// Data
	private State m_state;
	private TextureRegion m_background;
	private Music m_music;
	private float m_menuTime = m_minMenuTime;
	
	public StateMenu() {
		super("StateMenu");
		m_state = State.Loading;
	}

	@Override
	public void load() {
		super.load();
		
		m_assetManager.load("data/menu.png", Texture.class);
		m_assetManager.load("data/music.ogg", Music.class);
		
		m_menuTime = m_minMenuTime;
		m_state = State.Loading;
	}
	
	@Override
	public void dispose() {
		super.dispose();

		m_assetManager.unload("data/menu.png");
		m_assetManager.unload("data/music.ogg");
	}
	
	@Override
	public void finishLoading() {
		super.finishLoading();
		
		m_background = new TextureRegion(m_assetManager.get("data/menu.png", Texture.class));
		m_background.flip(false, true);
		
		m_music = m_assetManager.get("data/music.ogg", Music.class);
		m_music.setLooping(true);
		m_music.setVolume(0.7f);
		m_music.play();
	}
	
	@Override
	public void update(float deltaT) {
		if (m_state == State.Loading) {
			
			if (m_assetManager.update()) {
				finishLoading();
				m_state = State.Running;
			}
			
			return;
		}
		
		m_menuTime -= deltaT;
		
		m_batch.begin();
		m_batch.draw(m_background, 0.0f, 0.0f);
		m_batch.end();
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (m_menuTime < 0.0f) {
			m_stateManager.changeState("StateGame");
		}
		
		return true;
	}
}
