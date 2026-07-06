package constants;

import java.io.File;

/**
 * FrameworkConstants contains all the static final constants used across the automation framework.
 * 
 * Why do we need it?
 * To avoid hardcoding file paths, timeouts, and naming conventions in multiple files.
 * If a directory location changes, we only need to update it here.
 * 
 * Where is it used?
 * Used by ExtentReportManager, test classes, utility classes, and base setup.
 * 
 * Why is this approach better?
 * Centralizing resource paths prevents typos and directory misalignment when running
 * tests in different IDEs or on CI/CD servers.
 */
public final class FrameworkConstants {

    // Prevent instantiation of utility class
    private FrameworkConstants() {}

    public static final String PROJECT_PATH = System.getProperty("user.dir");

    // File Paths
    public static final String CONFIG_FILE_PATH = PROJECT_PATH + File.separator 
            + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "config.properties";

    public static final String REPORT_OUTPUT_FOLDER = PROJECT_PATH + File.separator + "reports" + File.separator;
    public static final String EXTENT_REPORT_NAME = "ANVNACare_API_Automation_Report.html";

    // System timeouts and connection properties
    public static final long DEFAULT_TIMEOUT_SECONDS = 10;
    public static final long MAX_RESPONSE_TIME_MS = 10000; // API response time SLA (10 seconds)

    /**
     * Resolves the full path of the Extent Report file.
     * @return Full report path
     */
    public static String getExtentReportFilePath() {
        return REPORT_OUTPUT_FOLDER + EXTENT_REPORT_NAME;
    }
}
