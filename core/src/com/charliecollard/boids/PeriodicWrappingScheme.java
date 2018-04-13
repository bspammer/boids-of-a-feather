package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class PeriodicWrappingScheme extends WrappingScheme {
    @Override
    public Vector2 relativeDisplacement(Boid from, Boid to) {
        return relativeDisplacement(from.getPosition(), to.getPosition());
    }

    @Override
    public Vector2 relativeDisplacement(Vector2 from, Vector2 to) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        float xDisplacement, yDisplacement;
        float leftHorDist = Math.abs(from.x-(to.x-screenWidth));
        float middleHorDist = Math.abs(from.x-to.x);
        float rightHorDist = Math.abs(from.x-(to.x+screenWidth));
        float bottomVerDist = Math.abs(from.y-(to.y-screenHeight));
        float middleVerDist = Math.abs(from.y-to.y);
        float topVerDist = Math.abs(from.y-(to.y+screenHeight));
        if (leftHorDist < middleHorDist && leftHorDist < rightHorDist) {
            xDisplacement = from.x - (to.x-screenWidth);
        } else if (middleHorDist < leftHorDist && middleHorDist < rightHorDist) {
            xDisplacement = from.x - to.x;
        } else {
            xDisplacement = from.x - (to.x + screenWidth);
        }
        if (bottomVerDist < middleVerDist && bottomVerDist < topVerDist) {
            yDisplacement = from.y - (to.y-screenHeight);
        } else if (middleVerDist < bottomVerDist && middleVerDist < topVerDist) {
            yDisplacement = from.y - to.y;
        } else {
            yDisplacement = from.y - (to.y + screenHeight);
        }
        return new Vector2(-xDisplacement, -yDisplacement);
    }

    @Override
    public Vector2 relativeVelocity(Boid from, Boid to) {
        return to.getVelocity();
    }

    @Override
    public Vector2 relativeVelocity(Vector2 myPos, Vector2 otherPos, Vector2 otherVel) {
        return otherVel;
    }

    @Override
    public void performWrapping(Boid boidToWrap) {
        boidToWrap.setPosition(wrappedPosition(boidToWrap.getPosition()));
    }

    @Override
    public Vector2 wrappedVelocity(Vector2 position, Vector2 currentVelocity) {
        return currentVelocity;
    }

    @Override
    public Vector2 wrappedPosition(Vector2 positionToWrap) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        if (positionToWrap.x < 0 || positionToWrap.x >= screenWidth) {
            positionToWrap.x += screenWidth * (int) -Math.floor(positionToWrap.x / screenWidth);
        }
        if (positionToWrap.y < 0 || positionToWrap.y >= screenHeight) {
            positionToWrap.y += screenHeight * (int) -Math.floor(positionToWrap.y / screenHeight);
        }
        return positionToWrap;
    }

    @Override
    public List<Pair<Vector2, Vector2>> getRenderingPositionsAndVelocities(Boid boid) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        List<Pair<Vector2, Vector2>> positionsAndVelocities = new ArrayList<>();
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(-screenWidth, -screenHeight), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(0, -screenHeight), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(screenWidth, -screenHeight), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(-screenWidth, 0), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(screenWidth, 0), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(-screenWidth, screenHeight), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(0, screenHeight), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(screenWidth, screenHeight), boid.getVelocity()));
        return positionsAndVelocities;
    }
}
