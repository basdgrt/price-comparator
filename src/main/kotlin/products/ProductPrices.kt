package com.github.basdgrt.products

import arrow.core.Either
import com.github.basdgrt.scraping.ScrapeFailure

data class ProductPrices(val product: Product, val webshopPrices: List<Either<ScrapeFailure, WebshopPrice>>)

data class WebshopPrice(val productDetailPage: ProductDetailPage, val price: Price)