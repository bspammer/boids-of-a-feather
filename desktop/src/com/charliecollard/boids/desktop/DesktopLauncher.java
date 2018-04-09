package com.charliecollard.boids.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.charliecollard.boids.Boid;
import com.charliecollard.boids.BoidSimulator;
import com.charliecollard.boids.TextureController;
import org.apache.commons.cli.*;

public class DesktopLauncher {
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 880;
        //		config.width = 880;
        config.height = 880;

        Option width = Option.builder("w")
                .longOpt("width")
                .desc("Width of the simulation")
                .hasArg()
                .required(false)
                .build();
        Option height = Option.builder("h")
                .longOpt("height")
                .desc("Height of the simulation")
                .hasArg()
                .required(false)
                .build();
        Option initialBoidCount = Option.builder("n")
                .longOpt("number")
                .desc("Initial boid count")
                .hasArg()
                .required(false)
                .build();
        Option initialSeparation = Option.builder("s")
                .longOpt("separation")
                .desc("Initial separation weighting")
                .hasArg()
                .required(false)
                .build();
        Option initialCohesion = Option.builder("c")
                .longOpt("cohesion")
                .desc("Initial cohesion weighting")
                .hasArg()
                .required(false)
                .build();
        Option initialAlignment = Option.builder("a")
                .longOpt("alignment")
                .desc("Initial alignment weighting")
                .hasArg()
                .required(false)
                .build();
        Option boidVisionRange = Option.builder("r")
                .longOpt("boid-vision-range")
                .desc("The range at which each boid considers its neighbours")
                .hasArg()
                .required(false)
                .build();
        Option boundaryCondition = Option.builder("b")
                .longOpt("boundary-condition")
                .desc("The boundary condition to use for the simulation, either 'periodic' (default) or 'sphere'")
                .hasArg()
                .required(false)
                .build();
        Option updateMode = Option.builder("u")
                .longOpt("update-mode")
                .desc("Update mode to use, either 'deterministic' (default) or 'timed'")
                .hasArg()
                .required(false)
                .build();
        Option boidSprite = Option.builder()
                .longOpt("boid-sprite")
                .desc("Boid sprite, either 'classic' (default) or 'particle'")
                .hasArg()
                .required(false)
                .build();

        Options options = new Options();
        options.addOption(width);
        options.addOption(height);
        options.addOption(initialBoidCount);
        options.addOption(initialSeparation);
        options.addOption(initialCohesion);
        options.addOption(initialAlignment);
        options.addOption(boidVisionRange);
        options.addOption(boundaryCondition);
        options.addOption(updateMode);
        options.addOption(boidSprite);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("width")) config.width = Integer.valueOf(cmd.getOptionValue("width"));
            if (cmd.hasOption("height")) config.height = Integer.valueOf(cmd.getOptionValue("height"));
            if (cmd.hasOption("number")) BoidSimulator.boidCount = Integer.valueOf(cmd.getOptionValue("number"));
            if (cmd.hasOption("separation")) Boid.separationWeight = Integer.valueOf(cmd.getOptionValue("separation"));
            if (cmd.hasOption("cohesion")) Boid.cohesionWeight = Integer.valueOf(cmd.getOptionValue("cohesion"));
            if (cmd.hasOption("alignment")) Boid.alignmentWeight = Integer.valueOf(cmd.getOptionValue("alignment"));
            if (cmd.hasOption("boid-vision-range")) Boid.visionRange = Integer.valueOf(cmd.getOptionValue("boid-vision-range"));
            if (cmd.hasOption("boundary-condition")) {
                String boundCond = cmd.getOptionValue("boundary-condition");
                if (boundCond.equals("sphere")) BoidSimulator.wrapMode = Boid.WRAP_SPHERE;
            }
            if (cmd.hasOption("update-mode")) {
                String boundCond = cmd.getOptionValue("update-mode");
                if (boundCond.equals("timed")) BoidSimulator.updateMode = Boid.UPDATE_TIMED;
            }
            if (cmd.hasOption("boid-sprite")) {
                String sprite = cmd.getOptionValue("boid-sprite");
                if (sprite.equals("particle")) Boid.boidTexture = TextureController.PARTICLE;
            }
        } catch (ParseException | NumberFormatException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
		    helpFormatter.printHelp("desktop-1.0", "Create a boid simulation", options, "", true);
		    return;
        }

        config.x = 1920/2 - config.width/2;
        config.y = 1040/2 - config.height/2;
        new LwjglApplication(new BoidSimulator(), config);
	}
}
