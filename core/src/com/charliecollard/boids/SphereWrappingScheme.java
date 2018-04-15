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

public class SphereWrappingScheme extends WrappingScheme {
    @Override
    public Vector2 relativeDisplacement(Boid from, Boid to) {
        return relativeDisplacement(from.getPosition(), to.getPosition());
    }

    @Override
    public Vector2 relativeDisplacement(final Vector2 from, Vector2 to) {
        Vector2 screenCentre = new Vector2(simulationWidth/2f, simulationHeight/2f);
        Vector2 clockwiseRotation = to.cpy().sub(screenCentre).rotate(-90).add(screenCentre);
        Vector2 anticlockwiseRotation = to.cpy().sub(screenCentre).rotate(90).add(screenCentre);
        Vector2 halfRotation = to.cpy().sub(screenCentre).rotate(180).add(screenCentre);

        List<Vector2> possiblePositions = new ArrayList<Vector2>();
        // same universe
        Vector2 samePosition = to.cpy();
        // left universe
        Vector2 leftPosition = anticlockwiseRotation.cpy().add(-simulationWidth, 0);
        // right universe
        Vector2 rightPosition = anticlockwiseRotation.cpy().add(simulationWidth, 0);
        // bottom universe
        Vector2 bottomPosition = clockwiseRotation.cpy().add(0, -simulationHeight);
        // top universe
        Vector2 topPosition = clockwiseRotation.cpy().add(0, simulationHeight);
        // top-left universe
        Vector2 topLeftPosition = halfRotation.cpy().add(-simulationWidth, simulationHeight);
        // top-right universe
        Vector2 topRightPosition = halfRotation.cpy().add(simulationWidth, simulationHeight);
        // bottom-left universe
        Vector2 bottomLeftPosition = halfRotation.cpy().add(-simulationWidth, -simulationHeight);
        // bottom-right universe
        Vector2 bottomRightPosition = halfRotation.cpy().add(simulationWidth, -simulationHeight);

        possiblePositions.add(samePosition);
        possiblePositions.add(leftPosition);
        possiblePositions.add(rightPosition);
        possiblePositions.add(bottomPosition);
        possiblePositions.add(topPosition);
        possiblePositions.add(topLeftPosition);
        possiblePositions.add(topRightPosition);
        possiblePositions.add(bottomLeftPosition);
        possiblePositions.add(bottomRightPosition);

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

        // corners
        if (    (otherAbsolutePosition.x < 0 && otherAbsolutePosition.y > simulationHeight)
                || (otherAbsolutePosition.x > simulationWidth && otherAbsolutePosition.y > simulationHeight)
                || (otherAbsolutePosition.x < 0 && otherAbsolutePosition.y < 0)
                || (otherAbsolutePosition.x > simulationWidth && otherAbsolutePosition.y < 0)) {
            return otherVel.rotate(180);
        }

        // left or right
        if (otherAbsolutePosition.x < 0 || otherAbsolutePosition.x > simulationWidth) {
            return otherVel.rotate(90);
        }

        // up or down
        if (otherAbsolutePosition.y < 0 || otherAbsolutePosition.y > simulationHeight) {
            return otherVel.rotate(-90);
        }

        return otherVel;
    }

    @Override
    public void performWrapping(Boid boidToWrap) {
        boidToWrap.setVelocity(wrappedVelocity(boidToWrap.getPosition(), boidToWrap.getVelocity()));
        boidToWrap.setPosition(wrappedPosition(boidToWrap.getPosition()));
    }

    @Override
    public Vector2 wrappedVelocity(Vector2 position, Vector2 currentVelocity) {
        Vector2 newVelocity = currentVelocity.cpy();

        // corners
        if (    (position.x < 0 && position.y > simulationHeight)
                || (position.x > simulationWidth && position.y > simulationHeight)
                || (position.x < 0 && position.y < 0)
                || (position.x > simulationWidth && position.y < 0)) {
            return newVelocity.rotate(180);
        }

        // left or right
        if (position.x < 0 || position.x > simulationWidth) {
            return newVelocity.rotate(-90);
        }

        // up or down
        if (position.y < 0 || position.y > simulationHeight) {
            return newVelocity.rotate(90);
        }
        return newVelocity;
    }

    @Override
    public Vector2 wrappedPosition(Vector2 positionToWrap) {
        Vector2 newPosition = positionToWrap.cpy();

        // Set up the out of bounds parameters
        boolean oobLeft = newPosition.x < 0;
        boolean oobRight = newPosition.x >= simulationWidth;
        boolean oobBottom  = newPosition.y < 0;
        boolean oobTop = newPosition.y >= simulationHeight;
        boolean oobTopLeft = oobTop && oobLeft;
        boolean oobTopRight = oobTop && oobRight;
        boolean oobBottomLeft = oobBottom && oobLeft;
        boolean oobBottomRight = oobBottom && oobRight;

        // handle the corners first
        if (oobTopLeft) {
            // rotate about the top-left corner
            newPosition.sub(0, simulationHeight).rotate(180).add(0, simulationHeight);
        } else if (oobTopRight) {
            // rotate about the top-right corner
            newPosition.sub(simulationWidth, simulationHeight).rotate(180).add(simulationWidth, simulationHeight);
        } else if (oobBottomLeft) {
            // rotate about the bottom-left corner (the origin)
            newPosition.rotate(180);
        } else if (oobBottomRight) {
            // rotate about the bottom-right corner
            newPosition.sub(simulationWidth, 0).rotate(180).add(simulationWidth, 0);
        }
        // handle the edges (should be mutually exclusive to the corners)
        else if (oobLeft) {
            // rotate about the bottom-left corner (the origin)
            newPosition.rotate(-90);
        } else if (oobRight) {
            // rotate about the top-right corner
            newPosition.sub(simulationWidth, simulationHeight).rotate(-90).add(simulationWidth, simulationHeight);
        } else if (oobBottom) {
            // rotate about the bottom-left corner (the origin)
            newPosition.rotate(90);
        } else if (oobTop) {
            // rotate about the top-right corner
            newPosition.sub(simulationWidth, simulationHeight).rotate(90).add(simulationWidth, simulationHeight);
        }
        return newPosition;
    }

    @Override
    public List<Pair<Vector2, Vector2>> getRenderingPositionsAndVelocities(Boid boid) {
        List<Pair<Vector2, Vector2>> positionsAndVelocities = new ArrayList<>();
        Vector2 leftPosition = boid.getPosition().sub(simulationWidth/2, simulationHeight/2).rotate(90).add(-simulationWidth/2, simulationHeight/2);
        Vector2 topLeftPosition = boid.getPosition().sub(simulationWidth/2, simulationHeight/2).rotate(180).add(-simulationWidth/2, 3*simulationHeight/2);
        Vector2 bottomLeftPosition = boid.getPosition().sub(simulationWidth/2, simulationHeight/2).rotate(180).add(-simulationWidth/2, -simulationHeight/2);
        Vector2 topPosition = boid.getPosition().sub(simulationWidth/2, simulationHeight/2).rotate(-90).add(simulationWidth/2, 3*simulationHeight/2);
        Vector2 bottomPosition = boid.getPosition().sub(simulationWidth/2, simulationHeight/2).rotate(-90).add(simulationWidth/2, -simulationHeight/2);
        Vector2 topRightPosition = boid.getPosition().sub(simulationWidth/2, simulationHeight/2).rotate(180).add(3*simulationWidth/2, 3*simulationHeight/2);
        Vector2 rightPosition = boid.getPosition().sub(simulationWidth/2, simulationHeight/2).rotate(90).add(3*simulationWidth/2, simulationHeight/2);
        Vector2 bottomRightPosition = boid.getPosition().sub(simulationWidth/2, simulationHeight/2).rotate(180).add(3*simulationWidth/2, -simulationHeight/2);
        positionsAndVelocities.add(new Pair<>(leftPosition, boid.getVelocity().rotate(90)));
        positionsAndVelocities.add(new Pair<>(topLeftPosition, boid.getVelocity().rotate(180)));
        positionsAndVelocities.add(new Pair<>(bottomLeftPosition, boid.getVelocity().rotate(180)));
        positionsAndVelocities.add(new Pair<>(topPosition, boid.getVelocity().rotate(-90)));
        positionsAndVelocities.add(new Pair<>(bottomPosition, boid.getVelocity().rotate(-90)));
        positionsAndVelocities.add(new Pair<>(topRightPosition, boid.getVelocity().rotate(180)));
        positionsAndVelocities.add(new Pair<>(rightPosition, boid.getVelocity().rotate(90)));
        positionsAndVelocities.add(new Pair<>(bottomRightPosition, boid.getVelocity().rotate(180)));
        return positionsAndVelocities;
    }
}
