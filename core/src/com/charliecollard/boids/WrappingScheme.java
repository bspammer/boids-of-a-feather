package com.charliecollard.boids;

import com.badlogic.gdx.math.Vector2;
import javafx.util.Pair;

import java.util.List;

public abstract class WrappingScheme {
    public abstract Vector2 relativeDisplacement(Boid from, Boid to);
    public abstract Vector2 relativeDisplacement(Vector2 from, Vector2 to);
    public abstract Vector2 relativeVelocity(Boid from, Boid to);
    public abstract Vector2 relativeVelocity(Vector2 myPos, Vector2 otherPos, Vector2 otherVel);
    public abstract void performWrapping(Boid boidToWrap);
    public abstract Vector2 wrappedVelocity(Vector2 position, Vector2 currentVelocity);
    public abstract Vector2 wrappedPosition(Vector2 positionToWrap);
    public abstract List<Pair<Vector2, Vector2>> getRenderingPositionsAndVelocities(Boid boid);
}
