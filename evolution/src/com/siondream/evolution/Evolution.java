package com.siondream.evolution;

import com.siondream.engine.Game;

public class Evolution extends Game {
	@Override
	public void create() {	
		super.create();
		
		m_stateManager.setStateFactory(new StateFactoryImpl());
		m_stateManager.pushState("StateMenu");
	}
}
