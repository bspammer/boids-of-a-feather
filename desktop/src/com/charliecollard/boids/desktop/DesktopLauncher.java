package com.charliecollard.boids.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.charliecollard.boids.BoidSimulator;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1820;
//		config.width = 880;
		config.height = 880;
		config.x = 700;
		new LwjglApplication(new BoidSimulator(), config);
	}
}
