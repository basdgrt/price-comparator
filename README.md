# Price Comparator

A service that compares prices by scraping pre-defined URLs, sends the comparison results to a Telegram channel, and is deployed as an AWS Lambda function triggered on a schedule.

## Setup

### Secrets Configuration

The application requires a `secrets.yaml` file for storing sensitive information. This file is not tracked in git for security reasons.

1. Copy the template file:
   ```
   cp src/main/resources/secrets.yaml.template src/main/resources/secrets.yaml
   ```

2. Edit the `secrets.yaml` file and replace the placeholder values with your actual credentials:
   ```yaml
   botToken: "your_telegram_bot_token"
   chatId: "your_telegram_chat_id"
   ```

## Features

- Scrapes product prices from multiple websites
- Compares prices and identifies the best deals
- Sends notifications to a Telegram channel
- Runs on a schedule as an AWS Lambda function

## Building the application

1. Build the application as a fat JAR using the Shadow plugin:
   ```
   ./gradlew shadowJar
   ```

2. The JAR file will be created in the `build/libs` directory as `price-comparator.jar`.