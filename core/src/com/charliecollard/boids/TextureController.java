package com.charliecollard.boids;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Charlie on 29/10/2017.
 */
public class TextureController {
    private static TextureController theInstance;
    public static final String BOID = "boid";
    public static final String PARTICLE = "particle";

    private static Texture boidTexture;
    private static Texture particleTexture;
    private static Map<String, Texture> textureMap = new HashMap<>();

    public static TextureController getInstance() {
        if (theInstance == null) {
            theInstance = new TextureController();
        }
        return theInstance;
    }

    public void disposeAllTextures() {
        boidTexture.dispose();
        particleTexture.dispose();
        textureMap.clear();
    }

    private TextureController() {
        boidTexture = new Texture("boid.png");
        particleTexture = new Texture("particle.png");
        textureMap.put(BOID, boidTexture);
        textureMap.put(PARTICLE, particleTexture);
    }

    public Texture getTexture(String textureName) {
        return textureMap.get(textureName);
    }
}
