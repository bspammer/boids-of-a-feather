package com.charliecollard.boids;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class BoidSimulator extends ApplicationAdapter {
    public static final int PLOT_UPDATE_PERIOD = 1; // update the plot every n ticks
    public static int simulationWidth = 880;
    public static int simulationHeight = 880;
    public static int updateCount = 0;
    public static int boidCount = 200;
    public static WrappingScheme wrappingScheme = new PeriodicWrappingScheme();
    public static int updateMode = Boid.UPDATE_DETERMINISTIC;
    public static String filepathToLoad;
    public static boolean zoomOut = false;
    private static boolean debugCircles = false;
    private static boolean debugFluctuations = false;
    public static boolean debugCorrelations = false;
    private static boolean debugInfluences = false;
    protected static boolean debugBoidColorsOn = true;
    public static boolean renderingOn = true;
    protected static OrthographicCamera cam;
    protected static OrthographicCamera zoomedOutCam;
    private static int plotUpdateCounter = 0;
    private SpriteBatch sb;
    private List<Boid> boidList = new ArrayList<>();
    private List<Vector2> velocityList = new ArrayList<>();
    private Vector2 avgVelocity = new Vector2(0, 0);
    private float polarization = 0;
    private ArrayBlockingQueue<Float> fpsQueue = new ArrayBlockingQueue<>(100);
    private float lastFps = 0;
    BitmapFont font;
    private static PlotFrame plotFrame;

    public BoidSimulator() {
        if (debugCorrelations) {
            plotFrame = new PlotFrame("Plot frame");
            plotFrame.setVisible(true);
        }

        // Create boids or load them from disk if specified
        if (filepathToLoad == null) {
            for (int i = 0; i < boidCount; i++) {
                boidList.add(new Boid(wrappingScheme));
            }
        } else {
            loadFromFile(filepathToLoad);
        }
    }

	@Override
	public void create() {
        for (Boid boid : boidList) {
            boid.loadSprite();
        }
		sb = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.GREEN);
        cam = new OrthographicCamera(simulationWidth, simulationHeight);
        cam.position.set(simulationWidth / 2f, simulationHeight / 2f, 0);
        cam.update();
        zoomedOutCam = new OrthographicCamera(simulationWidth, simulationHeight);
        zoomedOutCam.position.set(simulationWidth/2f, simulationHeight/2f, 0);
        zoomedOutCam.viewportWidth = simulationWidth*3;
        zoomedOutCam.viewportHeight = simulationHeight*3;
        zoomedOutCam.update();
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

                if (keycode == Input.Keys.U) {
                    debugInfluences = !debugInfluences;
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
                    Vector2 clickPosition = new Vector2(screenX, simulationHeight - screenY);
                    if (zoomOut) {
                        Vector3 projection = zoomedOutCam.unproject(new Vector3(clickPosition, 0));
                        clickPosition.x = projection.x;
                        clickPosition.y = simulationHeight - projection.y;
                    }
                    boidList.add(new Boid(clickPosition, wrappingScheme));
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
                Vector2 clickPosition = new Vector2(screenX, simulationHeight - screenY);
                if (zoomOut) {
                    Vector3 projection = zoomedOutCam.unproject(new Vector3(clickPosition, 0));
                    clickPosition.x = projection.x;
                    clickPosition.y = simulationHeight - projection.y;
                }
                boidList.add(new Boid(clickPosition, wrappingScheme));
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
            boid.update(renderingOn ? Gdx.graphics.getDeltaTime() : 0, neighbourPositionMap.get(boid), neighbourVelocityMap.get(boid));
            boid.performWrapping();
            velocityList.add(boid.getVelocity());
        }

        // Recalculate the average velocity and polarization for the whole system
        avgVelocity = new Vector2(0, 0);
        Vector2 sumOfNormalized = new Vector2(0, 0);
        for (Vector2 boidVelocity : velocityList) {
            avgVelocity.add(boidVelocity);
            sumOfNormalized.add(boidVelocity.nor());
        }
        if (velocityList.size() > 0) {
            avgVelocity.scl(1f/velocityList.size());
            sumOfNormalized.scl(1f/velocityList.size());
            polarization = sumOfNormalized.len();
        }

        // Update the plot
        if (debugCorrelations && plotFrame != null) {
            if (plotUpdateCounter == 0) {
                updatePlot(boidList, avgVelocity);
            }
            plotUpdateCounter += 1;
            plotUpdateCounter %= PLOT_UPDATE_PERIOD;
        }
        updateCount += 1;

        // Recalculate fps
        lastFps = 1f/Gdx.graphics.getDeltaTime();
        if (!fpsQueue.offer(lastFps)) {
            fpsQueue.poll();
            fpsQueue.add(lastFps);
        }
        lastFps = 0;
        for (float f : fpsQueue) {
            lastFps += f;
        }
        lastFps /= fpsQueue.size();
    }

	@Override
	public void render() {
        update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (zoomOut) {
            DebugShapeRenderer.drawLine(new Vector2(0, -simulationHeight), new Vector2(0, 2 * simulationHeight), 1, Color.WHITE, zoomedOutCam.combined);
            DebugShapeRenderer.drawLine(new Vector2(simulationWidth, -simulationHeight), new Vector2(simulationWidth, 2 * simulationHeight), 1, Color.WHITE, zoomedOutCam.combined);
            DebugShapeRenderer.drawLine(new Vector2(-simulationWidth, 0), new Vector2(2 * simulationWidth, 0), 1, Color.WHITE, zoomedOutCam.combined);
            DebugShapeRenderer.drawLine(new Vector2(-simulationWidth, simulationHeight), new Vector2(2 * simulationWidth, simulationHeight), 1, Color.WHITE, zoomedOutCam.combined);
        }

        if (debugCircles) {
            DebugShapeRenderer.startBatch(Color.GREEN, 1, zoomOut ? zoomedOutCam.combined : cam.combined);
            for (Boid boid : boidList) {
                DebugShapeRenderer.batchCircle(boid.getPosition(), Boid.visionRange);
            }
            DebugShapeRenderer.endBatch();
        }

        if (debugFluctuations) {
            DebugShapeRenderer.startBatch(Color.PINK, 1, zoomOut ? zoomedOutCam.combined : cam.combined);
            for (Boid boid : boidList) {
                Vector2 boidFluctuation = boid.getVelocity().sub(avgVelocity);
                DebugShapeRenderer.batchLine(boid.getPosition(), boid.getPosition().add(boidFluctuation));
            }
            DebugShapeRenderer.endBatch();
        }

        if (debugInfluences) {
            DebugShapeRenderer.startBatch(Color.YELLOW, 1, zoomOut ? zoomedOutCam.combined : cam.combined);
            for (Boid boid : boidList) {
                DebugShapeRenderer.batchLine(boid.getPosition(), boid.getPosition().add(boid.lastSeparation.cpy().scl(10)), Color.YELLOW);
                DebugShapeRenderer.batchLine(boid.getPosition(), boid.getPosition().add(boid.lastCohesion.cpy().scl(20)), Color.MAGENTA);
                DebugShapeRenderer.batchLine(boid.getPosition(), boid.getPosition().add(boid.lastAlignment.cpy().scl(20)), Color.CYAN);
            }
            DebugShapeRenderer.endBatch();
        }

        sb.setProjectionMatrix(zoomOut ? zoomedOutCam.combined : cam.combined);
        sb.begin();
        List<Disposable> trashcan = new ArrayList<>();

        for (Boid boid : boidList) {
            boid.render(sb);
        }


        sb.setProjectionMatrix(cam.combined);
        font.draw(sb, boidList.size() + " boids", 10, simulationHeight - 20);
        font.draw(sb, String.format("%.2f fps", lastFps), 10, simulationHeight - 40);
        font.draw(sb, String.format("Polarization: %.3f", polarization), 10, 100);
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

    private static void updatePlot(List<Boid> boidList, Vector2 avgVelocity) {
	    // Only update the plot if the previous thread isn't still running
	    if (UpdatePlotThread.mThread == null || !UpdatePlotThread.mThread.isAlive()) {
            List<Vector2> positions = new ArrayList<>();
            List<Vector2> velocities = new ArrayList<>();
            for (Boid boid : boidList) {
                positions.add(boid.getPosition());
                velocities.add(boid.getVelocity());
            }
            UpdatePlotThread updatePlotThread = new UpdatePlotThread(plotFrame, positions, velocities, avgVelocity);
            updatePlotThread.start();
        }
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
