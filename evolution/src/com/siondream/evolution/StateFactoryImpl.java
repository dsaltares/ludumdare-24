package com.siondream.evolution;

import java.util.HashMap;

import com.siondream.engine.State;
import com.siondream.engine.StateFactory;


public class StateFactoryImpl implements StateFactory {
	
	private HashMap<String, State> m_states;
	private int m_numStates;
	
	public StateFactoryImpl () {
		m_states = new HashMap<String, State>();
		
		// Add states
		m_states.put("StateGame", new StateGame());
		m_states.put("StateMenu", new StateMenu());
		
		m_numStates = m_states.size();
	}

	@Override
	public State getState(String name) {
		return m_states.get(name);
	}

	@Override
	public int getNumStates() {
		return m_numStates;
	}
}
