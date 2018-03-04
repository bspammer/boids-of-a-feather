package com.charliecollard.boids;

import com.badlogic.gdx.graphics.Texture;

/**
 * Created by Charlie on 29/10/2017.
 */
public class TextureController {
    private static TextureController instance = new TextureController();
    public Texture BOID;

    public static TextureController getInstance() {
        return instance;
    }

    public void disposeAllTextures() {
        BOID.dispose();
    }

    private TextureController() {
        BOID = new Texture("core/assets/boid.png");
    }
}
