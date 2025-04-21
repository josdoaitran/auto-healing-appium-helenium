package com.example.utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An auto-healing driver for Appium (Helenium) that provides self-healing capabilities
 * for mobile element locators.
 */
public class HeleniumDriver {
    private static final Logger logger = LoggerFactory.getLogger(HeleniumDriver.class);
    
    private final AppiumDriver driver;
    private final Map<String, AutoHealingElement> elementCache = new HashMap<>();
    private final ElementLocatorRepository locatorRepository;
    
    /**
     * Create a new HeleniumDriver for Android
     */
    public static HeleniumDriver createAndroidDriver(URL url, Capabilities capabilities) {
        return new HeleniumDriver(new AndroidDriver(url, capabilities));
    }
    
    /**
     * Create a new HeleniumDriver for iOS
     */
    public static HeleniumDriver createIOSDriver(URL url, Capabilities capabilities) {
        return new HeleniumDriver(new IOSDriver(url, capabilities));
    }
    
    /**
     * Create a new HeleniumDriver from an existing AppiumDriver
     */
    public HeleniumDriver(AppiumDriver driver) {
        this.driver = driver;
        this.locatorRepository = ElementLocatorRepository.getInstance();
        logger.info("Created HeleniumDriver with {} driver", 
                driver.getClass().getSimpleName());
    }
    
    /**
     * Find an element with auto-healing capabilities.
     * 
     * @param elementId The unique ID of the element
     * @return An auto-healing element
     */
    public AutoHealingElement findElement(String elementId) {
        logger.debug("Finding element with ID: {}", elementId);
        
        // Return from cache if available
        if (elementCache.containsKey(elementId)) {
            return elementCache.get(elementId);
        }
        
        // Create a new auto-healing element
        AutoHealingElement element = new AutoHealingElement(driver, elementId);
        elementCache.put(elementId, element);
        return element;
    }
    
    /**
     * Find an element with auto-healing using a locator.
     * This is for backward compatibility with existing code.
     * 
     * @param primaryLocator The primary locator to use
     * @param elementId The ID to associate with this element for healing
     * @param alternativeLocators Optional alternative locators
     * @return An auto-healing element
     */
    public AutoHealingElement findElement(By primaryLocator, String elementId, By... alternativeLocators) {
        logger.debug("Finding element by locator {} with ID: {}", primaryLocator, elementId);
        
        // If we already have this element defined, use the repository
        if (locatorRepository.getLocatorsForElement(elementId).size() > 0) {
            return findElement(elementId);
        }
        
        // Otherwise, add these locators to the repository
        List<By> locators = Arrays.asList(alternativeLocators);
        List<By> allLocators = locators.stream()
                .filter(l -> l != null)
                .collect(Collectors.toList());
                
        // Add primary locator as the first option
        allLocators.add(0, primaryLocator);
        
        locatorRepository.updateElementLocators(elementId, allLocators);
        
        // Return a new element with these locators
        return findElement(elementId);
    }
    
    /**
     * Add a new locator strategy for an element
     * 
     * @param elementId The element ID
     * @param locatorType The type of locator (id, xpath, etc.)
     * @param locatorValue The value of the locator
     */
    public void addLocatorStrategy(String elementId, String locatorType, String locatorValue) {
        By locator = createLocator(locatorType, locatorValue);
        if (locator != null) {
            List<By> existing = locatorRepository.getLocatorsForElement(elementId);
            existing.add(locator);
            locatorRepository.updateElementLocators(elementId, existing);
            logger.info("Added new locator strategy for element {}: {}={}", 
                    elementId, locatorType, locatorValue);
        }
    }
    
    /**
     * Create a By locator from type and value
     */
    private By createLocator(String type, String value) {
        switch (type.toLowerCase()) {
            case "id":
                return By.id(value);
            case "xpath":
                return By.xpath(value);
            case "name":
                return By.name(value);
            case "classname":
                return By.className(value);
            case "css":
                return By.cssSelector(value);
            case "linktext":
                return By.linkText(value);
            case "partiallinktext":
                return By.partialLinkText(value);
            case "tagname":
                return By.tagName(value);
            case "accessibilityid":
                return MobileBy.accessibilityId(value);
            default:
                logger.warn("Unsupported locator type: {}", type);
                return null;
        }
    }
    
    /**
     * Save the current state of healing locators
     */
    public void saveHealingData() {
        locatorRepository.saveHealingHistory();
    }
    
    /**
     * Generate a report of healing statistics
     */
    public void generateHealingReport() {
        LoggingUtils.generateHealingStatistics();
    }
    
    /**
     * Get the underlying Appium driver
     */
    public AppiumDriver getDriver() {
        return driver;
    }
    
    /**
     * Forward navigation to the Appium driver
     */
    public void navigate(String url) {
        logger.info("Navigating to: {}", url);
        driver.get(url);
    }
    
    /**
     * Clear the element cache
     */
    public void clearCache() {
        elementCache.clear();
        logger.debug("Element cache cleared");
    }
    
    /**
     * Quit the driver and save healing data
     */
    public void quit() {
        saveHealingData();
        driver.quit();
        logger.info("Driver quit and healing data saved");
    }
} 