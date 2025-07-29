package com.github.basdgrt.products

import arrow.core.Either
import com.github.basdgrt.scraping.ParseFailure

data class ProductPrices(val product: Product, val webshopPrices: List<WebshopPrice>)

data class WebshopPrice(val webshop: Webshop, val price: Either<ParseFailure, Price>)