package com.example.utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * A wrapper for AppiumDriver that provides auto-healing capabilities.
 * This is a basic implementation for demonstration purposes.
 */
public class AutoHealingDriver {
    private static final Logger logger = LoggerFactory.getLogger(AutoHealingDriver.class);
    private final AppiumDriver driver;

    public AutoHealingDriver(AppiumDriver driver) {
        this.driver = driver;
        logger.info("Initialized AutoHealingDriver with driver: {}", driver.getClass().getSimpleName());
    }

    /**
     * Find element with auto-healing capabilities.
     * If the primary locator fails, it will try alternative locators.
     */
    public WebElement findElement(By primaryLocator, By... alternativeLocators) {
        try {
            logger.debug("Attempting to find element with primary locator: {}", primaryLocator);
            WebElement element = driver.findElement(primaryLocator);
            logger.debug("Successfully found element with primary locator: {}", primaryLocator);
            return element;
        } catch (NoSuchElementException e) {
            logger.warn("Primary locator failed: {}. Reason: {}. Trying alternative locators.", 
                    primaryLocator, e.getMessage());
            return findElementWithAlternatives(primaryLocator, alternativeLocators);
        }
    }

    private WebElement findElementWithAlternatives(By primaryLocator, By[] alternativeLocators) {
        if (alternativeLocators.length == 0) {
            logger.error("No alternative locators provided after primary locator {} failed", primaryLocator);
            throw new NoSuchElementException("Element not found with primary locator and no alternatives provided");
        }

        List<By> locators = Arrays.asList(alternativeLocators);
        logger.debug("Trying {} alternative locators", locators.size());
        
        for (int i = 0; i < locators.size(); i++) {
            By locator = locators.get(i);
            try {
                logger.debug("Trying alternative locator {}/{}: {}", (i + 1), locators.size(), locator);
                WebElement element = driver.findElement(locator);
                
                // Log the successful healing event
                logger.info("Auto-healing successful! Element found with alternative locator {}: {} (primary: {})", 
                        (i + 1), locator, primaryLocator);
                
                // Record the healing event for analysis
                LoggingUtils.logHealingEvent(primaryLocator, locator, i + 1);
                
                return element;
            } catch (NoSuchElementException e) {
                logger.debug("Alternative locator {}/{} failed: {}", (i + 1), locators.size(), locator);
            }
        }
        
        logger.error("Element not found with primary or any of the {} alternative locators", locators.size());
        throw new NoSuchElementException(
                String.format("Element not found with primary locator %s or any of the %d alternatives",
                        primaryLocator, locators.size()));
    }

    /**
     * Click on an element with auto-healing capabilities.
     */
    public void click(By primaryLocator, By... alternativeLocators) {
        logger.debug("Attempting to click element with primary locator: {}", primaryLocator);
        WebElement element = findElement(primaryLocator, alternativeLocators);
        element.click();
        logger.debug("Successfully clicked element");
    }

    /**
     * Type text into an element with auto-healing capabilities.
     */
    public void sendKeys(By primaryLocator, String text, By... alternativeLocators) {
        // Mask any sensitive information before logging
        String maskedText = LoggingUtils.maskSensitiveInfo(text);
        logger.debug("Attempting to send keys '{}' to element with primary locator: {}", 
                maskedText, primaryLocator);
        
        WebElement element = findElement(primaryLocator, alternativeLocators);
        element.sendKeys(text);
        logger.debug("Successfully sent keys to element");
    }

    /**
     * Get text from an element with auto-healing capabilities.
     */
    public String getText(By primaryLocator, By... alternativeLocators) {
        logger.debug("Attempting to get text from element with primary locator: {}", primaryLocator);
        WebElement element = findElement(primaryLocator, alternativeLocators);
        String text = element.getText();
        logger.debug("Successfully got text from element: '{}'", text);
        return text;
    }

    /**
     * Get the wrapped AppiumDriver instance.
     */
    public AppiumDriver getDriver() {
        return driver;
    }
    
    /**
     * Generate healing statistics report
     */
    public void generateHealingReport() {
        LoggingUtils.generateHealingStatistics();
    }
} 