package listeners;

import com.aventstack.extentreports.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.ExtentReportManager;

/**
 * TestListener implements ITestListener to intercept test executions and bind results to reports.
 * 
 * Why do we need it?
 * Rather than adding reporting lines manually in every test method, this listener acts as a 
 * central interceptor that catches when a test starts, succeeds, fails, or skips, and logs 
 * that info automatically.
 * 
 * Where is it used?
 * Registered in the testng.xml file or with a Class-level @Listeners annotation to listen to TestNG.
 * 
 * Why is this approach better?
 * Separates core assertion logic from reporting framework calls, maintaining clean code (SRP principle).
 */
public class TestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        logger.info("================ Test Suite: '{}' Started ================", context.getName());
        // Initialize report instance
        ExtentReportManager.getReportInstance();
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("================ Test Suite: '{}' Finished ================", context.getName());
        // Write all logs to HTML report
        ExtentReportManager.flushReports();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        logger.info("Executing Test Case: {}", testName);

        // Register the test case inside Extent Reports
        ExtentReportManager.createTest(testName, description);
        ExtentReportManager.getTest().log(Status.INFO, "Execution started for: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("Test Case: {} - PASSED", testName);

        ExtentReportManager.getTest().log(Status.PASS, "Test Case PASSED: " + testName);
        ExtentReportManager.unloadTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.error("Test Case: {} - FAILED. Reason: {}", testName, result.getThrowable().getMessage());

        ExtentReportManager.getTest().log(Status.FAIL, "Test Case FAILED: " + testName);
        ExtentReportManager.getTest().fail(result.getThrowable());
        ExtentReportManager.unloadTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.warn("Test Case: {} - SKIPPED", testName);

        ExtentReportManager.getTest().log(Status.SKIP, "Test Case SKIPPED: " + testName);
        ExtentReportManager.getTest().log(Status.SKIP, result.getThrowable());
        ExtentReportManager.unloadTest();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Not used
    }
}
