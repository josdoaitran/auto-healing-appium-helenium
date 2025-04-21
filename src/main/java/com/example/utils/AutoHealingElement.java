package com.example.utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A wrapper for WebElement that provides auto-healing capabilities.
 * This class dynamically switches between different locator strategies if the primary one fails.
 */
public class AutoHealingElement implements WebElement {
    private static final Logger logger = LoggerFactory.getLogger(AutoHealingElement.class);
    
    private final AppiumDriver driver;
    private final String elementId;
    private WebElement cachedElement;
    private By currentLocator;
    
    /**
     * Create a new auto-healing element
     *
     * @param driver The Appium driver
     * @param elementId The unique identifier for this element
     */
    public AutoHealingElement(AppiumDriver driver, String elementId) {
        this.driver = driver;
        this.elementId = elementId;
        this.currentLocator = ElementLocatorRepository.getInstance().getBestLocatorForElement(elementId);
        
        if (this.currentLocator == null) {
            throw new IllegalArgumentException("No locators defined for element: " + elementId);
        }
    }
    
    /**
     * Find the actual WebElement, with auto-healing if necessary
     * 
     * @return The found WebElement
     * @throws NoSuchElementException if the element cannot be found with any strategy
     */
    private WebElement findElement() {
        // If we have a cached element that's still valid, use it
        if (cachedElement != null) {
            try {
                // Check if the element is still valid by getting a property
                cachedElement.isDisplayed();
                return cachedElement;
            } catch (StaleElementReferenceException e) {
                logger.debug("Cached element for {} is stale, will find again", elementId);
                cachedElement = null;
            }
        }
        
        // Try with the current (best known) locator first
        try {
            logger.debug("Finding element {} with locator: {}", elementId, currentLocator);
            cachedElement = driver.findElement(currentLocator);
            return cachedElement;
        } catch (NoSuchElementException e) {
            logger.warn("Failed to find element {} with primary locator: {}", elementId, currentLocator);
            // Try alternative locators
            return findElementWithAlternatives();
        }
    }
    
    /**
     * Try alternative locator strategies when the primary one fails
     * 
     * @return The found WebElement
     * @throws NoSuchElementException if the element cannot be found with any strategy
     */
    private WebElement findElementWithAlternatives() {
        ElementLocatorRepository repository = ElementLocatorRepository.getInstance();
        List<By> allLocators = repository.getLocatorsForElement(elementId);
        
        if (allLocators.size() <= 1) {
            logger.error("No alternative locators available for element: {}", elementId);
            throw new NoSuchElementException("Element not found and no alternatives available: " + elementId);
        }
        
        // Loop through all locators and try each one
        for (int i = 0; i < allLocators.size(); i++) {
            By locator = allLocators.get(i);
            
            // Skip the current locator since we already tried it
            if (locator.equals(currentLocator)) {
                continue;
            }
            
            try {
                logger.debug("Trying alternative locator {}/{} for element {}: {}", 
                        i + 1, allLocators.size(), elementId, locator);
                WebElement element = driver.findElement(locator);
                
                // If we found the element, update the repository with this successful strategy
                logger.info("Auto-healing successful for element {}. Using alternative locator: {}", 
                        elementId, locator);
                
                // Record this as the best locator for future use
                int locatorIndex = allLocators.indexOf(locator);
                repository.registerSuccessfulHealing(elementId, locatorIndex);
                
                // Update our current locator
                currentLocator = locator;
                cachedElement = element;
                
                // Log the healing event
                LoggingUtils.logHealingEvent(
                        allLocators.get(0), // Original primary locator
                        locator,            // Successful locator
                        i + 1               // Attempt number
                );
                
                return element;
            } catch (NoSuchElementException e) {
                logger.debug("Alternative locator failed for element {}: {}", elementId, locator);
            }
        }
        
        // If we get here, all locators failed
        logger.error("Failed to find element {} with any locator strategy", elementId);
        throw new NoSuchElementException("Element not found with any locator strategy: " + elementId);
    }

    @Override
    public void click() {
        logger.debug("Clicking on element: {}", elementId);
        findElement().click();
    }

    @Override
    public void submit() {
        logger.debug("Submitting form with element: {}", elementId);
        findElement().submit();
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        // Mask sensitive data in logs
        String maskedText = LoggingUtils.maskSensitiveInfo(String.join("", keysToSend));
        logger.debug("Sending keys to element {}: {}", elementId, maskedText);
        findElement().sendKeys(keysToSend);
    }

    @Override
    public void clear() {
        logger.debug("Clearing element: {}", elementId);
        findElement().clear();
    }

    @Override
    public String getTagName() {
        return findElement().getTagName();
    }

    @Override
    public String getAttribute(String name) {
        return findElement().getAttribute(name);
    }

    @Override
    public boolean isSelected() {
        return findElement().isSelected();
    }

    @Override
    public boolean isEnabled() {
        return findElement().isEnabled();
    }

    @Override
    public String getText() {
        String text = findElement().getText();
        logger.debug("Got text from element {}: {}", elementId, text);
        return text;
    }

    @Override
    public List<WebElement> findElements(By by) {
        return findElement().findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return findElement().findElement(by);
    }

    @Override
    public boolean isDisplayed() {
        return findElement().isDisplayed();
    }

    @Override
    public Point getLocation() {
        return findElement().getLocation();
    }

    @Override
    public Dimension getSize() {
        return findElement().getSize();
    }

    @Override
    public Rectangle getRect() {
        return findElement().getRect();
    }

    @Override
    public String getCssValue(String propertyName) {
        return findElement().getCssValue(propertyName);
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) {
        return findElement().getScreenshotAs(target);
    }
    
    /**
     * Get the current locator being used for this element
     * 
     * @return The current locator
     */
    public By getCurrentLocator() {
        return currentLocator;
    }
    
    /**
     * Get the element ID
     * 
     * @return The element ID
     */
    public String getElementId() {
        return elementId;
    }
} 