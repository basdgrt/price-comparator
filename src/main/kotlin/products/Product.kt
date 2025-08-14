package com.github.basdgrt.products

data class Product(
    val name: String,
    val productDetailPages: List<ProductDetailPage>,
    val originalPrice: Price
)

data class ProductDetailPage(
    val url: String,
    val webshop: Webshop = Webshop.fromURL(url)
)
