# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Kotlin-based price comparison service that scrapes product prices from multiple Dutch webshops, compares them against original prices, and sends notifications via Telegram. The application is designed to run as a fat JAR on a laptop.

## Development Commands

### Build and Test
- `./gradlew build` - Build and test the project
- `./gradlew test` - Run unit tests only
- `./gradlew shadowJar` - Create deployable fat JAR (creates `build/libs/price-comparator.jar`)
- `./gradlew clean` - Clean build directory

### Running Tests
- `./gradlew test --tests "*ComparatorTest*"` - Run specific test class
- `./gradlew test --tests "*ProductLoaderTest.testLoadProducts*"` - Run specific test method

### Development Setup
Before running locally, copy and configure secrets:
```bash
cp src/main/resources/secrets.yaml.template src/main/resources/secrets.yaml
```
Then edit `secrets.yaml` with actual Telegram bot credentials.

## Architecture

### Core Components
- **Main.kt**: Entry point that orchestrates the price comparison workflow
- **PriceScraper**: Manages concurrent web scraping across multiple webshops using Kotlin coroutines
- **Comparator**: Analyzes scraped prices and generates comparison results
- **TelegramNotifier**: Sends formatted results to Telegram chat
- **ProductLoader**: Loads product configurations from YAML files

### Data Flow
1. Products loaded from `products.yaml` configuration
2. Each product scraped concurrently across all configured webshops
3. Results compared against original prices
4. Notifications sent to Telegram with price differences and failed scrapes

### Web Scraping Architecture
Each webshop has its own parser implementing the `PriceParser` interface:
- **BolPriceParser**, **BabyParkPriceParser**, **BabyDumpPriceParser**, etc.
- All parsers use JSoup for HTML parsing
- Error handling with Arrow's `Either` type for functional error management
- Concurrent scraping with coroutines and proper timeout handling

### Configuration
- **products.yaml**: Defines products to track with original prices and webshop URLs
- **secrets.yaml**: Telegram bot credentials (not tracked in git)
- Product URLs are mapped to specific webshops based on domain patterns

### Dependencies
- **Kotlin Coroutines**: For concurrent web scraping
- **Arrow**: Functional programming utilities for error handling
- **JSoup**: HTML parsing for price extraction
- **SnakeYAML**: Configuration file parsing
- **Telegram Bots**: Notification delivery

## Testing Strategy
- Unit tests for each price parser with mock HTML responses
- Integration tests for ProductLoader with test YAML files
- All parsers tested against real webshop HTML structures
- Test resources in `src/test/resources/`

## Key Files to Understand
- `src/main/kotlin/Main.kt`: Application entry point and orchestration
- `src/main/kotlin/scraping/PriceScraper.kt`: Core scraping logic with concurrent execution
- `src/main/kotlin/products/ProductLoader.kt`: Configuration loading with classpath/filesystem fallback
- `src/main/resources/products.yaml`: Product configuration (modify this to track new products)
- `build.gradle.kts`: Build configuration with Shadow plugin for fat JAR creation