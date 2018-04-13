package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class SolidWrappingScheme extends WrappingScheme {
    @Override
    public Vector2 relativeDisplacement(Boid from, Boid to) {
        return relativeDisplacement(from.getPosition(), to.getPosition());
    }

    @Override
    public Vector2 relativeDisplacement(Vector2 from, Vector2 to) {
        return to.cpy().sub(from);
    }

    @Override
    public Vector2 relativeVelocity(Boid from, Boid to) {
        return relativeVelocity(from.getPosition(), to.getPosition(), to.getVelocity());
    }

    @Override
    public Vector2 relativeVelocity(Vector2 myPos, Vector2 otherPos, Vector2 otherVel) {
        return otherVel.cpy();
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
        if (position.x < 0) {
            newVelocity.scl(-1, 1);
        }
        if (position.x > screenWidth) {
            newVelocity.scl(-1, 1);
        }
        if (position.y < 0) {
            newVelocity.scl(1, -1);
        }
        if (position.y > screenHeight) {
            newVelocity.scl(1, -1);
        }
        return newVelocity;
    }

    @Override
    public Vector2 wrappedPosition(Vector2 positionToWrap) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 newPosition = positionToWrap.cpy();
        if (newPosition.x < 0) {
            newPosition.x = -newPosition.x;
        }
        if (newPosition.x > screenWidth) {
            newPosition.x = screenWidth - (newPosition.x - screenWidth);
        }
        if (newPosition.y < 0) {
            newPosition.y = -newPosition.y;
        }
        if (newPosition.y > screenHeight) {
            newPosition.y = screenHeight - (newPosition.y - screenHeight);
        }
        return newPosition;
    }

    @Override
    public List<Pair<Vector2, Vector2>> getRenderingPositionsAndVelocities(Boid boid) {
        return new ArrayList<>();
    }
}
