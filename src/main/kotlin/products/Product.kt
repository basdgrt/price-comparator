package com.github.basdgrt.products

val products: List<Product> = listOf(
    Product(
        name = "Maxi-Cosi FamilyFix 360",
        productDetailPages = listOf(
            ProductDetailPage(url = "https://www.bol.com/nl/nl/p/maxi-cosi-familyfix-360-i-size-isofix-autostoel-base-essential-grey/9300000074243531/?bltgh=oFC61UoF9XGrD3D6jvlDPw.4_8.9.ProductTitle"),
            ProductDetailPage(url = "https://www.babypark.nl/maxi-cosi-familyfix-360.html"),
            ProductDetailPage(url = "https://www.vanastenbabysuperstore.nl/isofixbase-maxi-cosi-familyfix-360"),
        ),
        originalPrice = Price.of("169.00").getOrNull()!!
    )
)

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
        else -> throw IllegalArgumentException("Unsupported webshop URL: $url")
    }
}