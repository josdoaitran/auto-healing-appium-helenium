package com.example.utils;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for logging operations specific to the auto-healing framework.
 */
public class LoggingUtils {
    private static final Logger logger = LoggerFactory.getLogger(LoggingUtils.class);
    
    /**
     * Logs healing events to a dedicated log file for later analysis.
     * 
     * @param primaryLocator The primary locator that failed
     * @param successfulLocator The locator that successfully found the element
     * @param attempts Number of attempts made
     */
    public static void logHealingEvent(By primaryLocator, By successfulLocator, int attempts) {
        try {
            // Ensure the logs directory exists
            File logDir = new File("logs/healing");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // Create a log record in CSV format
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String logEntry = String.format("%s,%s,%s,%d%n", 
                    timestamp, 
                    primaryLocator.toString(), 
                    successfulLocator.toString(), 
                    attempts);
            
            // Append to the healing log
            Files.write(
                    Paths.get("logs/healing/healing-events.csv"),
                    logEntry.getBytes(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
            
            logger.debug("Recorded healing event: primary={}, successful={}, attempts={}", 
                    primaryLocator, successfulLocator, attempts);
        } catch (Exception e) {
            logger.error("Failed to record healing event", e);
        }
    }
    
    /**
     * Creates a statistical summary of healing events.
     * This could be used for reporting purposes.
     */
    public static void generateHealingStatistics() {
        try {
            File healingLogFile = new File("logs/healing/healing-events.csv");
            if (!healingLogFile.exists()) {
                logger.info("No healing events recorded yet.");
                return;
            }
            
            long totalEvents = Files.lines(healingLogFile.toPath()).count();
            logger.info("Total healing events: {}", totalEvents);
            
            // More sophisticated analysis could be added here
        } catch (Exception e) {
            logger.error("Failed to generate healing statistics", e);
        }
    }
    
    /**
     * Masks sensitive information in strings for logging purposes.
     * 
     * @param input The string that might contain sensitive information
     * @return A masked string safe for logging
     */
    public static String maskSensitiveInfo(String input) {
        if (input == null) {
            return null;
        }
        
        // Mask passwords
        String masked = input.replaceAll("(?i)password=\\w+", "password=****");
        
        // Mask other sensitive information as needed
        // masked = masked.replaceAll("\\d{4}-\\d{4}-\\d{4}-\\d{4}", "****-****-****-****"); // Credit cards
        
        return masked;
    }
} 