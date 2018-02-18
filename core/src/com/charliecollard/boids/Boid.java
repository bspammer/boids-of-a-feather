package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.Random;

public class Boid {
    public static final float MAX_SPEED = 400f;
    public static final float MIN_DISTANCE = 40f;
    public static final float VISION_RANGE = 150f;
    public static final float CORRECTION_RATE = 1f;
    public static final float ACCELERATION = 1.01f;
    public static final float PI = (float)Math.PI;

    public static float separationWeight = 0.03f;
    public static float cohesionWeight = 0.03f;
    public static float alignmentWeight = 0.03f;

    private Vector2 position;
    private Vector2 velocity;
    Sprite boidSprite;

    public Boid() {
        Random rand = new Random();

        boidSprite = new Sprite(TextureController.getInstance().BOID);
        boidSprite.setOrigin(boidSprite.getWidth() / 2, boidSprite.getHeight() / 2);

        float heading = rand.nextFloat() * 4 * PI;
//        float heading = 0;
//        this.setPosition(new Vector2(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2));
        this.setPosition(new Vector2(rand.nextFloat() * Gdx.graphics.getWidth(), rand.nextFloat() * Gdx.graphics.getHeight()));
        this.setVelocity(new Vector2((float) (MAX_SPEED * Math.sin(heading)), (float) (MAX_SPEED * Math.cos(heading))));
    }

    public Boid(Vector2 startPosition) {
        this();
        this.setPosition(startPosition);
    }

    public Boid(Vector2 startPosition, Vector2 startVelocity) {
        this();
        this.setPosition(startPosition);
        this.setVelocity(startVelocity);
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
            separation.add(boidDisplacement);
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

        // Add the steering forces to current velocity, then calculate new position
        newVelocity.add(separation.scl(separationWeight));
        newVelocity.add(cohesion.scl(cohesionWeight));
        newVelocity.add(alignment.scl(alignmentWeight));
        if (newVelocity.len() > MAX_SPEED) newVelocity.scl(MAX_SPEED/newVelocity.len());
        newPosition.add(newVelocity.cpy().scl(deltaTime));

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        if (newPosition.x < 0) {
            newPosition.x += screenWidth;
        }
        if (newPosition.x > screenWidth) {
            newPosition.x -= screenWidth;
        }
        if (newPosition.y < 0) {
            newPosition.y += screenHeight;
        }
        if (newPosition.y > screenHeight) {
            newPosition.y -= screenHeight;
        }
        this.setPosition(newPosition);
        this.setVelocity(newVelocity);
    }

    public void render(SpriteBatch sb) {
        Vector2 currentPosition = this.getPosition();
        Vector2 currentVelocity = this.getVelocity();
        boidSprite.setRotation(currentVelocity.angle()-90);
        boidSprite.setPosition(currentPosition.x-boidSprite.getWidth()/2, currentPosition.y-boidSprite.getHeight()/2);
        boidSprite.draw(sb);
    }

    public Vector2 boidDisplacement(Boid other) {
        return boidDisplacement(other.getPosition());
    }

    // Takes into account screen wrapping
    public Vector2 boidDisplacement(Vector2 otherPos) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 myPos = this.getPosition();
        float xDisplacement, yDisplacement;
        float leftHorDist = Math.abs(myPos.x-(otherPos.x-screenWidth));
        float middleHorDist = Math.abs(myPos.x-otherPos.x);
        float rightHorDist = Math.abs(myPos.x-(otherPos.x+screenWidth));
        float bottomVerDist = Math.abs(myPos.y-(otherPos.y-screenHeight));
        float middleVerDist = Math.abs(myPos.y-otherPos.y);
        float topVerDist = Math.abs(myPos.y-(otherPos.y+screenHeight));
        if (leftHorDist < middleHorDist && leftHorDist < rightHorDist) {
            xDisplacement = myPos.x - (otherPos.x-screenWidth);
        } else if (middleHorDist < leftHorDist && middleHorDist < rightHorDist) {
            xDisplacement = myPos.x - otherPos.x;
        } else {
            xDisplacement = myPos.x - (otherPos.x + screenWidth);
        }
        if (bottomVerDist < middleVerDist && bottomVerDist < topVerDist) {
            yDisplacement = myPos.y - (otherPos.y-screenHeight);
        } else if (middleVerDist < bottomVerDist && middleVerDist < topVerDist) {
            yDisplacement = myPos.y - otherPos.y;
        } else {
            yDisplacement = myPos.y - (otherPos.y + screenHeight);
        }
        return new Vector2(-xDisplacement, -yDisplacement);
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