# Auto-Healing Appium-Selenium (Helenium) Test Framework

A Java test automation framework that combines Appium and Selenium with TestNG for mobile and web testing, featuring advanced auto-healing capabilities.

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
    │   │               ├── AutoHealingDriver.java     # Original auto-healing driver (legacy)
    │   │               ├── AutoHealingElement.java    # Auto-healing WebElement implementation
    │   │               ├── ElementLocatorRepository.java # Repository for locator strategies
    │   │               ├── HeleniumDriver.java        # Improved Appium wrapper with healing
    │   │               └── LoggingUtils.java          # Logging utilities
    │   └── resources
    │       ├── logback.xml                # Logback configuration
    │       └── locators                   # Locator properties files
    │           └── LoginPage.properties   # Example locator definitions
    └── test
        └── java
            └── com
                └── example
                    └── tests
                        ├── SampleAppiumTest.java     # Original sample test
                        └── HeleniumMobileTest.java   # Advanced Helenium test
```

## Auto-Healing Architecture

The framework provides a robust approach to element locator healing for mobile applications:

1. **Locator Strategy Repository**
   - Centralized storage of element locators
   - Multiple strategies per element stored in properties files
   - Automatic tracking of the most successful strategies

2. **Self-Healing Elements**
   - WebElement wrapper with auto-healing capabilities
   - Dynamically switches between locator strategies on failure
   - Caches elements for improved performance
   - Implements the full WebElement interface

3. **Advanced HeleniumDriver**
   - Extends Appium's capabilities with healing powers
   - Persistent healing across test runs
   - Transparent healing with no test code changes needed
   - Comprehensive healing statistics and reports

4. **Easy Configuration**
   - Simple properties file format for locator definitions
   - Format: `elementType=locatorType=value;alternativeType=value`
   - Automatic loading and parsing of locator files

## Healing Process

When an element cannot be found:

1. The framework attempts to locate it with the primary (best known) strategy
2. If that fails, it systematically tries alternative strategies
3. Upon success, it records the working strategy for future use
4. The healing event is logged for analysis and reporting
5. Future test runs automatically use the previously successful strategy first

## Example Usage

### Locator Definition (in properties file)

```properties
# Format: locatorType=value;alternativeType=value;...
usernameField=id=username;xpath=//input[@name='username'];accessibilityid=username-input
```

### Using Elements in Tests

```java
// Simple usage referencing an element by ID
AutoHealingElement usernameField = driver.findElement("LoginPage.usernameField");
usernameField.sendKeys("testuser");

// Dynamic definition with healing capabilities
driver.findElement(
    By.id("login-button"),     // Primary strategy
    "LoginPage.loginButton",   // Element ID
    By.xpath("//button[@type='submit']"),  // Alternative 1
    By.accessibilityId("login-button")     // Alternative 2
);
```

## Setup

1. Install Appium:
   ```
   npm install -g appium
   ```

2. Start Appium server:
   ```
   appium
   ```

3. Define your element locators in properties files:
   - Create files in `src/main/resources/locators/`
   - Name them according to screens/pages (e.g., `LoginPage.properties`)
   - Define multiple locator strategies for each element

4. Run tests using Maven:
   ```
   mvn clean test
   ```

## Advanced Features

1. **Healing Analytics**
   - CSV format logging of all healing events
   - Statistical reports on healing activity
   - Identification of fragile locators

2. **Persistent Healing History**
   - Auto-healing data persists between test runs
   - Gradual improvement of test reliability over time
   - Automatic adaptation to UI changes

3. **Mobile-Specific Locators**
   - Support for AccessibilityId and other mobile-specific locators
   - Easy configuration for both Android and iOS
   - Optimized for native, hybrid, and web apps

4. **Security & Performance**
   - Automatic masking of sensitive data in logs
   - Element caching for better performance
   - Smart reuse of successful locator strategies 