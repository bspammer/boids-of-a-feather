package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.Random;

public class Boid {
    public static final float MAX_SPEED = 200f;
    public static final float MIN_DISTANCE = 20f;
    public static final float VISION_RANGE = 90f;
    public static final float CORRECTION_RATE = 1f;
    public static final float ACCELERATION = 1.01f;
    public static final float PI = (float)Math.PI;

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

    public void update(float dt, List<Vector2> nearbyBoidPositions, List<Vector2> nearbyBoidVelocities) {
        // Sanity check
        if (nearbyBoidPositions.size() != nearbyBoidVelocities.size()) {
            throw new IllegalStateException("Position list and velocity list not of equal length.");
        }

        Vector2 myPosition = this.getPosition();
        Vector2 myVelocity = this.getVelocity();

        if (nearbyBoidPositions.size() > 0) {
            Vector2 avgVelocity = new Vector2(0, 0);
            for (Vector2 boidVelocity : nearbyBoidVelocities) {
                boidVelocity.scl(1/boidVelocity.len());
                avgVelocity.add(boidVelocity);
            }
            avgVelocity.scl(1/avgVelocity.len());
            avgVelocity.scl(CORRECTION_RATE);
            myVelocity.add(avgVelocity);
        }

        for (Vector2 boidPosition : nearbyBoidPositions) {
            Vector2 displacement = this.boidDisplacement(boidPosition);
            float distance = displacement.len();
            if (distance > MIN_DISTANCE) {
                displacement.scl(-1f);
            }
            // Normalize the displacement direction
            if (distance > 0) {
                displacement.scl(1/distance);
            }
            displacement.scl(CORRECTION_RATE);
            myVelocity.add(displacement);
        }

        float currentSpeed = myVelocity.len();
        if (currentSpeed > MAX_SPEED) {
            myVelocity.scl(MAX_SPEED/currentSpeed);
        } else {
            myVelocity.scl(ACCELERATION);
        }

        myPosition.add(myVelocity.cpy().scl(dt));

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        if (myPosition.x < 0) {
            myPosition.x += screenWidth;
        }
        if (myPosition.x > screenWidth) {
            myPosition.x -= screenWidth;
        }
        if (myPosition.y < 0) {
            myPosition.y += screenHeight;
        }
        if (myPosition.y > screenHeight) {
            myPosition.y -= screenHeight;
        }
        this.setPosition(myPosition);
        this.setVelocity(myVelocity);
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
        return new Vector2(xDisplacement, yDisplacement);
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