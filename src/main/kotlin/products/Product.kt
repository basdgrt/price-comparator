package com.github.basdgrt.products

// TODO add some validations. Every product should have at least a BabyPark url
// TODO load products from a .yaml file
data class Product(
    val name: String,
    val productDetailPages: List<ProductDetailPage>,
    val originalPrice: Price
)

data class ProductDetailPage(
    val url: String,
    val webshop: Webshop = determineWebshop(url)
)

// TODO refactor this so it becomes a method of the Webshop enum
/**
 * Determines the webshop based on the URL.
 */
private fun determineWebshop(url: String): Webshop {
    return when {
        url.contains("bol.com") -> Webshop.BOL
        url.contains("babypark") -> Webshop.BABY_PARK
        url.contains("vanastenbabysuperstore") -> Webshop.VAN_ASTEN
        url.contains("maxi-cosi.nl") -> Webshop.MAXI_COSI
        url.contains("prenatal.nl") -> Webshop.PRENATAL
        url.contains("little-dutch.com") -> Webshop.LITTLE_DUTCH
        else -> throw IllegalArgumentException("Unsupported webshop URL: $url")
    }
}
