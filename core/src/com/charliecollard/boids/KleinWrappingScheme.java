package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.charliecollard.boids.BoidSimulator.simulationHeight;
import static com.charliecollard.boids.BoidSimulator.simulationWidth;

public class KleinWrappingScheme extends WrappingScheme {
    @Override
    public Vector2 relativeDisplacement(Boid from, Boid to) {
        return relativeDisplacement(from.getPosition(), to.getPosition());
    }

    @Override
    public Vector2 relativeDisplacement(final Vector2 from, Vector2 to) {
        Vector2 flippedHorizontally = to.cpy().sub(simulationWidth/2, 0).scl(-1, 1).add(simulationWidth/2, 0);
        List<Vector2> possiblePositions = new ArrayList<>();

        // same universe
        possiblePositions.add(to.cpy());
        // left universe
        possiblePositions.add(to.cpy().sub(simulationWidth, 0));
        // right universe
        possiblePositions.add(to.cpy().add(simulationWidth, 0));
        // bottom universe
        possiblePositions.add(flippedHorizontally.cpy().add(0, -simulationHeight));
        // top universe
        possiblePositions.add(flippedHorizontally.cpy().add(0, simulationHeight));
        // top-left universe
        possiblePositions.add(flippedHorizontally.cpy().add(-simulationWidth, simulationHeight));
        // top-right universe
        possiblePositions.add(flippedHorizontally.cpy().add(simulationWidth, simulationHeight));
        // bottom-left universe
        possiblePositions.add(flippedHorizontally.cpy().add(-simulationWidth, -simulationHeight));
        // bottom-right universe
        possiblePositions.add(flippedHorizontally.cpy().add(simulationWidth, -simulationHeight));

        Vector2 closestPosition = Collections.min(possiblePositions, new Comparator<Vector2>() {
            @Override
            public int compare(Vector2 position1, Vector2 position2) {
                Vector2 position1Displacement = from.cpy().sub(position1);
                Vector2 position2Displacement = from.cpy().sub(position2);
                float distanceDifference =  position1Displacement.len() - position2Displacement.len();
                if (distanceDifference == 0) return 0;
                return distanceDifference < 0 ? -1 : 1;
            }
        });
        return closestPosition.sub(from);
    }

    @Override
    public Vector2 relativeVelocity(Boid from, Boid to) {
        return relativeVelocity(from.getPosition(), to.getPosition(), to.getVelocity());
    }

    @Override
    public Vector2 relativeVelocity(Vector2 myPos, Vector2 otherPos, Vector2 otherVel) {
        Vector2 otherRelativeDisplacement = relativeDisplacement(myPos, otherPos);
        Vector2 otherAbsolutePosition = myPos.cpy().add(otherRelativeDisplacement);

        if (Math.floor(otherAbsolutePosition.y / simulationHeight) == Math.floor(myPos.y / simulationHeight)) {
            return otherVel;
        } else {
            return otherVel.scl(-1, 1);
        }
    }

    @Override
    public void performWrapping(Boid boidToWrap) {
        boidToWrap.setVelocity(wrappedVelocity(boidToWrap.getPosition(), boidToWrap.getVelocity()));
        boidToWrap.setPosition(wrappedPosition(boidToWrap.getPosition()));
    }

    @Override
    public Vector2 wrappedVelocity(Vector2 position, Vector2 currentVelocity) {
        Vector2 newVelocity = currentVelocity.cpy();

        boolean oobBottom = position.y < 0;
        boolean oobTop = position.y >= simulationHeight;
        if (oobBottom || oobTop) {
            return newVelocity.scl(-1, 1);
        }
        return newVelocity;
    }

    @Override
    public Vector2 wrappedPosition(Vector2 positionToWrap) {
        Vector2 newPosition = positionToWrap.cpy();

        // Set up the horizontal out of bounds parameters
        boolean oobLeft = newPosition.x < 0;
        boolean oobRight = newPosition.x >= 0;
        if (oobLeft || oobRight) {
            newPosition.x += simulationWidth * (int) -Math.floor(newPosition.x / simulationWidth);
        }

        // Set up the vertical out of bounds parameters
        boolean oobBottom  = newPosition.y < 0;
        boolean oobTop = newPosition.y >= simulationHeight;
        if (oobBottom || oobTop) {
            newPosition.y += simulationHeight * (int) -Math.floor(newPosition.y / simulationHeight);
            newPosition.sub(simulationWidth - 2*(simulationWidth - newPosition.x), 0);
        }
        return newPosition;
    }

    @Override
    public List<Pair<Vector2, Vector2>> getRenderingPositionsAndVelocities(Boid boid) {
        List<Pair<Vector2, Vector2>> positionsAndVelocities = new ArrayList<>();
        Vector2 leftPosition = boid.getPosition().sub(simulationWidth, 0);
        Vector2 topLeftPosition = boid.getPosition().scl(-1, 1).add(0, simulationHeight);
        Vector2 bottomLeftPosition = boid.getPosition().scl(-1, 1).add(0, -simulationHeight);
        Vector2 topPosition = boid.getPosition().scl(-1, 1).add(simulationWidth, simulationHeight);
        Vector2 bottomPosition = boid.getPosition().scl(-1, 1).add(simulationWidth, -simulationHeight);
        Vector2 topRightPosition = boid.getPosition().scl(-1, 1).add(2*simulationWidth, simulationHeight);
        Vector2 rightPosition = boid.getPosition().add(simulationWidth, 0);
        Vector2 bottomRightPosition = boid.getPosition().scl(-1, 1).add(2*simulationWidth, -simulationHeight);
        positionsAndVelocities.add(new Pair<>(leftPosition, boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(topLeftPosition, boid.getVelocity().scl(-1, 1)));
        positionsAndVelocities.add(new Pair<>(bottomLeftPosition, boid.getVelocity().scl(-1, 1)));
        positionsAndVelocities.add(new Pair<>(topPosition, boid.getVelocity().scl(-1, 1)));
        positionsAndVelocities.add(new Pair<>(bottomPosition, boid.getVelocity().scl(-1, 1)));
        positionsAndVelocities.add(new Pair<>(topRightPosition, boid.getVelocity().scl(-1, 1)));
        positionsAndVelocities.add(new Pair<>(rightPosition, boid.getVelocity()));
        positionsAndVelocities.add(new Pair<>(bottomRightPosition, boid.getVelocity().scl(-1, 1)));
        return positionsAndVelocities;
    }
}
