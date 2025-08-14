This document outlines the guidelines for building the Price Comparator Service in Kotlin. The aim of this project is to compare prices by scraping pre-defined URLs, send the comparison results to a Telegram channel, and deploy the service as an AWS Lambda function that is triggered on a schedule.
### **1. Key Requirements**
1. Scraping pre-configured URLs to collect product price data.
2. Comparing prices and performing necessary calculations.
3. Sending the results via Telegram notifications.
4. Deploying the service as an AWS Lambda.
5. Running the Lambda function on a defined schedule using AWS CloudWatch Events (or EventBridge).
6. Using **package-by-feature** directory structure for modularity.

### **2. Project Features**
This service is broken into distinct features, each corresponding to a specific functionality:
- **Scraping**: Handles the logic for scraping content from URLs.
- **Processing**: Compares the extracted prices and prepares the result.
- **Notification**: Sends notifications to a Telegram channel using the Telegram Bot API.
- **Infrastructure**: AWS Lambda handler setup and related configurations.
- **Scheduler**: Configures the Lambda to run on a defined schedule.

### **3. Development Guidelines**
#### Kotlin-Specific Guidelines:
- Use **extension functions** where applicable for enhanced readability.
- Create **data classes** for representing scraped data.
- Use **coroutines** to handle concurrent scraping of multiple URLs.
- Follow Kotlin's idiomatic principles; avoid unnecessary Java interop.

#### Dependency Management:
- Scraping: Use `Jsoup` or a similar library for HTML parsing.
- Telegram Bot API: Use a library like `kotlin-telegram-bot`.
- For JSON parsing and config handling, use `kotlinx.serialization`.
