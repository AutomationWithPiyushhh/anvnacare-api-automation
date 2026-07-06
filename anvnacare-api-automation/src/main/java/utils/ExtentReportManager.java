package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import config.ConfigReader;
import constants.FrameworkConstants;
import java.io.File;

/**
 * ExtentReportManager is a helper class to configure and manage Extent Reports.
 * 
 * Why do we need it?
 * To generate modern, highly visual HTML dashboards showing test execution summaries.
 * 
 * Why is this approach better?
 * Using a ThreadLocal variable for ExtentTest ensures that when running tests in parallel, 
 * logs from different test threads do not overlap or get corrupted, guaranteeing report accuracy.
 */
public class ExtentReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();

    /**
     * Initializes Extent Reports. Called once during execution setup.
     */
    public static synchronized ExtentReports getReportInstance() {
        if (extent == null) {
            // Create target folder if it doesn't exist
            File directory = new File(FrameworkConstants.REPORT_OUTPUT_FOLDER);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(FrameworkConstants.getExtentReportFilePath());
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setDocumentTitle("ANVNACare API Automation Report");
            sparkReporter.config().setReportName("API Test Execution Dashboard");
            sparkReporter.config().setEncoding("utf-8");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            // Add runtime system configuration metadata
            extent.setSystemInfo("Application", "ANVNACare Healthcare Platform");
            extent.setSystemInfo("Environment", ConfigReader.getEnv().toUpperCase());
            extent.setSystemInfo("Base URL", ConfigReader.getBaseUrl());
            extent.setSystemInfo("Operating System", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("User", System.getProperty("user.name"));
        }
        return extent;
    }

    /**
     * Creates a test instance in the report and registers it on the active thread.
     * 
     * @param testName Name of the test method
     * @param description Brief test description
     * @return ExtentTest object
     */
    public static synchronized ExtentTest createTest(String testName, String description) {
        ExtentTest test = getReportInstance().createTest(testName, description);
        testThreadLocal.set(test);
        return test;
    }

    /**
     * Gets the ExtentTest instance bound to the calling thread.
     */
    public static ExtentTest getTest() {
        return testThreadLocal.get();
    }

    /**
     * Removes the ExtentTest instance from the ThreadLocal map.
     */
    public static void unloadTest() {
        testThreadLocal.remove();
    }

    /**
     * Writes all logged details to the output HTML file.
     */
    public static synchronized void flushReports() {
        if (extent != null) {
            extent.flush();
        }
    }
}
