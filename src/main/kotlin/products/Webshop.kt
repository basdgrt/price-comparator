package com.github.basdgrt.products

enum class Webshop(val url: String) {
    BOL("bol.com"),
    BABY_PARK("babypark.nl"),
    BABY_DUMP("baby-dump.nl"),
    VAN_ASTEN("vanastenbabysuperstore.nl"),
    MAXI_COSI("maxi-cosi.nl"),
    PRENATAL("prenatal.nl"),
    LITTLE_DUTCH("little-dutch.com"),
    COOLBLUE("coolblue.nl");

    companion object {
        fun fromURL(url: String): Webshop = entries.single() { url.contains(it.url) }
    }

    override fun toString(): String = url
}
