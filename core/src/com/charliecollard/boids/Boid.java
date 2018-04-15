package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.List;

import static com.charliecollard.boids.BoidSimulator.simulationWidth;
import static com.charliecollard.boids.BoidSimulator.simulationHeight;

public class Boid implements Serializable {
    public static final float MAX_SPEED = 300f;
    public static final float PI = (float) Math.PI;
    public static final int UPDATE_DETERMINISTIC = 201;
    public static final int UPDATE_TIMED = 202;

    public static float visionRange = 40f;
    public static String boidTexture = TextureController.BOID;
    public static int separationWeight = 60;
    public static int cohesionWeight = 20;
    public static int alignmentWeight = 1;
    public static int boidSusceptibility = 20;
    public static final float WEIGHT_SCALING_FACTOR = 0.005f;

    private Vector2 position;
    private Vector2 velocity;
    protected transient Sprite boidSprite;
    protected transient Color spriteColor;
    protected transient WrappingScheme wrappingScheme;
    protected transient Vector2 lastSeparation;
    protected transient Vector2 lastCohesion;
    protected transient Vector2 lastAlignment;

    public Boid() {
        this(new PeriodicWrappingScheme());
    }

    public Boid(WrappingScheme wrappingScheme) {
        Random rand = new Random();

        try {
            loadSprite();
        } catch (NullPointerException e) {
            // We're probably in headless mode
        }

        float heading = rand.nextFloat() * 2 * PI;
//        float heading = 0;
//        this.setPosition(new Vector2(simulationWidth/2,simulationHeight/2));
        this.wrappingScheme = wrappingScheme;
        this.setPosition(new Vector2(rand.nextFloat() * simulationWidth, rand.nextFloat() * simulationHeight));
        this.setVelocity(new Vector2((float) (MAX_SPEED * Math.sin(heading)), (float) (MAX_SPEED * Math.cos(heading))));
    }

    public Boid(Vector2 startPosition, WrappingScheme wrappingScheme) {
        this(wrappingScheme);
        this.setPosition(startPosition);
    }

    public Boid(Vector2 startPosition, Vector2 startVelocity, WrappingScheme wrappingScheme) {
        this(wrappingScheme);
        this.setPosition(startPosition);
        this.setVelocity(startVelocity);
    }

    public void loadSprite() {
        if (BoidSimulator.renderingOn) {
            Random rand = new Random();
            boidSprite = new Sprite(TextureController.getInstance().getTexture(boidTexture));
            boidSprite.setOrigin(boidSprite.getWidth() / 2, boidSprite.getHeight() / 2);
            float hue = rand.nextFloat();
            float saturation = (rand.nextInt(2000) + 1000) / 2000f;
            float luminance = 0.9f;
            java.awt.Color tempColor = java.awt.Color.getHSBColor(hue, saturation, luminance);
            spriteColor = new Color(((tempColor.getRGB() & 0xffffff) << 8) | 0xff);
            if (BoidSimulator.debugBoidColorsOn) boidSprite.setColor(spriteColor);
            boidSprite.setScale(0.3f);
        }
    }

    public void update(float deltaTime, List<Vector2> nearbyBoidDisplacements, List<Vector2> nearbyBoidVelocities) {
        // Sanity check
        if (nearbyBoidDisplacements.size() != nearbyBoidVelocities.size()) {
            throw new IllegalStateException("Position list and velocity list not of equal length.");
        }
        Vector2 newPosition = this.getPosition();
        Vector2 newVelocity = this.getVelocity();

        // Calculate separation steer
        Vector2 separation = new Vector2(0, 0);
        for (Vector2 boidDisplacement : nearbyBoidDisplacements) {
            float distanceSquared = boidDisplacement.len2();
            if (distanceSquared != 0) separation.add(boidDisplacement.cpy().scl(30/distanceSquared));
        }
        separation.scl(-1);

        // Calculate cohesion steer
        Vector2 cohesion;
        if (nearbyBoidDisplacements.size() == 0) {
            cohesion = new Vector2(0, 0);
        } else {
            Vector2 centreOfMass = new Vector2(0, 0);
            for (Vector2 boidDisplacement : nearbyBoidDisplacements) {
                centreOfMass.add(boidDisplacement);
            }
            centreOfMass.scl(1f/nearbyBoidDisplacements.size());
            cohesion = centreOfMass;
        }

        // Calculate alignment steer
        Vector2 alignment = new Vector2(0, 0);
        if (nearbyBoidVelocities.size() != 0) {
            for (Vector2 boidVelocity : nearbyBoidVelocities) {
                alignment.add(boidVelocity);
            }
            alignment.scl(1f / nearbyBoidVelocities.size());
        }

        Vector2 combinedSteer = new Vector2(0, 0);
        combinedSteer.add(separation.scl(separationWeight * WEIGHT_SCALING_FACTOR));
        lastSeparation = separation;
        combinedSteer.add(cohesion.scl(cohesionWeight * WEIGHT_SCALING_FACTOR));
        lastCohesion = cohesion;
        combinedSteer.add(alignment.scl(alignmentWeight * WEIGHT_SCALING_FACTOR));
        lastAlignment = alignment;
        if (combinedSteer.len() > 0) combinedSteer.scl(boidSusceptibility/combinedSteer.len());
        newVelocity.add(combinedSteer);
        if (newVelocity.len() > MAX_SPEED) newVelocity.scl(MAX_SPEED/newVelocity.len());
        newPosition.add(newVelocity.cpy().scl(BoidSimulator.updateMode == UPDATE_TIMED ? deltaTime : 0.02f));
        this.setPosition(newPosition);
        this.setVelocity(newVelocity);
    }

    public void render(SpriteBatch sb) {
        boidSprite.setScale(BoidSimulator.zoomOut ? 1 : 0.35f);
        boidSprite.setRotation(getVelocity().angle()-90);
        boidSprite.setPosition(getPosition().x-boidSprite.getWidth()/2, getPosition().y-boidSprite.getHeight()/2);
        boidSprite.draw(sb);
        if (BoidSimulator.zoomOut) {
            for (Pair<Vector2, Vector2> positionVelocityPair : wrappingScheme.getRenderingPositionsAndVelocities(this)) {
                Vector2 position = positionVelocityPair.getKey();
                Vector2 velocity = positionVelocityPair.getValue();
                boidSprite.setRotation(velocity.angle() - 90);
                boidSprite.setPosition(position.x - boidSprite.getWidth() / 2, position.y - boidSprite.getHeight() / 2);
                boidSprite.draw(sb);
            }
        }
    }

    public Vector2 relativeDisplacement(Boid other) {
        return wrappingScheme.relativeDisplacement(this, other);
    }

    public Vector2 relativeDisplacement(Vector2 otherPos) {
        otherPos = otherPos.cpy();
        return wrappingScheme.relativeDisplacement(this.getPosition(), otherPos);
    }

    public Vector2 relativeVelocity(Boid other) {
        return wrappingScheme.relativeVelocity(this, other);
    }

    public Vector2 relativeVelocity(Vector2 otherPos, Vector2 otherVel) {
        return wrappingScheme.relativeVelocity(this.getPosition(), otherPos, otherVel);
    }

    public void performWrapping() {
        wrappingScheme.performWrapping(this);
    }

    public void setPosition(Vector2 newPos) {
        this.position = newPos;
    }

    public void setPositionX(float newX) {
        this.position.x = newX;
    }

    public void setPositionY(float newY) {
        this.position.y = newY;
    }

    public void setVelocity(Vector2 newVel) {
        this.velocity = newVel;
    }

    public void setVelocityX(float newX) {
        this.velocity.x = newX;
    }

    public void setVelocityY(float newY) {
        this.velocity.y = newY;
    }

    public Vector2 getPosition() {
        return this.position.cpy();
    }

    public Vector2 getVelocity() {
        return this.velocity.cpy();
    }
}