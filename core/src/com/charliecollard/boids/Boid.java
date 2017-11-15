package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.Random;

public class Boid {
    public static final float MAX_SPEED = 200f;
    public static final float MIN_DISTANCE = 40f;
    public static final float VISION_RANGE = 80f;
    public static final float CORRECTION_RATE = 0.1f;
    public static final float ACCELERATION = 1.01f;
    public static final float PI = (float) Math.PI;

    private Vector2 position;
    private Vector2 velocity;
    Sprite boidSprite;

    public Boid() {
        Random rand = new Random();

        boidSprite = new Sprite(TextureController.getInstance().BOID);
        boidSprite.setOrigin(boidSprite.getWidth() / 2, boidSprite.getHeight() / 2);

//        float heading = rand.nextFloat() * 4 * PI;
        float heading = 0;
//        this.setPosition(new Vector2(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2));
        this.setPosition(new Vector2(rand.nextFloat() * Gdx.graphics.getWidth(), rand.nextFloat() * Gdx.graphics.getHeight()));
        this.setVelocity(new Vector2((float) (MAX_SPEED * Math.sin(heading)), (float) (MAX_SPEED * Math.cos(heading))));
    }

    public Boid(Vector2 startPosition, Vector2 startVelocity) {
        this();
        this.setPosition(startPosition);
        this.setVelocity(startVelocity);
    }

    public void update(float dt, List<Vector2> nearbyBoids) {
        Vector2 lastPosition = this.getPosition();
        Vector2 lastVelocity = this.getVelocity();

        for (Vector2 boidPosition : nearbyBoids) {
            Vector2 deltaV;
            float distance = this.boidDst(boidPosition);
            if (distance < MIN_DISTANCE) {
                deltaV = lastPosition.cpy().sub(boidPosition).scl(CORRECTION_RATE);
            } else {
                deltaV = lastPosition.cpy().add(boidPosition).scl(CORRECTION_RATE);
            }
            lastVelocity.add(deltaV);
        }

        float currentSpeed = lastVelocity.len();
        if (currentSpeed > MAX_SPEED) {
            lastVelocity.scl(MAX_SPEED/currentSpeed);
        } else {
            lastVelocity.scl(ACCELERATION);
        }

        lastPosition.add(lastVelocity.cpy().scl(dt));

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        if (lastPosition.x < 0) {
            lastPosition.x += screenWidth;
        }
        if (lastPosition.x > screenWidth) {
            lastPosition.x -= screenWidth;
        }
        if (lastPosition.y < 0) {
            lastPosition.y += screenHeight;
        }
        if (lastPosition.y > screenHeight) {
            lastPosition.y -= screenHeight;
        }
        this.setPosition(lastPosition);
        this.setVelocity(lastVelocity);
    }

    public void render(SpriteBatch sb) {
        Vector2 currentPosition = this.getPosition();
        Vector2 currentVelocity = this.getVelocity();
        boidSprite.setRotation(currentVelocity.angle()-90);
        boidSprite.setPosition(currentPosition.x-boidSprite.getWidth()/2, currentPosition.y-boidSprite.getHeight()/2);
        boidSprite.draw(sb);
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

    public float boidDst(Boid other) {
        return boidDst(other.getPosition());
    }

    public float boidDst(Vector2 otherPos) {
        float xDelta = Math.abs(this.getPosition().x - otherPos.x);
        float yDelta = Math.abs(this.getPosition().y - otherPos.y);
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        if (xDelta > screenWidth/2) {
            xDelta = screenWidth - xDelta;
        }
        if (yDelta > screenHeight/2) {
            yDelta = screenHeight - yDelta;
        }
        return (float)Math.sqrt(xDelta*xDelta + yDelta*yDelta);
    }
}