package com.milagros.stockwise

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform