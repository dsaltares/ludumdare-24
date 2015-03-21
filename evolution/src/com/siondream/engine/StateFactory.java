package com.siondream.engine;

public interface StateFactory {
	
	public State getState(String name);
	public int getNumStates();
}
