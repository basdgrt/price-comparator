package com.github.basdgrt.scraping

data class Product(
    val name: String,
    val url: String,
    val webshop: Webshop = determineWebshop(url)
)

/**
 * Determines the webshop based on the URL.
 */
private fun determineWebshop(url: String): Webshop {
    return when {
        url.contains("bol.com") -> Webshop.BOL
        url.contains("babypark") -> Webshop.BABYPARK
        else -> throw IllegalArgumentException("Unsupported webshop URL: $url")
    }
}
