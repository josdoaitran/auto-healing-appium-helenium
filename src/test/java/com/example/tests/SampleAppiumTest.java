package com.example.tests;

import com.example.utils.AutoHealingDriver;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;
import java.time.Duration;

public class SampleAppiumTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleAppiumTest.class);
    private AppiumDriver driver;
    private AutoHealingDriver autoHealingDriver;
    private boolean isAndroid = true; // Change to false for iOS

    @BeforeMethod
    public void setUp() throws Exception {
        logger.info("Setting up test environment");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        if (isAndroid) {
            // Android setup
            capabilities.setCapability("platformName", "Android");
            capabilities.setCapability("automationName", "UiAutomator2");
            capabilities.setCapability("deviceName", "Android Device");
            // capabilities.setCapability("app", "/path/to/your/app.apk");
            
            // Or for web testing
            capabilities.setCapability("browserName", "Chrome");
            
            logger.info("Initializing Android driver");
            driver = new AndroidDriver(new URL("http://localhost:4723/wd/hub"), capabilities);
        } else {
            // iOS setup
            capabilities.setCapability("platformName", "iOS");
            capabilities.setCapability("automationName", "XCUITest");
            capabilities.setCapability("deviceName", "iPhone Simulator");
            // capabilities.setCapability("app", "/path/to/your/app.ipa");
            
            // Or for web testing
            capabilities.setCapability("browserName", "Safari");
            
            logger.info("Initializing iOS driver");
            driver = new IOSDriver(new URL("http://localhost:4723/wd/hub"), capabilities);
        }
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        autoHealingDriver = new AutoHealingDriver(driver);
        logger.info("Test setup completed");
    }
    
    @Test
    public void sampleTest() {
        logger.info("Starting sample test");
        // For web testing
        driver.get("https://www.example.com");
        
        // Example assertion
        String pageTitle = driver.getTitle();
        Assert.assertTrue(pageTitle.contains("Example"), "Page title should contain 'Example'");
        
        // Example of finding an element using auto-healing driver
        // Multiple locator strategies are provided as fallbacks
        String headerText = autoHealingDriver.getText(
            By.tagName("h1"),                               // Primary locator
            By.xpath("//h1"),                               // Alternative 1
            By.cssSelector("div > h1"),                     // Alternative 2
            By.xpath("//*[contains(text(), 'Example')]")    // Alternative 3
        );
        
        Assert.assertEquals(headerText, "Example Domain", "Header text should match");
        logger.info("Sample test completed successfully");
    }
    
    @Test
    public void demoAutoHealingClick() {
        logger.info("Starting demo auto-healing click test");
        driver.get("https://www.example.com");
        
        // Intentionally use a wrong locator first to trigger auto-healing
        try {
            logger.info("Testing intentional failure to demonstrate auto-healing");
            autoHealingDriver.click(
                By.linkText("Wrong Link Text"),              // Will fail
                By.linkText("More information..."),          // Should succeed
                By.partialLinkText("More information"),
                By.xpath("//a[contains(@href, 'iana.org')]"),
                By.cssSelector("a[href*='iana']")
            );
            
            // Verify we navigated to the IANA page
            Assert.assertTrue(driver.getCurrentUrl().contains("iana.org"), 
                             "Should navigate to IANA website");
            
            logger.info("Auto-healing click test completed successfully");
        } catch (Exception e) {
            logger.error("Test failed", e);
            throw e;
        }
    }
    
    @AfterMethod
    public void tearDown() {
        logger.info("Tearing down test");
        if (driver != null) {
            driver.quit();
        }
    }
    
    @AfterSuite
    public void afterSuite() {
        logger.info("Test suite completed, generating healing report");
        // If we had an instance of autoHealingDriver here, we could call:
        // autoHealingDriver.generateHealingReport();
        
        // As a workaround, we'll create a temporary instance just for the report
        try {
            new AutoHealingDriver(null).generateHealingReport();
        } catch (Exception e) {
            logger.warn("Could not generate healing report", e);
        }
    }
} 