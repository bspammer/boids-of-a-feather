package com.charliecollard.boids.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.charliecollard.boids.*;
import org.apache.commons.cli.*;

public class DesktopLauncher {
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 880;
        config.height = 880;
        config.resizable = false;

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
        Option boidSusceptibility = Option.builder()
                .longOpt("boid-susceptibility")
                .desc("How strongly the boids will react to their peers")
                .hasArg()
                .required(false)
                .build();
        Option loadFile = Option.builder("l")
                .longOpt("load-file")
                .desc("Path to a file to restore simulation state to a previous point")
                .hasArg()
                .required(false)
                .build();
        Option zoomOut = Option.builder("z")
                .longOpt("zoom-out")
                .desc("Zoom out to show the surrounding virtual universes")
                .required(false)
                .build();
        Option fullscreen = Option.builder("f")
                .longOpt("fullscreen")
                .desc("Fullscreen the application (it is recommended to set the width and height parameters to your screen size along with this option)")
                .required(false)
                .build();
        Option headless = Option.builder()
                .longOpt("headless")
                .desc("Run without rendering the simulation")
                .required(false)
                .build();
        Option showCorrelations = Option.builder()
                .longOpt("show-correlations")
                .desc("Show the correlation plot")
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
        options.addOption(boidSusceptibility);
        options.addOption(loadFile);
        options.addOption(zoomOut);
        options.addOption(fullscreen);
        options.addOption(headless);
        options.addOption(showCorrelations);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("width")) {
                int passedWidth = Integer.valueOf(cmd.getOptionValue("width"));
                config.width = passedWidth;
                BoidSimulator.simulationWidth = passedWidth;
            }
            if (cmd.hasOption("height")) {
                int passedHeight = Integer.valueOf(cmd.getOptionValue("height"));
                config.height = passedHeight;
                BoidSimulator.simulationHeight = passedHeight;
            }
            if (cmd.hasOption("number")) BoidSimulator.boidCount = Integer.valueOf(cmd.getOptionValue("number"));
            if (cmd.hasOption("separation")) Boid.separationWeight = Integer.valueOf(cmd.getOptionValue("separation"));
            if (cmd.hasOption("cohesion")) Boid.cohesionWeight = Integer.valueOf(cmd.getOptionValue("cohesion"));
            if (cmd.hasOption("alignment")) Boid.alignmentWeight = Integer.valueOf(cmd.getOptionValue("alignment"));
            if (cmd.hasOption("boid-vision-range")) Boid.visionRange = Integer.valueOf(cmd.getOptionValue("boid-vision-range"));
            if (cmd.hasOption("boid-susceptibility")) Boid.boidSusceptibility = Integer.valueOf(cmd.getOptionValue("boid-susceptibility"));
            if (cmd.hasOption("boundary-condition")) {
                String boundCond = cmd.getOptionValue("boundary-condition");
                if (boundCond.equals("sphere")) BoidSimulator.wrappingScheme = new SphereWrappingScheme();
                if (boundCond.equals("solid")) BoidSimulator.wrappingScheme = new SolidWrappingScheme();
                if (boundCond.equals("klein")) BoidSimulator.wrappingScheme = new KleinWrappingScheme();
            }
            if (cmd.hasOption("update-mode")) {
                String updateModeStr = cmd.getOptionValue("update-mode");
                if (updateModeStr.equals("timed")) BoidSimulator.updateMode = Boid.UPDATE_TIMED;
            }
            if (cmd.hasOption("boid-sprite")) {
                String sprite = cmd.getOptionValue("boid-sprite");
                if (sprite.equals("particle")) Boid.boidTexture = TextureController.PARTICLE;
            }
            if (cmd.hasOption("load-file")) {
                BoidSimulator.filepathToLoad = cmd.getOptionValue("load-file");
            }
            if (cmd.hasOption("zoom-out")) BoidSimulator.zoomOut = true;
            if (cmd.hasOption("fullscreen")) config.fullscreen = true;
            if (cmd.hasOption("headless")) BoidSimulator.renderingOn = false;
            if (cmd.hasOption("show-correlations")) BoidSimulator.debugCorrelations = true;
        } catch (ParseException | NumberFormatException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
		    helpFormatter.printHelp("desktop-1.0", "Create a boid simulation", options, "", true);
		    return;
        }

        config.x = 1920/2 - config.width/2;
        config.y = 1040/2 - config.height/2;
        if (BoidSimulator.renderingOn) {
            new LwjglApplication(new BoidSimulator(), config);
        } else {
            BoidSimulator boidSimulator = new BoidSimulator();
            while (true) {
                boidSimulator.update();
            }
        }
	}
}
