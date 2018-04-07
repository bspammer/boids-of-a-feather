package com.charliecollard.boids;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.util.*;

public class BoidSimulator extends ApplicationAdapter {
	public static final int BOID_COUNT = 100;
    public static final int PLOT_UPDATE_PERIOD = 50; // update the plot every n ticks
    private static final boolean PLOT_ENABLED = true;
    private static final int WRAP_MODE = Boid.WRAP_PACMAN;

    private static boolean debugCircles = false;
    private static boolean debugFluctuations = false;
    protected static boolean debugBoidColorsOn = true;

    private static int plotUpdateCounter = 0;

	private SpriteBatch sb;
	private List<Boid> boidList = new ArrayList<Boid>();
    BitmapFont font;

    private static PlotFrame plotFrame;

	@Override
	public void create() {
        if (PLOT_ENABLED) {
            plotFrame = new PlotFrame("Plot frame");
            plotFrame.setVisible(true);
        }

		sb = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.GREEN);
		for (int i = 0; i < BOID_COUNT; i++) {
			boidList.add(new Boid());
		}
//        boidList.add(new Boid(new Vector2(440, 870), new Vector2(200, 0)));
//        boidList.add(new Boid(new Vector2(870, 440), new Vector2(0, -200)));
//        boidList.add(new Boid(new Vector2(440, 440), new Vector2(400, 400)));
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    debugCircles = !debugCircles;
                    return true;
                }

                if (keycode == Input.Keys.O) {
                    debugFluctuations = !debugFluctuations;
                    return true;
                }

                // Change steer weights
                if (keycode == Input.Keys.A) {
                    Boid.separationWeight += 1;
                    return true;
                }
                if (keycode == Input.Keys.Z) {
                    if (Boid.separationWeight > 0) Boid.separationWeight -= 1;
                    return true;
                }
                if (keycode == Input.Keys.S) {
                    Boid.cohesionWeight += 1;
                    return true;
                }
                if (keycode == Input.Keys.X) {
                    if (Boid.cohesionWeight > 0) Boid.cohesionWeight -= 1;
                    return true;
                }
                if (keycode == Input.Keys.D) {
                    Boid.alignmentWeight += 1;
                    return true;
                }
                if (keycode == Input.Keys.C) {
                    if (Boid.alignmentWeight > 0) Boid.alignmentWeight -= 1;
                    return true;
                }

                if (keycode == Input.Keys.P) {
                    for (Boid boid : boidList) {
                        if (debugBoidColorsOn) {
                           boid.boidSprite.setColor(1f, 1f, 1f, 1f);
                        } else {
                            boid.boidSprite.setColor(boid.spriteColor);
                        }
                    }
                    debugBoidColorsOn = !debugBoidColorsOn;
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
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                Vector2 boidPosition = new Vector2(screenX, Gdx.graphics.getHeight()-screenY);
                boidList.add(new Boid(boidPosition));
                return true;
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
        Map<Boid, List<Vector2>> neighbourPositionMap = new HashMap<>();
        Map<Boid, List<Vector2>> neighbourVelocityMap = new HashMap<>();
        List<Float> distances = new ArrayList<>();
        // For each boid, discover all close boids without updating them
        // This way we update all boids in synchronisation
        for (Boid boid : boidList) {
            List<Vector2> neighbourPositions = new ArrayList<>();
            List<Vector2> neighbourVelocities = new ArrayList<>();
            for (Boid otherBoid : boidList) {
                Vector2 relativeDisplacement = boid.relativeDisplacement(otherBoid, WRAP_MODE);
                Vector2 relativeVelocity = boid.relativeVelocity(otherBoid, WRAP_MODE);
                float distance = relativeDisplacement.len();
                if (!boid.equals(otherBoid) && distance < Boid.VISION_RANGE) {
                    // We give the boid its relative displacement to the neighbouring boids
                    // This allows calculations to be done regardless of the screen wrapping
                    neighbourPositions.add(relativeDisplacement);
                    neighbourVelocities.add(relativeVelocity);
                }
                distances.add(distance);
            }
            neighbourPositionMap.put(boid, neighbourPositions);
            neighbourVelocityMap.put(boid, neighbourVelocities);
        }

        // Update each boid by passing it the nearby positions of other boids
        for (Boid boid : boidList) {
            boid.update(Gdx.graphics.getDeltaTime(), neighbourPositionMap.get(boid), neighbourVelocityMap.get(boid));
            boid.performWrapping(WRAP_MODE);
        }

        // Update the plot
        if (plotFrame != null) {
            if (plotUpdateCounter == 0) {
                updatePlot(distances);
            }
            plotUpdateCounter += 1;
            plotUpdateCounter %= PLOT_UPDATE_PERIOD;
        }
    }

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update();
		sb.begin();
        List<Disposable> trashcan = new ArrayList<>();
        if (debugCircles) {
            Pixmap circlePixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
            trashcan.add(circlePixmap);

            for (Boid boid : boidList) {
                Vector2 boidPosition = boid.getPosition();
                int x = (int)boidPosition.x;
                int y = (int)boidPosition.y;
                circlePixmap.setColor(Color.GREEN);
                circlePixmap.drawCircle(x, Gdx.graphics.getHeight()-y, (int)Boid.VISION_RANGE);
//                debugCircles.setColor(Color.RED);
//                debugCircles.drawCircle(x, Gdx.graphics.getHeight()-y, (int)Boid.MIN_DISTANCE);
            }
            Texture debugCircleTexture = new Texture(circlePixmap);
            trashcan.add(debugCircleTexture);
            sb.draw(debugCircleTexture, 0, 0);
        }

        if (debugFluctuations) {
            Pixmap fluctuationPixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
            trashcan.add(fluctuationPixmap);

            Vector2 avgVelocity = new Vector2(0, 0);
            for (Boid boid : boidList) {
                Vector2 boidVelocity = boid.getVelocity();
                avgVelocity.add(boidVelocity);
                boid.render(sb);
            }
            avgVelocity.scl(1f/boidList.size());

            for (Boid boid : boidList) {
                Vector2 boidPosition = boid.getPosition();
                Vector2 boidFluctuation = boid.getVelocity().sub(avgVelocity);
                float angle = boidFluctuation.angleRad();
                int x = (int) boidPosition.x;
                int y = (int) (Gdx.graphics.getHeight() - boidPosition.y);
                int x2 = (int) (x + (boidFluctuation.len() * Math.cos(angle)));
                int y2 = (int) (y - (boidFluctuation.len() * Math.sin(angle)));
                fluctuationPixmap.setColor(Color.CYAN);
                fluctuationPixmap.drawLine(x, y, x2, y2);
            }
            Texture fluctuationTexture = new Texture(fluctuationPixmap);
            trashcan.add(fluctuationTexture);
            sb.draw(fluctuationTexture, 0, 0);
        } else {
            for (Boid boid : boidList) {
                boid.render(sb);
            }
        }

        font.draw(sb, boidList.size() + " boids", 10, 80);
        font.draw(sb, "Separation:", 10, 60);
        font.draw(sb, String.format("%.3f", Boid.separationWeight * Boid.WEIGHT_SCALING_FACTOR), 90, 60);
        font.draw(sb, "Cohesion:", 10, 40);
        font.draw(sb, String.format("%.3f", Boid.cohesionWeight * Boid.WEIGHT_SCALING_FACTOR), 90, 40);
        font.draw(sb, "Alignment:", 10, 20);
        font.draw(sb, String.format("%.3f", Boid.alignmentWeight * Boid.WEIGHT_SCALING_FACTOR), 90, 20);
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

    private static void updatePlot(List<Float> distances) {
        UpdatePlotThread updatePlotThread = new UpdatePlotThread(plotFrame, distances);
        updatePlotThread.start();
    }
}
