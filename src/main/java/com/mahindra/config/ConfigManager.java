package com.mahindra.config;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class ConfigManager {

    private static final String CONFIG_FILE_NAME = "config.yaml";
    public final static Logger logger = LogManager.getLogger(ConfigManager.class.getName());

    private static Map<String, Object> config;
    private static String env;

    // 🔹 Static block → loads once when class is loaded
    static {
        loadConfig();
        resolveEnvironment();
    }

    // 🔹 Load YAML file
    private static void loadConfig() {
        String userInputConfigFilePath = System.getProperty("userInputConfigFilePath");
        String path;

        if (userInputConfigFilePath == null || userInputConfigFilePath.trim().isEmpty()) {
            path = System.getProperty("user.dir") + File.separator + CONFIG_FILE_NAME;
        } else {
            File file = new File(userInputConfigFilePath.trim());
            if (file.isAbsolute()) {
                path = file.getAbsolutePath();
            } else {
                path = System.getProperty("user.dir") + File.separator + userInputConfigFilePath.trim();
            }
        }

        logger.info("Loading config file from path: {}", path);

        try {
            File file = new File(path);

            if (!file.exists()) {
                String errorMsg = "Config file NOT found at: " + path;
                logger.error(errorMsg);
                System.out.println("==========================================================================");
                System.out.println("❌ ERROR: Configuration file not found!");
                System.out.println("   👉 File Name  : " + CONFIG_FILE_NAME);
                System.out.println("   👉 Looked At  : " + path);
                System.out.println("   👉 Resolution : Please ensure the file exists at the specified location, or");
                System.out.println("                   pass a different file using: -DuserInputConfigFilePath=path/to/config.yaml");
                System.out.println("==========================================================================");
                System.exit(0);
            }

            try (InputStream input = new FileInputStream(file)) {
                Yaml yaml = new Yaml();
                config = yaml.load(input);
            }

            if (config == null || config.isEmpty()) {
                String errorMsg = "Config file is EMPTY at: " + path;
                logger.error(errorMsg);
                System.out.println("==========================================================================");
                System.out.println("❌ ERROR: Configuration file is empty!");
                System.out.println("   👉 Path  : " + path);
                System.out.println("   👉 Resolution : Ensure the yaml has valid configuration contents.");
                System.out.println("==========================================================================");
                System.exit(0);
            }

            logger.info("Config file loaded successfully. Total root keys: {}", config.size());

        } catch (Exception e) {
            String errorMsg = "Failed to load config file from path: " + path;
            logger.error("{} | Error: {}", errorMsg, e.getMessage(), e);
            System.out.println("==========================================================================");
            System.out.println("❌ ERROR: Failed to load config file!");
            System.out.println("   👉 Path  : " + path);
            System.out.println("   👉 Error : " + e.getMessage());
            System.out.println("==========================================================================");
            System.exit(0);
        }
    }

    private static void resolveEnvironment() {
        try {
            env = System.getProperty("env");
            logger.info("System property 'env' value: {}", (env != null ? env : "NULL (not passed via -Denv)"));

            if (env == null || env.isBlank()) {
                env = (String) config.get("defaultEnv");
                logger.info("Using 'defaultEnv' from config.yaml: {}", (env != null ? env : "NULL (not defined in YAML)"));
            }

            if (env == null || env.isBlank()) {
                String errorMsg = "Environment not specified! Pass -Denv=qa or define defaultEnv in YAML";
                logger.error(errorMsg);
                System.out.println("==========================================================================");
                System.out.println("❌ ERROR: Environment not specified!");
                System.out.println("   👉 Resolution : Please pass an environment using: -Denv=qa (or your env name)");
                System.out.println("                   or define 'defaultEnv: qa' in config.yaml.");
                System.out.println("==========================================================================");
                System.exit(0);
            }

            env = env.trim();

            // ✅ Validate if the environment section exists in config.yaml and is a map
            if (!config.containsKey(env) || !(config.get(env) instanceof Map)) {
                String errorMsg = "Invalid environment: '" + env + "'. Section not found in config.yaml";
                logger.error(errorMsg);

                java.util.List<String> validEnvs = new java.util.ArrayList<>();
                for (Map.Entry<String, Object> entry : config.entrySet()) {
                    if (entry.getValue() instanceof Map && !entry.getKey().equalsIgnoreCase("common")) {
                        validEnvs.add(entry.getKey());
                    }
                }

                System.out.println("==========================================================================");
                System.out.println("❌ ERROR: Invalid environment specified!");
                System.out.println("   👉 Environment Passed : " + env);
                System.out.println("   👉 Available in YAML   : " + validEnvs);
                System.out.println("   👉 Resolution          : Please choose a valid environment from the list above,");
                System.out.println("                            or declare a new section '" + env + "' in config.yaml.");
                System.out.println("==========================================================================");
                System.exit(0);
            }

            logger.info("✅ Running on ENV: {}", env);

        } catch (Exception e) {
            String errorMsg = "Failed to resolve environment";
            logger.error("{} | Error: {}", errorMsg, e.getMessage(), e);
            System.out.println("==========================================================================");
            System.out.println("❌ ERROR: Failed to resolve environment!");
            System.out.println("   👉 Error : " + e.getMessage());
            System.out.println("==========================================================================");
            System.exit(0);
        }
    }

    // 🔹 Public getter
    public static String get(String key) {

        try {
            Map<String, Object> envConfig = getMap(config, env);
            Map<String, Object> commonConfig = getMapSafe(config, "common");

            // Try ENV first
            Object value = getValue(envConfig, key);

            // Fallback to COMMON
            if (value == null && commonConfig != null) {
                value = getValue(commonConfig, key);
            }

            if (value == null) {
                String errorMsg = "Key NOT found: " + key + " in ENV: " + env + " or COMMON";
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            String result = String.valueOf(value);
            logger.info("ConfigManager.get('{}') => '{}'", key, result);
            return result;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = "Unexpected error fetching config key: " + key;
            logger.error("{} | Error: {}", errorMsg, e.getMessage(), e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    // 🔹 Extract nested value using dot notation
    private static Object getValue(Map<String, Object> source, String key) {

        if (source == null || key == null || key.isBlank()) {
            return null;
        }

        String[] keys = key.split("\\.");
        Object value = source;

        for (String k : keys) {
            if (!(value instanceof Map)) {
                return null;
            }

            value = ((Map<?, ?>) value).get(k);

            if (value == null) {
                return null;
            }
        }

        return value;
    }

    // 🔹 Safe map extractor (throws on missing/invalid)
    private static Map<String, Object> getMap(Map<String, Object> source, String key) {
        Object value = source.get(key);

        if (value == null) {
            String errorMsg = "Missing section: '" + key + "' in config.yaml";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        if (!(value instanceof Map)) {
            String errorMsg = "Invalid structure for key: '" + key + "' — expected a Map/Object, found: " + value.getClass().getSimpleName();
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) value;
        return result;
    }
    // 🔹 Safe map extractor (returns null on missing — for optional sections like "common")
    private static Map<String, Object> getMapSafe(Map<String, Object> source, String key) {
        try {
            Object value = source.get(key);
            if (value == null) {
                logger.debug("Optional section '{}' not found in config.yaml — skipping.", key);
                return null;
            }
            if (!(value instanceof Map)) {
                logger.warn("Section '{}' in config.yaml is not a Map — skipping. Found type: {}", key, value.getClass().getSimpleName());
                return null;
            }
            return (Map<String, Object>) value;
        } catch (Exception e) {
            logger.warn("Error accessing optional section '{}': {}", key, e.getMessage());
            return null;
        }
    }

    // 🔹 Get current environment
    public static String getEnv() {
        return env;
    }
}