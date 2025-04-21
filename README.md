# Auto-Healing Appium-Selenium Test Framework

A Java test automation framework that combines Appium and Selenium with TestNG for mobile and web testing, featuring auto-healing capabilities.

## Prerequisites

- Java 17
- Maven
- Appium Server
- Android SDK (for Android testing)
- Xcode (for iOS testing)

## Project Structure

```
├── pom.xml                 # Maven configuration
├── testng.xml              # TestNG test suite configuration
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── utils
    │   │               ├── AutoHealingDriver.java  # Auto-healing driver wrapper
    │   │               └── LoggingUtils.java       # Logging utilities
    │   └── resources
    │       └── logback.xml              # Logback configuration
    └── test
        └── java
            └── com
                └── example
                    └── tests
                        └── SampleAppiumTest.java   # Sample test class
```

## Auto-Healing Features

This framework implements a basic auto-healing mechanism that:

1. Tries multiple locator strategies when an element can't be found
2. Falls back to alternative locators in a specified order
3. Logs successful alternative strategies for potential learning
4. Provides a clean API for common actions (click, sendKeys, getText)

Example usage in tests:

```java
// Multiple locator strategies are provided as fallbacks
String headerText = autoHealingDriver.getText(
    By.tagName("h1"),                            // Primary locator
    By.xpath("//h1"),                            // Alternative 1
    By.cssSelector("div > h1"),                  // Alternative 2
    By.xpath("//*[contains(text(), 'Example')]") // Alternative 3
);
```

## Logging Implementation

The framework uses Logback for efficient and flexible logging:

1. **Console and File Logging**:
   - Console output for immediate visibility
   - File logging for persistent records

2. **Healing Event Tracking**:
   - CSV format logging of all healing events
   - Records timestamps, failed locators, successful alternatives

3. **Sensitive Data Masking**:
   - Automatic masking of passwords and sensitive information
   - Configurable patterns for different types of sensitive data

4. **Log Levels**:
   - DEBUG: Detailed tracing information
   - INFO: Normal application behavior
   - WARN: Issues that can be recovered from
   - ERROR: Application failures

5. **Healing Analytics**:
   - Generation of basic statistics on healing events
   - Support for advanced analytics through CSV log processing

## Setup

1. Install Appium:
   ```
   npm install -g appium
   ```

2. Start Appium server:
   ```
   appium
   ```

3. Configure device capabilities in the test class:
   - Update the `isAndroid` flag based on your target platform
   - Set the correct `deviceName` and other capabilities
   - Uncomment and update the `app` capability with path to your app

## Running Tests

Run tests using Maven:

```
mvn clean test
```

## Customization

- Add your test classes to the `src/test/java/com/example/tests` directory
- Update the `testng.xml` file to include your test classes
- Modify the capabilities in the test setup according to your device/emulator configuration
- Adjust the `logback.xml` file to customize logging behavior

## Extending the Auto-Healing Capabilities

The current implementation can be extended with:

1. Persistence of successful alternative locators
2. Machine learning to identify patterns in UI changes
3. Visual element recognition as a fallback strategy
4. Automatic generation of alternative locators
5. Reporting of healing activities for better maintainability 