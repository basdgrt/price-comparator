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

## AWS Lambda Deployment

### Building the Lambda Package

1. Build the application as a fat JAR using the Shadow plugin:
   ```
   ./gradlew shadowJar
   ```

2. The JAR file will be created in the `build/libs` directory as `price-comparator.jar`.

### Deploying to AWS Lambda

1. Log in to the AWS Management Console and navigate to the Lambda service.

2. Click "Create function" and select "Author from scratch".

3. Enter a function name (e.g., "price-comparator").

4. For Runtime, select "Java 17".

5. Under "Function code", upload the JAR file from `build/libs/price-comparator.jar`.

6. Set the Handler to: `com.github.basdgrt.infrastructure.LambdaHandler::handleRequest`

7. Configure memory and timeout settings:
   - Memory: At least 512 MB recommended
   - Timeout: At least 1 minute (depending on how many products you're scraping)

8. Configure environment variables if needed (alternatively, you can include the secrets.yaml file in the JAR).

9. Click "Create function" to deploy the Lambda.

### Setting Up a Schedule

1. In the Lambda function configuration, go to the "Triggers" tab.

2. Click "Add trigger" and select "EventBridge (CloudWatch Events)".

3. Create a new rule with a schedule expression:
   - For daily execution: `cron(0 8 * * ? *)`  # Runs at 8:00 AM UTC every day
   - For hourly execution: `rate(1 hour)`

4. Click "Add" to create the scheduled trigger.
