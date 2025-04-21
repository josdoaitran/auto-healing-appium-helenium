package com.example.utils;

import io.appium.java_client.MobileBy;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for storing and managing element locators with auto-healing capabilities.
 * This class serves as the central database for all element locators and their alternatives.
 */
public class ElementLocatorRepository {
    private static final Logger logger = LoggerFactory.getLogger(ElementLocatorRepository.class);
    
    private static final String REPOSITORY_DIR = "src/main/resources/locators";
    private static final String HEALING_HISTORY_FILE = "logs/healing/healing-history.properties";
    
    // Main locator repository: elementId -> List of locator strategies
    private final Map<String, List<By>> locatorRepository = new ConcurrentHashMap<>();
    
    // Healing history: elementId -> best working locator index
    private final Map<String, Integer> healingHistory = new ConcurrentHashMap<>();
    
    // Singleton instance
    private static ElementLocatorRepository instance;
    
    /**
     * Get singleton instance of the repository
     */
    public static synchronized ElementLocatorRepository getInstance() {
        if (instance == null) {
            instance = new ElementLocatorRepository();
        }
        return instance;
    }
    
    /**
     * Private constructor to enforce singleton pattern
     */
    private ElementLocatorRepository() {
        initializeRepository();
        loadHealingHistory();
    }
    
    /**
     * Initialize the repository from locator files
     */
    private void initializeRepository() {
        try {
            File repoDir = new File(REPOSITORY_DIR);
            if (!repoDir.exists()) {
                repoDir.mkdirs();
                logger.info("Created locator repository directory: {}", REPOSITORY_DIR);
                return;
            }
            
            File[] locatorFiles = repoDir.listFiles((dir, name) -> name.endsWith(".properties"));
            if (locatorFiles == null || locatorFiles.length == 0) {
                logger.info("No locator files found in repository");
                return;
            }
            
            for (File file : locatorFiles) {
                try {
                    loadLocatorsFromFile(file);
                } catch (Exception e) {
                    logger.error("Failed to load locators from file: {}", file.getName(), e);
                }
            }
            
            logger.info("Loaded {} element locators into repository", locatorRepository.size());
        } catch (Exception e) {
            logger.error("Failed to initialize locator repository", e);
        }
    }
    
    /**
     * Load locators from a properties file
     */
    private void loadLocatorsFromFile(File file) throws IOException {
        Properties props = new Properties();
        try (FileReader reader = new FileReader(file)) {
            props.load(reader);
            
            String pageName = file.getName().replace(".properties", "");
            
            for (String key : props.stringPropertyNames()) {
                String locatorStr = props.getProperty(key);
                String elementId = pageName + "." + key;
                
                List<By> locators = parseLocators(locatorStr);
                if (!locators.isEmpty()) {
                    locatorRepository.put(elementId, locators);
                    logger.debug("Loaded locator for element: {}", elementId);
                }
            }
        }
    }
    
    /**
     * Parse a string of locator strategies into a list of By objects
     * Format: "strategy1=value1;strategy2=value2;..."
     */
    private List<By> parseLocators(String locatorStr) {
        List<By> locators = new ArrayList<>();
        
        String[] strategies = locatorStr.split(";");
        for (String strategy : strategies) {
            String[] parts = strategy.trim().split("=", 2);
            if (parts.length != 2) {
                logger.warn("Invalid locator format: {}", strategy);
                continue;
            }
            
            String type = parts[0].trim().toLowerCase();
            String value = parts[1].trim();
            
            By locator = createByLocator(type, value);
            if (locator != null) {
                locators.add(locator);
            }
        }
        
        return locators;
    }
    
    /**
     * Create a By locator based on the strategy type and value
     */
    private By createByLocator(String type, String value) {
        switch (type) {
            case "id":
                return By.id(value);
            case "name":
                return By.name(value);
            case "xpath":
                return By.xpath(value);
            case "css":
                return By.cssSelector(value);
            case "classname":
                return By.className(value);
            case "tagname":
                return By.tagName(value);
            case "linktext":
                return By.linkText(value);
            case "partiallinktext":
                return By.partialLinkText(value);
            case "accessibilityid":
                return MobileBy.accessibilityId(value);
            default:
                logger.warn("Unsupported locator type: {}", type);
                return null;
        }
    }
    
    /**
     * Load healing history from file
     */
    private void loadHealingHistory() {
        try {
            File historyFile = new File(HEALING_HISTORY_FILE);
            if (!historyFile.exists()) {
                logger.info("No healing history file found");
                return;
            }
            
            Properties props = new Properties();
            try (FileReader reader = new FileReader(historyFile)) {
                props.load(reader);
                
                for (String elementId : props.stringPropertyNames()) {
                    try {
                        int bestIndex = Integer.parseInt(props.getProperty(elementId));
                        healingHistory.put(elementId, bestIndex);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid healing index for element {}: {}", 
                                elementId, props.getProperty(elementId));
                    }
                }
            }
            
            logger.info("Loaded healing history for {} elements", healingHistory.size());
        } catch (Exception e) {
            logger.error("Failed to load healing history", e);
        }
    }
    
    /**
     * Save healing history to file
     */
    public void saveHealingHistory() {
        try {
            File historyFile = new File(HEALING_HISTORY_FILE);
            File parentDir = historyFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            Properties props = new Properties();
            for (Map.Entry<String, Integer> entry : healingHistory.entrySet()) {
                props.setProperty(entry.getKey(), entry.getValue().toString());
            }
            
            try (FileWriter writer = new FileWriter(historyFile)) {
                props.store(writer, "Auto-healing locator history");
                logger.info("Saved healing history for {} elements", healingHistory.size());
            }
        } catch (Exception e) {
            logger.error("Failed to save healing history", e);
        }
    }
    
    /**
     * Register a successful locator healing event
     */
    public void registerSuccessfulHealing(String elementId, int successfulIndex) {
        healingHistory.put(elementId, successfulIndex);
        logger.info("Registered successful healing for element {}: using strategy index {}", 
                elementId, successfulIndex);
        
        // Periodically save to file (could be optimized to save less frequently)
        saveHealingHistory();
    }
    
    /**
     * Get all locator strategies for an element
     */
    public List<By> getLocatorsForElement(String elementId) {
        return locatorRepository.getOrDefault(elementId, new ArrayList<>());
    }
    
    /**
     * Get the best locator for an element based on healing history
     */
    public By getBestLocatorForElement(String elementId) {
        List<By> locators = getLocatorsForElement(elementId);
        if (locators.isEmpty()) {
            logger.warn("No locators found for element: {}", elementId);
            return null;
        }
        
        // If we have healing history, use the best known locator
        if (healingHistory.containsKey(elementId)) {
            int bestIndex = healingHistory.get(elementId);
            if (bestIndex >= 0 && bestIndex < locators.size()) {
                logger.debug("Using best known locator (index {}) for element {}", bestIndex, elementId);
                return locators.get(bestIndex);
            }
        }
        
        // Otherwise, use the first locator as default
        logger.debug("Using default locator for element {}", elementId);
        return locators.get(0);
    }
    
    /**
     * Add or update an element's locators in the repository
     */
    public void updateElementLocators(String elementId, List<By> locators) {
        locatorRepository.put(elementId, new ArrayList<>(locators));
        
        // If this is a new element or the locator list changed, reset healing history
        healingHistory.remove(elementId);
        
        logger.info("Updated locators for element {}: {} strategies", 
                elementId, locators.size());
    }
} 