package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SphereWrappingScheme extends WrappingScheme {
    @Override
    public Vector2 relativeDisplacement(Boid from, Boid to) {
        return relativeDisplacement(from.getPosition(), to.getPosition());
    }

    @Override
    public Vector2 relativeDisplacement(final Vector2 from, Vector2 to) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 screenCentre = new Vector2(screenWidth/2f, screenHeight/2f);
        Vector2 clockwiseRotation = to.cpy().sub(screenCentre).rotate(-90).add(screenCentre);
        Vector2 anticlockwiseRotation = to.cpy().sub(screenCentre).rotate(90).add(screenCentre);
        Vector2 halfRotation = to.cpy().sub(screenCentre).rotate(180).add(screenCentre);

        List<Vector2> possiblePositions = new ArrayList<Vector2>();
        // same universe
        Vector2 samePosition = to.cpy();
        // left universe
        Vector2 leftPosition = anticlockwiseRotation.cpy().add(-screenWidth, 0);
        // right universe
        Vector2 rightPosition = anticlockwiseRotation.cpy().add(screenWidth, 0);
        // bottom universe
        Vector2 bottomPosition = clockwiseRotation.cpy().add(0, -screenHeight);
        // top universe
        Vector2 topPosition = clockwiseRotation.cpy().add(0, screenHeight);
        // top-left universe
        Vector2 topLeftPosition = halfRotation.cpy().add(-screenWidth, screenHeight);
        // top-right universe
        Vector2 topRightPosition = halfRotation.cpy().add(screenWidth, screenHeight);
        // bottom-left universe
        Vector2 bottomLeftPosition = halfRotation.cpy().add(-screenWidth, -screenHeight);
        // bottom-right universe
        Vector2 bottomRightPosition = halfRotation.cpy().add(screenWidth, -screenHeight);

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
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 otherRelativeDisplacement = relativeDisplacement(myPos, otherPos);
        Vector2 otherAbsolutePosition = myPos.cpy().add(otherRelativeDisplacement);

        // corners
        if (    (otherAbsolutePosition.x < 0 && otherAbsolutePosition.y > screenHeight)
                || (otherAbsolutePosition.x > screenWidth && otherAbsolutePosition.y > screenHeight)
                || (otherAbsolutePosition.x < 0 && otherAbsolutePosition.y < 0)
                || (otherAbsolutePosition.x > screenWidth && otherAbsolutePosition.y < 0)) {
            return otherVel.rotate(180);
        }

        // left or right
        if (otherAbsolutePosition.x < 0 || otherAbsolutePosition.x > screenWidth) {
            return otherVel.rotate(90);
        }

        // up or down
        if (otherAbsolutePosition.y < 0 || otherAbsolutePosition.y > screenHeight) {
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
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 newVelocity = currentVelocity.cpy();

        // corners
        if (    (position.x < 0 && position.y > screenHeight)
                || (position.x > screenWidth && position.y > screenHeight)
                || (position.x < 0 && position.y < 0)
                || (position.x > screenWidth && position.y < 0)) {
            return newVelocity.rotate(180);
        }

        // left or right
        if (position.x < 0 || position.x > screenWidth) {
            return newVelocity.rotate(-90);
        }

        // up or down
        if (position.y < 0 || position.y > screenHeight) {
            return newVelocity.rotate(90);
        }
        return newVelocity;
    }

    @Override
    public Vector2 wrappedPosition(Vector2 positionToWrap) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 newPosition = positionToWrap.cpy();

        // Set up the out of bounds parameters
        boolean oobLeft = newPosition.x < 0;
        boolean oobRight = newPosition.x >= screenWidth;
        boolean oobBottom  = newPosition.y < 0;
        boolean oobTop = newPosition.y >= screenHeight;
        boolean oobTopLeft = oobTop && oobLeft;
        boolean oobTopRight = oobTop && oobRight;
        boolean oobBottomLeft = oobBottom && oobLeft;
        boolean oobBottomRight = oobBottom && oobRight;

        // handle the corners first
        if (oobTopLeft) {
            // rotate about the top-left corner
            newPosition.sub(0, screenHeight).rotate(180).add(0, screenHeight);
        } else if (oobTopRight) {
            // rotate about the top-right corner
            newPosition.sub(screenWidth, screenHeight).rotate(180).add(screenWidth, screenHeight);
        } else if (oobBottomLeft) {
            // rotate about the bottom-left corner (the origin)
            newPosition.rotate(180);
        } else if (oobBottomRight) {
            // rotate about the bottom-right corner
            newPosition.sub(screenWidth, 0).rotate(180).add(screenWidth, 0);
        }
        // handle the edges (should be mutually exclusive to the corners)
        else if (oobLeft) {
            // rotate about the bottom-left corner (the origin)
            newPosition.rotate(-90);
        } else if (oobRight) {
            // rotate about the top-right corner
            newPosition.sub(screenWidth, screenHeight).rotate(-90).add(screenWidth, screenHeight);
        } else if (oobBottom) {
            // rotate about the bottom-left corner (the origin)
            newPosition.rotate(90);
        } else if (oobTop) {
            // rotate about the top-right corner
            newPosition.sub(screenWidth, screenHeight).rotate(90).add(screenWidth, screenHeight);
        }
        return newPosition;
    }

    @Override
    public List<Pair<Vector2, Vector2>> getRenderingPositionsAndVelocities(Boid boid) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        List<Pair<Vector2, Vector2>> positionsAndVelocities = new ArrayList<>();
        Vector2 leftPosition = boid.getPosition().sub(screenWidth/2, screenHeight/2).rotate(90).add(-screenWidth/2, screenHeight/2);
        Vector2 topLeftPosition = boid.getPosition().sub(screenWidth/2, screenHeight/2).rotate(180).add(-screenWidth/2, 3*screenHeight/2);
        Vector2 bottomLeftPosition = boid.getPosition().sub(screenWidth/2, screenHeight/2).rotate(180).add(-screenWidth/2, -screenHeight/2);
        Vector2 topPosition = boid.getPosition().sub(screenWidth/2, screenHeight/2).rotate(-90).add(screenWidth/2, 3*screenHeight/2);
        Vector2 bottomPosition = boid.getPosition().sub(screenWidth/2, screenHeight/2).rotate(-90).add(screenWidth/2, -screenHeight/2);
        Vector2 topRightPosition = boid.getPosition().sub(screenWidth/2, screenHeight/2).rotate(180).add(3*screenWidth/2, 3*screenHeight/2);
        Vector2 rightPosition = boid.getPosition().sub(screenWidth/2, screenHeight/2).rotate(90).add(3*screenWidth/2, screenHeight/2);
        Vector2 bottomRightPosition = boid.getPosition().sub(screenWidth/2, screenHeight/2).rotate(180).add(3*screenWidth/2, -screenHeight/2);
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
