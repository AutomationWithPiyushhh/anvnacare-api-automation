package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader is responsible for loading and retrieving configuration options
 * from the src/main/resources/config.properties file.
 * 
 * Why do we need it?
 * We want to avoid hardcoding environment URLs, logins, and settings in our Java code.
 * This class reads them from a central properties file, making the framework configurable.
 * 
 * Where is it used?
 * It is used primarily by base API setup classes, endpoint routing classes, and test hooks
 * to fetch URLs and default testing credentials dynamically.
 * 
 * Why is this approach better?
 * Loading the properties file in a static block ensures it is read into memory only once
 * when the class is first loaded, optimizing memory and file I/O operations.
 */
public class ConfigReader {

    private static final Properties properties = new Properties();

    // Static block to load properties once when the class is loaded
    static {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Error: config.properties file not found in resources directory.");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading config.properties file", ex);
        }
    }

    /**
     * Retrieves value corresponding to the key from config properties.
     * @param key Config key name
     * @return String value
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Retrieves the target environment.
     * Real-world improvement: Allows overriding via System properties (command line)
     * e.g., 'mvn test -Denv=uat', falling back to the properties file default.
     * 
     * @return Target environment name (e.g., qa, uat, prod)
     */
    public static String getEnv() {
        String sysEnv = System.getProperty("env");
        if (sysEnv != null && !sysEnv.trim().isEmpty()) {
            return sysEnv.trim().toLowerCase();
        }
        return getProperty("env");
    }

    /**
     * Retrieves the base URL based on the active environment.
     * @return Dynamic Base URL
     */
    public static String getBaseUrl() {
        String env = getEnv();
        String urlKey = env + ".baseUrl";
        String baseUrl = getProperty(urlKey);
        if (baseUrl == null) {
            throw new IllegalArgumentException("Error: No base URL found for environment: " + env);
        }
        return baseUrl;
    }
}
