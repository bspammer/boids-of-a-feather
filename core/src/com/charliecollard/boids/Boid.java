package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.Random;

public class Boid {
    private static final int SPEED = 200;
    private static final float PI = (float) Math.PI;

    private Vector2 position;
    private Vector2 velocity;
    private float heading; // 0 to 2*pi
    Sprite boidSprite;

    public Boid() {
        Random rand = new Random();

        boidSprite = new Sprite(TextureController.getInstance().BOID);
        boidSprite.setOrigin(boidSprite.getWidth() / 2, boidSprite.getHeight() / 2);

        heading = rand.nextBoolean() ? PI/2-0.3f : 3*PI/2+0.3f;
        position = new Vector2(rand.nextFloat()*Gdx.graphics.getWidth(), rand.nextFloat()*Gdx.graphics.getHeight());
        velocity = new Vector2((float) (SPEED * Math.sin(heading)), (float) (SPEED * Math.cos(heading)));
    }

    public static float headingDifference(float from, float to) {
        if (from == 2*PI) {
            from = 0;
        }
        if (to == 2*PI) {
            to = 0;
        }
        double clockwiseDiff = (to - from);
        double anticlockwiseDiff = (2*PI - to + from);
        return (float) ((Math.abs(clockwiseDiff) <= Math.abs(anticlockwiseDiff)) ? clockwiseDiff : -anticlockwiseDiff);
    }

    public void update(float dt, List<Boid> nearbyBoids) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float spriteWidth = boidSprite.getWidth();
        float spriteHeight = boidSprite.getHeight();

        float sumHeading = 0;
        for (Boid boid : nearbyBoids) {
            sumHeading += boid.getHeading();
            float headingDiff = headingDifference(heading, boid.getHeading());
            heading += headingDiff * 0.05;
            if (heading < 0) {
                heading = 2*PI + heading;
            }
            if (heading > 2*PI) {
                heading = heading % 2*PI;
            }
        }
        float avgHeading = sumHeading/BoidSimulator.BOID_COUNT;
        float proportionDiffFromAvgHeading = Math.abs(headingDifference(avgHeading, heading))/(2*PI);
//        boidSprite.setColor(Math.min(20*proportionDiffFromAvgHeading,1), 1-Math.min(20*proportionDiffFromAvgHeading,1), 0, 1);
//        boidSprite.setScale(Math.max(Math.min(20*proportionDiffFromAvgHeading, 1), 0.1f));

        velocity.x = (float) Math.sin(heading) * SPEED;
        velocity.y = (float) Math.cos(heading) * SPEED;

        position.x += velocity.x * dt;
        position.y += velocity.y * dt;

        if (position.x < 0 - spriteWidth) {
            position.x = screenWidth + spriteWidth + (position.x % screenWidth);
        }
        if (position.x > screenWidth + spriteWidth) {
            position.x = 0 - 2*spriteWidth + (position.x % screenWidth);
        }
        if (position.y < 0 - spriteHeight) {
            position.y = screenHeight + spriteHeight + (position.y % screenHeight);
        }
        if (position.y > screenHeight + spriteHeight) {
            position.y = 0 - 2*spriteHeight + (position.y % screenHeight);
        }
    }

    public void render(SpriteBatch sb) {
        boidSprite.setPosition(position.x, position.y);
        boidSprite.setRotation(-heading*360/(2*PI));
        boidSprite.draw(sb);
    }

    public Vector2 getPosition() {
        return this.position;
    }

    public Vector2 getVelocity() {
        return this.velocity;
    }

    public float getHeading() {
        return this.heading;
    }
}
