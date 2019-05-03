package smc;

import util.ConfigLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

public class SMCModelLoader {

    List<SMCModel> models;
    long lastModified;

    public SMCModelLoader() {
    }

    public List<SMCModel> loadModels() {

        try {
            File configFile = new File(ConfigLoader.configFileLocation);
            if (!configFile.exists()) {
                throw new RuntimeException("SMCConfig.properties file not found at following path: " + ConfigLoader.configFileLocation);
            } else {
                long lastModified = configFile.lastModified();
                if (this.lastModified == lastModified) {
                    return models;
                }
                this.lastModified = lastModified;
            }

            models = new LinkedList<>();
            ConfigLoader configLoader = ConfigLoader.getInstance();

            // load the requirements, aka the models to be predicted by the smc
            String reqs[] = configLoader.getProperty("requirements").split(",");

            // get where the folders are located
            String modelsFolderName = configLoader.getProperty("modelsFolderName");

            Path mfp = Paths.get(System.getProperty("user.dir"), modelsFolderName);
            String modelsFolderPath = mfp.toString();

            // In case the target folder does not exist yet, create it
            new File(Paths.get(modelsFolderPath, "target").toString()).mkdir();

            for (String req : reqs) {
                String key, path, simulations = "25", alpha = "0.05", epsilon = "0.05", model;
                ModelType type = null;

                key = req.trim();

                // Get path to model
                String modelFileName = configLoader.getProperty(key + ".modelFileName");
                mfp = Paths.get(modelsFolderPath, modelFileName);

                // Copy the model to the target folder so that it is only modified there
                Files.copy(mfp, Paths.get(modelsFolderPath, "target", modelFileName), StandardCopyOption.REPLACE_EXISTING);
                path = Paths.get(modelsFolderPath, "target", modelFileName).toString();

                String modelType = configLoader.getProperty(key + ".type");
                if (modelType.equalsIgnoreCase("simulation")) {
                    type = ModelType.SIMULATION;
                    simulations = configLoader.getProperty(key + ".totalSimulations");
                } else if (modelType.equalsIgnoreCase("probability")) {
                    type = ModelType.PROBABILITY;
                    alpha = configLoader.getProperty(key + ".alpha");
                    epsilon = configLoader.getProperty(key + ".epsilon");
                }

                // read in the model from the filepath
                model = new String(Files.readAllBytes(Paths.get(path)), Charset.defaultCharset());

                // construct model from the properties you have read
                SMCModel smcModel = new SMCModel(key, path, type, Integer.parseInt(simulations),
                        Double.parseDouble(alpha), Double.parseDouble(epsilon), model);

                // add the model
                models.add(smcModel);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return models;
    }
}
