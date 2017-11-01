package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.Random;

public class Boid {
    private static final float MAX_SPEED = 200f;
    private static final float MIN_SEPERATION = 80f;
    private static final float CORRECTION_RATE = 0.5f;
    private static final float ACCELERATION = 1.01f;
    private static final float PI = (float) Math.PI;

    private Vector2 position;
    private Vector2 velocity;
    Sprite boidSprite;

    public Boid() {
        Random rand = new Random();

        boidSprite = new Sprite(TextureController.getInstance().BOID);
        boidSprite.setOrigin(boidSprite.getWidth() / 2, boidSprite.getHeight() / 2);

        float heading = rand.nextFloat() * 4 * PI;
        this.setPosition(new Vector2(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2));//this.setPosition(new Vector2(rand.nextFloat() * Gdx.graphics.getWidth(), rand.nextFloat() * Gdx.graphics.getHeight()));
        this.setVelocity(new Vector2((float) (MAX_SPEED * Math.sin(heading)), (float) (MAX_SPEED * Math.cos(heading))));
    }

    public void update(float dt, List<Boid> nearbyBoids) {
        Vector2 currentPosition = this.getPosition();
        Vector2 currentVelocity = this.getVelocity();

        for (Boid boid : nearbyBoids) {
            Vector2 boidPosition = boid.getPosition();
            Vector2 deltaV;
            float seperation = currentPosition.dst(boidPosition);
            if (seperation < MIN_SEPERATION) {
                deltaV = currentPosition.cpy().sub(boidPosition).scl(CORRECTION_RATE);
            } else {
                deltaV = currentPosition.cpy().add(boidPosition).scl(CORRECTION_RATE);
            }
            currentVelocity.add(deltaV);
        }

        float currentSpeed = currentVelocity.len();
        if (currentSpeed > MAX_SPEED) {
            currentVelocity.scl(MAX_SPEED/currentSpeed);
        } else {
            currentVelocity.scl(ACCELERATION);
        }

        currentPosition.add(currentVelocity.cpy().scl(dt));

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float spriteWidth = boidSprite.getWidth();
        float spriteHeight = boidSprite.getHeight();
        if (currentPosition.x < 0 - spriteWidth) {
            currentPosition.x = screenWidth + spriteWidth + (currentPosition.x % screenWidth);
        }
        if (currentPosition.x > screenWidth + spriteWidth) {
            currentPosition.x = 0 - 2 * spriteWidth + (currentPosition.x % screenWidth);
        }
        if (currentPosition.y < 0 - spriteHeight) {
            currentPosition.y = screenHeight + spriteHeight + (currentPosition.y % screenHeight);
        }
        if (currentPosition.y > screenHeight + spriteHeight) {
            currentPosition.y = 0 - 2 * spriteHeight + (currentPosition.y % screenHeight);
        }
        this.setPosition(currentPosition);
        this.setVelocity(currentVelocity);
    }

    public void render(SpriteBatch sb) {
        Vector2 currentPosition = this.getPosition();
        Vector2 currentVelocity = this.getVelocity();
        boidSprite.setPosition(currentPosition.x, currentPosition.y);
        boidSprite.setRotation(currentVelocity.angle()-90);
        boidSprite.setScale(0.5f);
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

    public void setVelocity(Vector2 newPos) {
        this.velocity = newPos;
    }

    public void setVelocityX(float newX) {
        this.velocity.x = newX;
    }

    public void setVelocityY(float newY) {
        this.velocity.y = newY;
    }

    public Vector2 getPosition() {
        return new Vector2(this.position);
    }

    public Vector2 getVelocity() {
        return new Vector2(this.velocity);
    }
}