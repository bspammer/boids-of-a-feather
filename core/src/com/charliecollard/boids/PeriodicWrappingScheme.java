package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.charliecollard.boids.BoidSimulator.simulationHeight;
import static com.charliecollard.boids.BoidSimulator.simulationWidth;

public class PeriodicWrappingScheme extends WrappingScheme {
    @Override
    public Vector2 relativeDisplacement(Boid from, Boid to) {
        return relativeDisplacement(from.getPosition(), to.getPosition());
    }

    @Override
    public Vector2 relativeDisplacement(Vector2 from, Vector2 to) {
        float xDisplacement, yDisplacement;
        float leftHorDist = Math.abs(from.x-(to.x-simulationWidth));
        float middleHorDist = Math.abs(from.x-to.x);
        float rightHorDist = Math.abs(from.x-(to.x+simulationWidth));
        float bottomVerDist = Math.abs(from.y-(to.y-simulationHeight));
        float middleVerDist = Math.abs(from.y-to.y);
        float topVerDist = Math.abs(from.y-(to.y+simulationHeight));
        if (leftHorDist < middleHorDist && leftHorDist < rightHorDist) {
            xDisplacement = from.x - (to.x-simulationWidth);
        } else if (middleHorDist < leftHorDist && middleHorDist < rightHorDist) {
            xDisplacement = from.x - to.x;
        } else {
            xDisplacement = from.x - (to.x + simulationWidth);
        }
        if (bottomVerDist < middleVerDist && bottomVerDist < topVerDist) {
            yDisplacement = from.y - (to.y-simulationHeight);
        } else if (middleVerDist < bottomVerDist && middleVerDist < topVerDist) {
            yDisplacement = from.y - to.y;
        } else {
            yDisplacement = from.y - (to.y + simulationHeight);
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
        if (positionToWrap.x < 0 || positionToWrap.x >= simulationWidth) {
            positionToWrap.x += simulationWidth * (int) -Math.floor(positionToWrap.x / simulationWidth);
        }
        if (positionToWrap.y < 0 || positionToWrap.y >= simulationHeight) {
            positionToWrap.y += simulationHeight * (int) -Math.floor(positionToWrap.y / simulationHeight);
        }
        return positionToWrap;
    }

    @Override
    public List<Pair<Vector2, Vector2>> getRenderingPositionsAndVelocities(Boid boid) {
        List<Pair<Vector2, Vector2>> positionsAndVelocities = new ArrayList<>();
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(-simulationWidth, -simulationHeight), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(0, -simulationHeight), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(simulationWidth, -simulationHeight), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(-simulationWidth, 0), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(simulationWidth, 0), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(-simulationWidth, simulationHeight), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(0, simulationHeight), boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(boid.getPosition().add(simulationWidth, simulationHeight), boid.getVelocity()));
        return positionsAndVelocities;
    }
}
