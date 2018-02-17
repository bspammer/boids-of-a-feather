package com.charliecollard.boids;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.util.*;

public class BoidSimulator extends ApplicationAdapter {
	public static final int BOID_COUNT = 50;

    public static boolean debug_circles = false;

	private SpriteBatch sb;
	private List<Boid> boidList = new ArrayList<Boid>();

	@Override
	public void create() {
		sb = new SpriteBatch();
		for (int i = 0; i < BOID_COUNT; i++) {
			boidList.add(new Boid());
		}
//        boidList.add(new Boid(new Vector2(500, 500), new Vector2(0, 200)));
//        boidList.add(new Boid(new Vector2(500, 500.01f), new Vector2(0, -200)));
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    debug_circles = !debug_circles;
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    Vector2 boidPosition = new Vector2(screenX, Gdx.graphics.getHeight()-screenY);
                    boidList.add(new Boid(boidPosition));
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }

            @Override
            public boolean scrolled(int amount) {
                return false;
            }
        });
	}

    public void update() {
        Map<Boid, List<Vector2>> neighbourPositionMap = new HashMap<Boid, List<Vector2>>();
        Map<Boid, List<Vector2>> neighbourVelocityMap = new HashMap<Boid, List<Vector2>>();
        // For each boid, discover all close boids without updating them
        for (Boid boid : boidList) {
            List<Vector2> neighbourPositions = new ArrayList<Vector2>();
            List<Vector2> neighbourVelocities = new ArrayList<Vector2>();
            for (Boid otherBoid : boidList) {
                float distanceFromOther = boid.boidDisplacement(otherBoid).len();
                if (!boid.equals(otherBoid) && distanceFromOther < Boid.VISION_RANGE) {
                    neighbourPositions.add(otherBoid.getPosition());
                    neighbourVelocities.add(otherBoid.getVelocity());
                }
            }
            neighbourPositionMap.put(boid, neighbourPositions);
            neighbourVelocityMap.put(boid, neighbourVelocities);
        }

        // Update each boid by passing it the nearby positions of other boids
        for (Boid boid : boidList) {
            boid.update(Gdx.graphics.getDeltaTime(), neighbourPositionMap.get(boid), neighbourVelocityMap.get(boid));
        }
    }

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update();
		sb.begin();
        List<Disposable> trashcan = new ArrayList<Disposable>();
        if (debug_circles) {
            Pixmap debugCircles = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
            trashcan.add(debugCircles);

            for (Boid boid : boidList) {
                Vector2 boidPosition = boid.getPosition();
                int x = (int)boidPosition.x;
                int y = (int)boidPosition.y;
                debugCircles.setColor(Color.GREEN);
                debugCircles.drawCircle(x, Gdx.graphics.getHeight()-y, (int)Boid.VISION_RANGE);
                debugCircles.setColor(Color.RED);
                debugCircles.drawCircle(x, Gdx.graphics.getHeight()-y, (int)Boid.MIN_DISTANCE);
            }
            Texture debugCircleTexture = new Texture(debugCircles);
            trashcan.add(debugCircleTexture);
            sb.draw(debugCircleTexture, 0, 0);
        }

        for (Boid boid : boidList) {
            boid.render(sb);
        }
		sb.end();

        // Take out the trash
        for (Disposable trash : trashcan) {
            trash.dispose();
        }
        trashcan.clear();
	}

	@Override
	public void dispose() {
		sb.dispose();
		TextureController.getInstance().disposeAllTextures();
	}
}
