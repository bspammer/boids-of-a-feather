package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.*;
import java.util.List;

public class Boid {
    public static final float MAX_SPEED = 300f;
//    public static final float MIN_DISTANCE = 75f;
    public static final float VISION_RANGE = 40f;
    public static final float PI = (float) Math.PI;
    public static final int WRAP_PACMAN = 101;
    public static final int WRAP_SPHERE = 102;

    public static int separationWeight = 60;
    public static int cohesionWeight = 20;
    public static int alignmentWeight = 1;
    public static final float WEIGHT_SCALING_FACTOR = 0.005f;

    private Vector2 position;
    private Vector2 velocity;
    protected Sprite boidSprite;
    protected Color spriteColor;

    public Boid() {
        Random rand = new Random();

        boidSprite = new Sprite(TextureController.getInstance().BOID);
        boidSprite.setOrigin(boidSprite.getWidth() / 2, boidSprite.getHeight() / 2);
        float hue = rand.nextFloat();
        float saturation = (rand.nextInt(2000) + 1000) / 2000f;
        float luminance = 0.9f;
        java.awt.Color tempColor = java.awt.Color.getHSBColor(hue, saturation, luminance);
        spriteColor = new Color(((tempColor.getRGB() & 0xffffff) << 8) | 0xff);
        if (BoidSimulator.debugBoidColorsOn) boidSprite.setColor(spriteColor);
        boidSprite.setScale(0.3f);

        float heading = rand.nextFloat() * 2 * PI;
//        float heading = 0;
//        this.setPosition(new Vector2(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2));
        this.setPosition(new Vector2(rand.nextFloat() * Gdx.graphics.getWidth(), rand.nextFloat() * Gdx.graphics.getHeight()));
        this.setVelocity(new Vector2((float) (MAX_SPEED * Math.sin(heading)), (float) (MAX_SPEED * Math.cos(heading))));
    }

    public Boid(Vector2 startPosition) {
        this();
        this.setPosition(startPosition);
    }

    public Boid(Vector2 startPosition, Vector2 startVelocity) {
        this();
        this.setPosition(startPosition);
        this.setVelocity(startVelocity);
    }

    public void update(float deltaTime, List<Vector2> nearbyBoidDisplacements, List<Vector2> nearbyBoidVelocities) {
        // Sanity check
        if (nearbyBoidDisplacements.size() != nearbyBoidVelocities.size()) {
            throw new IllegalStateException("Position list and velocity list not of equal length.");
        }
        Vector2 newPosition = this.getPosition();
        Vector2 newVelocity = this.getVelocity();

        // Calculate separation steer
        Vector2 separation = new Vector2(0, 0);
        for (Vector2 boidDisplacement : nearbyBoidDisplacements) {
            float distanceSquared = boidDisplacement.len2();
            if (distanceSquared != 0) separation.add(boidDisplacement.cpy().scl(30/distanceSquared));
        }
        separation.scl(-1);

        // Calculate cohesion steer
        Vector2 cohesion;
        if (nearbyBoidDisplacements.size() == 0) {
            cohesion = new Vector2(0, 0);
        } else {
            Vector2 centreOfMass = new Vector2(0, 0);
            for (Vector2 boidDisplacement : nearbyBoidDisplacements) {
                centreOfMass.add(boidDisplacement);
            }
            centreOfMass.scl(1f/nearbyBoidDisplacements.size());
            cohesion = centreOfMass;
        }

        // Calculate alignment steer
        Vector2 alignment = new Vector2(0, 0);
        if (nearbyBoidVelocities.size() != 0) {
            for (Vector2 boidVelocity : nearbyBoidVelocities) {
                alignment.add(boidVelocity);
            }
            alignment.scl(1f / nearbyBoidVelocities.size());
        }

        // Add the steering forces to current velocity, then calculate new position
        newVelocity.add(separation.scl(separationWeight * WEIGHT_SCALING_FACTOR));
        newVelocity.add(cohesion.scl(cohesionWeight * WEIGHT_SCALING_FACTOR));
        newVelocity.add(alignment.scl(alignmentWeight * WEIGHT_SCALING_FACTOR));
        if (newVelocity.len() > MAX_SPEED) newVelocity.scl(MAX_SPEED/newVelocity.len());
        newPosition.add(newVelocity.cpy().scl(deltaTime));
        this.setPosition(newPosition);
        this.setVelocity(newVelocity);
    }

    public void render(SpriteBatch sb) {
        Vector2 currentPosition = this.getPosition();
        Vector2 currentVelocity = this.getVelocity();
        boidSprite.setRotation(currentVelocity.angle()-90);
        boidSprite.setPosition(currentPosition.x-boidSprite.getWidth()/2, currentPosition.y-boidSprite.getHeight()/2);
        boidSprite.draw(sb);
    }

    public Vector2 relativeDisplacement(Boid other, int mode) {
        return relativeDisplacement(other.getPosition(), mode);
    }

    public Vector2 relativeDisplacement(Vector2 otherPos, int mode) {
        switch (mode) {
            case WRAP_PACMAN: return pacmanRelativeDisplacement(otherPos);
            case WRAP_SPHERE: return sphereRelativeDisplacement(otherPos);
            default: return pacmanRelativeDisplacement(otherPos);
        }
    }

    public Vector2 relativeVelocity(Boid other, int mode) {
        return relativeVelocity(other.getPosition(), other.getVelocity(), mode);
    }

    public Vector2 relativeVelocity(Vector2 otherPos, Vector2 otherVel, int mode) {
        switch (mode) {
            case WRAP_PACMAN: return otherVel;
            case WRAP_SPHERE: return sphereRelativeVelocity(otherPos, otherVel);
            default: return otherVel;
        }
    }

    private Vector2 sphereRelativeVelocity(Vector2 otherPos, Vector2 otherVel) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 otherRelativeDisplacement = sphereRelativeDisplacement(otherPos);
        Vector2 otherAbsolutePosition = getPosition().add(otherRelativeDisplacement);

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

    public void performWrapping(int mode) {
        switch (mode) {
            case WRAP_PACMAN: performPacmanWrapping(); break;
            case WRAP_SPHERE: performSphereWrapping(); break;
            default: performPacmanWrapping(); break;
        }
    }

    private void performPacmanWrapping() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 newPosition = getPosition();
        if (newPosition.x < 0) {
            newPosition.x += screenWidth * (int) -Math.floor(newPosition.x / screenWidth);
        }
        if (newPosition.x > screenWidth) {
            newPosition.x -= screenWidth * (int) Math.floor(newPosition.x / screenWidth);
        }
        if (newPosition.y < 0) {
            newPosition.y += screenHeight * (int) -Math.floor(newPosition.y / screenHeight);
        }
        if (newPosition.y > screenHeight) {
            newPosition.y -= screenHeight * (int) Math.floor(newPosition.y / screenHeight);
        }
        setPosition(newPosition);
    }

    private Vector2 pacmanRelativeDisplacement(Vector2 otherPos) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 myPos = this.getPosition();
        float xDisplacement, yDisplacement;
        float leftHorDist = Math.abs(myPos.x-(otherPos.x-screenWidth));
        float middleHorDist = Math.abs(myPos.x-otherPos.x);
        float rightHorDist = Math.abs(myPos.x-(otherPos.x+screenWidth));
        float bottomVerDist = Math.abs(myPos.y-(otherPos.y-screenHeight));
        float middleVerDist = Math.abs(myPos.y-otherPos.y);
        float topVerDist = Math.abs(myPos.y-(otherPos.y+screenHeight));
        if (leftHorDist < middleHorDist && leftHorDist < rightHorDist) {
            xDisplacement = myPos.x - (otherPos.x-screenWidth);
        } else if (middleHorDist < leftHorDist && middleHorDist < rightHorDist) {
            xDisplacement = myPos.x - otherPos.x;
        } else {
            xDisplacement = myPos.x - (otherPos.x + screenWidth);
        }
        if (bottomVerDist < middleVerDist && bottomVerDist < topVerDist) {
            yDisplacement = myPos.y - (otherPos.y-screenHeight);
        } else if (middleVerDist < bottomVerDist && middleVerDist < topVerDist) {
            yDisplacement = myPos.y - otherPos.y;
        } else {
            yDisplacement = myPos.y - (otherPos.y + screenHeight);
        }
        return new Vector2(-xDisplacement, -yDisplacement);
    }

    private void performSphereWrapping() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 newPosition = getPosition();
        Vector2 newVelocity = getVelocity();

        // Set up the out of bounds parameters
        boolean oobLeft = newPosition.x < 0;
        boolean oobRight = newPosition.x > screenWidth;
        boolean oobBottom  = newPosition.y < 0;
        boolean oobTop = newPosition.y > screenHeight;
        boolean oobTopLeft = oobTop && oobLeft;
        boolean oobTopRight = oobTop && oobRight;
        boolean oobBottomLeft = oobBottom && oobLeft;
        boolean oobBottomRight = oobBottom && oobRight;

        // handle the corners first
        if (oobTopLeft) {
            // rotate about the top-left corner
            newPosition.sub(0, screenHeight).rotate(180).add(0, screenHeight);
            newVelocity.rotate(180);
        } else if (oobTopRight) {
            // rotate about the top-right corner
            newPosition.sub(screenWidth, screenHeight).rotate(180).add(screenWidth, screenHeight);
            newVelocity.rotate(180);
        } else if (oobBottomLeft) {
            // rotate about the bottom-left corner (the origin)
            newPosition.rotate(180);
            newVelocity.rotate(180);
        } else if (oobBottomRight) {
            // rotate about the bottom-right corner
            newPosition.sub(screenWidth, 0).rotate(180).add(screenWidth, 0);
            newVelocity.rotate(180);
        }
        // handle the edges (should be mutually exclusive to the corners)
        else if (oobLeft) {
            // rotate about the bottom-left corner (the origin)
            newPosition.rotate(-90);
            newVelocity.rotate(-90);
        } else if (oobRight) {
            // rotate about the top-right corner
            newPosition.sub(screenWidth, screenHeight).rotate(-90).add(screenWidth, screenHeight);
            newVelocity.rotate(-90);
        } else if (oobBottom) {
            // rotate about the bottom-left corner (the origin)
            newPosition.rotate(90);
            newVelocity.rotate(90);
        } else if (oobTop) {
            // rotate about the top-right corner
            newPosition.sub(screenWidth, screenHeight).rotate(90).add(screenWidth, screenHeight);
            newVelocity.rotate(90);
        }
        setPosition(newPosition);
        setVelocity(newVelocity);
    }

    private Vector2 sphereRelativeDisplacement(Vector2 otherPos) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        Vector2 screenCentre = new Vector2(screenWidth/2f, screenHeight/2f);
        Vector2 clockwiseRotation = otherPos.cpy().sub(screenCentre).rotate(-90).add(screenCentre);
        Vector2 anticlockwiseRotation = otherPos.cpy().sub(screenCentre).rotate(90).add(screenCentre);
        Vector2 halfRotation = otherPos.cpy().sub(screenCentre).rotate(180).add(screenCentre);

        List<Vector2> possiblePositions = new ArrayList<Vector2>();
        // same universe
        Vector2 samePosition = otherPos.cpy();
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
                Vector2 position1Displacement = getPosition().sub(position1);
                Vector2 position2Displacement = getPosition().sub(position2);
                float distanceDifference =  position1Displacement.len() - position2Displacement.len();
                if (distanceDifference == 0) return 0;
                return distanceDifference < 0 ? -1 : 1;
            }
        });
        return closestPosition.sub(getPosition());
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

    public void setVelocity(Vector2 newVel) {
        this.velocity = newVel;
    }

    public void setVelocityX(float newX) {
        this.velocity.x = newX;
    }

    public void setVelocityY(float newY) {
        this.velocity.y = newY;
    }

    public Vector2 getPosition() {
        return this.position.cpy();
    }

    public Vector2 getVelocity() {
        return this.velocity.cpy();
    }
}