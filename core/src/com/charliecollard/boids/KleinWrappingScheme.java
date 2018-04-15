package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KleinWrappingScheme extends WrappingScheme {
    @Override
    public Vector2 relativeDisplacement(Boid from, Boid to) {
        return relativeDisplacement(from.getPosition(), to.getPosition());
    }

    @Override
    public Vector2 relativeDisplacement(final Vector2 from, Vector2 to) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 flippedHorizontally = to.cpy().sub(screenWidth/2, 0).scl(-1, 1).add(screenWidth/2, 0);
        List<Vector2> possiblePositions = new ArrayList<>();

        // same universe
        possiblePositions.add(to.cpy());
        // left universe
        possiblePositions.add(to.cpy().sub(screenWidth, 0));
        // right universe
        possiblePositions.add(to.cpy().add(screenWidth, 0));
        // bottom universe
        possiblePositions.add(flippedHorizontally.cpy().add(0, -screenHeight));
        // top universe
        possiblePositions.add(flippedHorizontally.cpy().add(0, screenHeight));
        // top-left universe
        possiblePositions.add(flippedHorizontally.cpy().add(-screenWidth, screenHeight));
        // top-right universe
        possiblePositions.add(flippedHorizontally.cpy().add(screenWidth, screenHeight));
        // bottom-left universe
        possiblePositions.add(flippedHorizontally.cpy().add(-screenWidth, -screenHeight));
        // bottom-right universe
        possiblePositions.add(flippedHorizontally.cpy().add(screenWidth, -screenHeight));

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
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 otherRelativeDisplacement = relativeDisplacement(myPos, otherPos);
        Vector2 otherAbsolutePosition = myPos.cpy().add(otherRelativeDisplacement);

        if (Math.floor(otherAbsolutePosition.y / screenHeight) == Math.floor(myPos.y / screenHeight)) {
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
        boolean oobTop = position.y >= Gdx.graphics.getHeight();
        if (oobBottom || oobTop) {
            return newVelocity.scl(-1, 1);
        }
        return newVelocity;
    }

    @Override
    public Vector2 wrappedPosition(Vector2 positionToWrap) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 newPosition = positionToWrap.cpy();

        // Set up the horizontal out of bounds parameters
        boolean oobLeft = newPosition.x < 0;
        boolean oobRight = newPosition.x >= 0;
        if (oobLeft || oobRight) {
            newPosition.x += screenWidth * (int) -Math.floor(newPosition.x / screenWidth);
        }

        // Set up the vertical out of bounds parameters
        boolean oobBottom  = newPosition.y < 0;
        boolean oobTop = newPosition.y >= screenHeight;
        if (oobBottom || oobTop) {
            newPosition.y += screenHeight * (int) -Math.floor(newPosition.y / screenHeight);
            newPosition.sub(screenWidth - 2*(screenWidth - newPosition.x), 0);
        }
        return newPosition;
    }

    @Override
    public List<Pair<Vector2, Vector2>> getRenderingPositionsAndVelocities(Boid boid) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        List<Pair<Vector2, Vector2>> positionsAndVelocities = new ArrayList<>();
        Vector2 leftPosition = boid.getPosition().sub(screenWidth, 0);
        Vector2 topLeftPosition = boid.getPosition().scl(-1, 1).add(0, screenHeight);
        Vector2 bottomLeftPosition = boid.getPosition().scl(-1, 1).add(0, -screenHeight);
        Vector2 topPosition = boid.getPosition().scl(-1, 1).add(screenWidth, screenHeight);
        Vector2 bottomPosition = boid.getPosition().scl(-1, 1).add(screenWidth, -screenHeight);
        Vector2 topRightPosition = boid.getPosition().scl(-1, 1).add(2*screenWidth, screenHeight);
        Vector2 rightPosition = boid.getPosition().add(screenWidth, 0);
        Vector2 bottomRightPosition = boid.getPosition().scl(-1, 1).add(2*screenWidth, -screenHeight);
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
