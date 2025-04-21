package com.example.tests;

import com.example.utils.AutoHealingElement;
import com.example.utils.HeleniumDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;
import java.time.Duration;

public class HeleniumMobileTest {
    private static final Logger logger = LoggerFactory.getLogger(HeleniumMobileTest.class);
    private HeleniumDriver driver;
    private boolean isAndroid = true; // Change to false for iOS
    
    @BeforeMethod
    public void setUp() throws Exception {
        logger.info("Setting up test environment with Helenium Driver");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        if (isAndroid) {
            // Android setup
            capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
            capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android Device");
            // capabilities.setCapability(MobileCapabilityType.APP, "/path/to/your/app.apk");
            capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Chrome");
            
            logger.info("Initializing Helenium Android driver");
            driver = HeleniumDriver.createAndroidDriver(
                    new URL("http://localhost:4723/wd/hub"), 
                    capabilities);
        } else {
            // iOS setup
            capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "iOS");
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
            capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "iPhone Simulator");
            // capabilities.setCapability(MobileCapabilityType.APP, "/path/to/your/app.ipa");
            capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Safari");
            
            logger.info("Initializing Helenium iOS driver");
            driver = HeleniumDriver.createIOSDriver(
                    new URL("http://localhost:4723/wd/hub"), 
                    capabilities);
        }
        
        // Set implicit wait time
        driver.getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        logger.info("Test setup completed with Helenium driver");
    }
    
    @Test
    public void webBasedLoginTest() {
        logger.info("Starting web-based login test with auto-healing elements");
        // Navigate to a test login page
        driver.navigate("https://the-internet.herokuapp.com/login");
        
        // Get auto-healing elements using their IDs from the properties file
        AutoHealingElement usernameField = driver.findElement("LoginPage.usernameField");
        AutoHealingElement passwordField = driver.findElement("LoginPage.passwordField");
        AutoHealingElement loginButton = driver.findElement("LoginPage.loginButton");
        
        // Test if the page loaded correctly
        Assert.assertTrue(usernameField.isDisplayed(), "Username field should be displayed");
        
        // Enter credentials
        usernameField.sendKeys("tomsmith");
        passwordField.sendKeys("SuperSecretPassword!");
        
        // Click login button
        loginButton.click();
        
        // Verify we're logged in by checking the URL
        String currentUrl = driver.getDriver().getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/secure"), 
                "Should navigate to secure area after login");
        
        logger.info("Web-based login test completed successfully");
    }
    
    @Test
    public void demonstrateAutoHealing() {
        logger.info("Starting test to demonstrate auto-healing with intentional failures");
        driver.navigate("https://the-internet.herokuapp.com/login");
        
        // Define element with broken primary locator but working alternatives
        // In practice, this would come from the properties file, but here we show how to do it programmatically
        driver.findElement(
                // This primary locator is intentionally wrong to trigger healing
                org.openqa.selenium.By.id("wrong-username-id"),
                "LoginPage.brokenUsernameField",
                // These alternatives will work
                org.openqa.selenium.By.name("username"),
                org.openqa.selenium.By.xpath("//input[@id='username']")
        );
        
        // Use the element - should trigger auto-healing to find the alternative
        AutoHealingElement brokenField = driver.findElement("LoginPage.brokenUsernameField");
        brokenField.clear();
        brokenField.sendKeys("tomsmith");
        
        // Get the password field and log in
        AutoHealingElement passwordField = driver.findElement("LoginPage.passwordField");
        passwordField.sendKeys("SuperSecretPassword!");
        
        // Get the login button and click it
        AutoHealingElement loginButton = driver.findElement("LoginPage.loginButton");
        loginButton.click();
        
        // Verify login was successful
        String pageSource = driver.getDriver().getPageSource();
        Assert.assertTrue(pageSource.contains("You logged into a secure area!"), 
                "Should see success message after login");
        
        // Generate a healing report
        driver.generateHealingReport();
        
        logger.info("Auto-healing demonstration test completed successfully");
    }
    
    @AfterMethod
    public void tearDown() {
        logger.info("Tearing down test");
        if (driver != null) {
            // This will also save the healing data
            driver.quit();
        }
    }
} 