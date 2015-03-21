package com.siondream.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;

public class StateManager {
	
	private static class Operation {
		public enum Type {Push, Pop};
		public Type type;
		public String stateName;
		
		public Operation(Type type, String stateName) {
			this.stateName = stateName;
			this.type = type;
		}
	}
	
	private static final Logger m_logger = Game.getLogger();
	private StateFactory m_stateFactory = null;
	private Array<State> m_statesStack = new Array<State>();
	private Array<Operation> m_pendingOperations = new Array<Operation>();
	private int m_numStates = 0;
	
	public StateManager() {}
	
	public void update(float deltaT) {
		// Iterate over states
		for (int i = 0; i < m_numStates; ++i) {
			State state = m_statesStack.get(i);
			
			// If is active and loaded, update and render
			if (state.isActive() && state.isLoaded()) {
				state.update(deltaT);
			}
		}
		
		// Safely perform state operations
		performOperations();
	}
	
	public void pushState(String stateName) {
		m_pendingOperations.add(new Operation(Operation.Type.Push, stateName));
	}
	
	public void popState() {
		m_pendingOperations.add(new Operation(Operation.Type.Pop, ""));
	}
	
	public void changeState(String stateName) {
		popState();
		pushState(stateName);
	}
	
	public State getState(String name) {
		for (int i = 0; i < m_numStates; ++i) {
			State state = m_statesStack.get(i);
			if (state.getName().equals(name)) {
				return state;
			}
		}
		
		return null;
	}
	
	public State getStateFromRegistry(String name) {
		if (m_stateFactory != null) {
			return m_stateFactory.getState(name);
		}
		
		return null;
		
	}
	
	public void pause() {
		for (int i = 0; i < m_numStates; ++i) {
			m_statesStack.get(i).pause();
		}
	}
	
	public void resume() {
		for (int i = 0; i < m_numStates; ++i) {
			m_statesStack.get(i).resume();
		}
	}
	
	public void setStateFactory(StateFactory stateFactory) {
		m_stateFactory = stateFactory;
	}
	
	public void onEvent(Object sender, int type, Object data) {
		for (int i = 0; i < m_numStates; ++i) {
			m_statesStack.get(i).onEvent(sender, type, data);
		}
	}
	
	private void performOperations() {
		int numOperations = m_pendingOperations.size;
		
		// Iterate over pending operations
		for (int i = 0; i < numOperations; ++i) {
			Operation operation = m_pendingOperations.get(i);
			
			// Depending on the type, perform the operation
			if (operation.type == Operation.Type.Push) {
				performPush(operation.stateName);
			}
			else if (operation.type == Operation.Type.Pop) {
				performPop();
			}
			else {
				m_logger.error("StateManager: operation not managed");
			}
		}
		
		m_pendingOperations.clear();
	}
	
	private void performPush(String stateName) {
		State state = m_stateFactory.getState(stateName);
		
		// If the state is valid
		if (state != null) {
			m_logger.info("StateManager: pushing state " + state.getName());
			
			// If the state was already in the stack
			int i = m_statesStack.indexOf(state, true);
			if (i != -1) {
				m_logger.info("StateManager: state already in active stack");
				m_statesStack.removeIndex(i);
			}
			else {
				++m_numStates;
			}
			
			// Push the state at the end of the stack
			m_statesStack.add(state);
			
			// Enable the state
			state.load();
			state.setActive(true);
			Gdx.input.setInputProcessor(state/*.getGestureDetector()*/);
		}
		else {
			m_logger.error("StateManager: couldn't find " + stateName + " in the registry");
		}
	}
	
	private void performPop() {
		if (m_numStates > 0){
			State state = m_statesStack.get(m_numStates - 1);
			m_logger.info("StateManager: pop state " + state.getName());
			state.dispose();
			m_statesStack.removeIndex(m_numStates - 1);
			--m_numStates;
			
			if (m_numStates > 0) {
				state = m_statesStack.get(m_numStates - 1);
				state.setActive(true);
				state.load();
				Gdx.input.setInputProcessor(state.getGestureDetector());
			}
		}
	}
}
