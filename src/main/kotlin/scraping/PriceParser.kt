package com.github.basdgrt.scraping

import org.jsoup.nodes.Document
import java.math.BigDecimal

interface PriceParser {

    fun parse(document: Document): BigDecimal?
}