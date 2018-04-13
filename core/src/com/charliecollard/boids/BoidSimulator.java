package com.charliecollard.boids;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class BoidSimulator extends ApplicationAdapter {
    public static final int PLOT_UPDATE_PERIOD = 50; // update the plot every n ticks
    public static final boolean PLOT_ENABLED = false;

    public static int boidCount = 200;
    public static WrappingScheme wrappingScheme = new PeriodicWrappingScheme();
    public static int updateMode = Boid.UPDATE_DETERMINISTIC;
    public static String filepathToLoad;
    public static boolean zoomOut = false;
    private static boolean debugCircles = false;
    private static boolean debugFluctuations = false;
    private static boolean debugCorrelations = false;
    private static int correlationInterval = 10;
    private static int correlationNumber = 40;
    private static ArrayList<ArrayList<Float>> correlationLists = new ArrayList<>();
    protected static boolean debugBoidColorsOn = true;
    private static int plotUpdateCounter = 0;
    private SpriteBatch sb;
    private List<Boid> boidList = new ArrayList<>();
    private List<Vector2> velocityList = new ArrayList<>();
    private Vector2 avgVelocity = new Vector2(0, 0);
    BitmapFont font;
    private static PlotFrame plotFrame;
    private OrthographicCamera cam;

	@Override
	public void create() {
        if (PLOT_ENABLED) {
            plotFrame = new PlotFrame("Plot frame");
            plotFrame.setVisible(true);
        }

        for (int i=0; i<correlationNumber; i++) {
            correlationLists.add(new ArrayList<Float>());
        }

		sb = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.GREEN);
        cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f, 0);
        cam.update();

        // Create boids or load them from disk if specified
        if (filepathToLoad == null) {
            for (int i = 0; i < boidCount; i++) {
                boidList.add(new Boid(wrappingScheme));
            }
        } else {
            loadFromFile(filepathToLoad);
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

                if (keycode == Input.Keys.I) {
                    debugCorrelations = !debugCorrelations;
                    return true;
                }

                if (keycode == Input.Keys.Y) {
                    zoomOut = !zoomOut;
                    return true;
                }

                if (keycode == Input.Keys.W) {
                    writeBoids();
                    return true;
                }

                if (keycode == Input.Keys.R) {
                    loadFromFile();
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
                    boidList.add(new Boid(boidPosition, wrappingScheme));
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
                Vector2 boidPosition = new Vector2(screenX, Gdx.graphics.getHeight()-screenY);
                boidList.add(new Boid(boidPosition, wrappingScheme));
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
        for (ArrayList<Float> list : correlationLists) {
            list.clear();
        }
        // For each boid, discover all close boids without updating them
        // This way we update all boids in synchronisation
        for (Boid boid : boidList) {
            List<Vector2> neighbourPositions = new ArrayList<>();
            List<Vector2> neighbourVelocities = new ArrayList<>();
            for (Boid otherBoid : boidList) {
                if (!boid.equals(otherBoid)) {
                    Vector2 relativeDisplacement = boid.relativeDisplacement(otherBoid);
                    Vector2 relativeVelocity = boid.relativeVelocity(otherBoid);
                    float distance = relativeDisplacement.len();
                    if (distance < Boid.visionRange) {
                        // We give the boid its relative displacement to the neighbouring boids
                        // This allows calculations to be done regardless of the screen wrapping
                        neighbourPositions.add(relativeDisplacement);
                        neighbourVelocities.add(relativeVelocity);
                    }
                    distances.add(distance);
                }
            }
            neighbourPositionMap.put(boid, neighbourPositions);
            neighbourVelocityMap.put(boid, neighbourVelocities);
        }

        velocityList.clear();
        // Update each boid by passing it the nearby positions of other boids
        for (Boid boid : boidList) {
            boid.update(Gdx.graphics.getDeltaTime(), neighbourPositionMap.get(boid), neighbourVelocityMap.get(boid));
            boid.performWrapping();
            velocityList.add(boid.getVelocity());
        }

        // Recalculate the average velocity for the whole system
        avgVelocity = new Vector2(0, 0);
        for (Vector2 boidVelocity : velocityList) {
            avgVelocity.add(boidVelocity);
        }
        avgVelocity.scl(1f/boidList.size());

        if (debugCorrelations) {
            for (Boid boid : boidList) {
                for (Boid otherBoid : boidList) {
                    float distance = boid.relativeDisplacement(otherBoid).len();
                    if (distance < correlationInterval * correlationNumber) {
                        Vector2 boidFluctuation = boid.getVelocity().sub(avgVelocity);
                        Vector2 otherBoidFluctuation = otherBoid.getVelocity().sub(avgVelocity);
                        float dotProduct = boidFluctuation.dot(otherBoidFluctuation);
                        int index = (int) Math.floor(distance / correlationInterval);
                        ArrayList<Float> list = correlationLists.get(index);
                        list.add(dotProduct);
                        correlationLists.set(index, list);
                    }
                }
            }
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
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update();
        cam.viewportWidth = screenWidth;
        cam.viewportHeight = screenHeight;
        cam.update();
        if (zoomOut) {
            cam.viewportWidth = screenWidth*3;
            cam.viewportHeight = screenHeight*3;
            cam.update();
            DebugShapeRenderer.drawLine(new Vector2(0, -screenHeight), new Vector2(0, 2 * screenHeight), 1, Color.WHITE, cam.combined);
            DebugShapeRenderer.drawLine(new Vector2(screenWidth, -screenHeight), new Vector2(screenWidth, 2 * screenHeight), 1, Color.WHITE, cam.combined);
            DebugShapeRenderer.drawLine(new Vector2(-screenWidth, 0), new Vector2(2 * screenWidth, 0), 1, Color.WHITE, cam.combined);
            DebugShapeRenderer.drawLine(new Vector2(-screenWidth, screenHeight), new Vector2(2 * screenWidth, screenHeight), 1, Color.WHITE, cam.combined);
        }

        sb.setProjectionMatrix(cam.combined);
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
                circlePixmap.drawCircle(x, Gdx.graphics.getHeight()-y, (int)Boid.visionRange);
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

            for (Boid boid : boidList) {
                Vector2 boidPosition = boid.getPosition();
                Vector2 boidFluctuation = boid.getVelocity().sub(avgVelocity);
                float fluctuationSize = boidFluctuation.len();
                float angle = boidFluctuation.angleRad();
                int x = (int) boidPosition.x;
                int y = (int) (Gdx.graphics.getHeight() - boidPosition.y);
                int x2 = (int) (x + (fluctuationSize * Math.cos(angle)));
                int y2 = (int) (y - (fluctuationSize * Math.sin(angle)));
                fluctuationPixmap.setColor(Color.CYAN);
                fluctuationPixmap.drawLine(x, y, x2, y2);
                boid.render(sb);
            }
            Texture fluctuationTexture = new Texture(fluctuationPixmap);
            trashcan.add(fluctuationTexture);
            sb.draw(fluctuationTexture, 0, 0);
        } else {
            for (Boid boid : boidList) {
                boid.render(sb);
            }
        }

        font.draw(sb, boidList.size() + " boids", 10, 100);
        font.draw(sb, "Separation:", 10, 60);
        font.draw(sb, String.format("%.3f", Boid.separationWeight * Boid.WEIGHT_SCALING_FACTOR), 90, 60);
        font.draw(sb, "Cohesion:", 10, 40);
        font.draw(sb, String.format("%.3f", Boid.cohesionWeight * Boid.WEIGHT_SCALING_FACTOR), 90, 40);
        font.draw(sb, "Alignment:", 10, 20);
        font.draw(sb, String.format("%.3f", Boid.alignmentWeight * Boid.WEIGHT_SCALING_FACTOR), 90, 20);

        if (debugCorrelations) {
            ArrayList<Float> sums = new ArrayList<>();
            for (ArrayList<Float> list : correlationLists) {
                float sum = 0;
                for (Float f : list) {
                    sum += f;
                }
                if (list.size() > 0) sum /= list.size();
                sums.add(sum);
            }
            System.out.println(sums.toString());
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

    private static void updatePlot(List<Float> distances) {
        UpdatePlotThread updatePlotThread = new UpdatePlotThread(plotFrame, distances);
        updatePlotThread.start();
    }

    private void writeBoids() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        String filePath = "./simulation-saves/" + dateFormat.format(now) + ".ser";
	    try {
	        File savesDir = new File("./simulation-saves");
            if (!savesDir.exists()) {
	            savesDir.mkdir();
            }
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(boidList);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File folder = new File("./simulation-saves");
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".ser");
            }
        };
        File[] fileArray = folder.listFiles(filter);
        if (fileArray != null && fileArray.length > 0) {
            List<File> fileList = Arrays.asList(fileArray);
            Collections.sort(fileList);
            Collections.reverse(fileList);
            loadFromFile(fileList.get(0).getPath());
            System.out.println("Loaded file " + fileList.get(0).getPath());
        } else {
            System.out.println("Can't find any .ser files in the simulation-saves directory");
        }
    }

    private void loadFromFile(String filepath) {
	    try {
            FileInputStream fileIn = new FileInputStream(filepath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            List<Boid> newBoidList = (ArrayList<Boid>) in.readObject();
            boidList.clear();
            for (Boid boid : newBoidList) {
                boidList.add(new Boid(boid.getPosition(), boid.getVelocity(), wrappingScheme));
            }
        } catch (IOException | ClassNotFoundException e) {
	        e.printStackTrace();
        }
    }
}
