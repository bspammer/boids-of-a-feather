package com.charliecollard.boids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class DebugShapeRenderer {
    private static ShapeRenderer shapeRenderer = new ShapeRenderer();
    private static boolean batching = false;

    public static void drawLine(Vector2 start, Vector2 end, int lineWidth, Color color, Matrix4 projectionMatrix) {
        Gdx.gl.glLineWidth(lineWidth);
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.setColor(color);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.line(start, end);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    public static void drawCircle(Vector2 centre, float radius, Color color, int lineWidth, Matrix4 projectionMatrix) {
        Gdx.gl.glLineWidth(lineWidth);
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.setColor(color);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.circle(centre.x, centre.y, radius);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    public static void startBatch(Color color, int lineWidth, Matrix4 projectionMatrix) {
        if (!batching) {
            Gdx.gl.glLineWidth(lineWidth);
            shapeRenderer.setProjectionMatrix(projectionMatrix);
            shapeRenderer.setColor(color);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            batching = true;
        } else {
            throw new IllegalStateException("Attempt to start a batch when already batching");
        }
    }

    // Should only be called between startBatch and endBatch
    public static void batchCircle(Vector2 centre, float radius) {
        if (batching) {
            shapeRenderer.circle(centre.x, centre.y, radius);
        } else {
            throw new IllegalStateException("Attempt to draw a batched circle when not batching");
        }
    }

    public static void batchLine(Vector2 start, Vector2 end) {
        if (batching) {
            shapeRenderer.line(start, end);
        } else {
            throw new IllegalStateException("Attempt to draw a batched line when not batching");
        }
    }

    public static void endBatch() {
        if (batching) {
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);
            batching = false;
        } else {
            throw new IllegalStateException("Attempt to end a batch when not batching");
        }
    }
}
