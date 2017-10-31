package com.charliecollard.boids;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BoidSimulator extends ApplicationAdapter {
	public static final int BOID_COUNT = 50;

	SpriteBatch sb;
	List<Boid> boidList = new ArrayList<Boid>();

	@Override
	public void create() {
		sb = new SpriteBatch();
		for (int i = 0; i < BOID_COUNT; i++) {
			boidList.add(new Boid());
		}
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		sb.begin();
        for (Boid boid : boidList) {
            List<Boid> closeBoids = new ArrayList<Boid>();
            for (Boid closeBoid : boidList) {
                if (boid.getPosition().dst(closeBoid.getPosition()) < 80) {
                    closeBoids.add(closeBoid);
                }
            }
            boid.update(0.02f, closeBoids);
            boid.render(sb);
        }
		sb.end();
	}
	
	@Override
	public void dispose() {
		sb.dispose();
		TextureController.getInstance().disposeAllTextures();
	}
}
