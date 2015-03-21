package com.siondream.evolution;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Evolution - Ludum Dare #24";
		cfg.useGL20 = true;
		cfg.width = 1280;
		cfg.height = 720;
		
		Evolution evolution = new Evolution();
		Evolution.setPlatformResolver(new DesktopResolver());
		
		new LwjglApplication(evolution, cfg);
	}
}
