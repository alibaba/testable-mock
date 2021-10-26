package com.alibaba.demo.association

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SellerServiceTest {

    private val sellerService = SellerService()

    @Test
    fun should_sell_sandwich() {
        assertEquals("Fake-Sandwich-Cooker & Faked-Sandwich", sellerService.sellSandwich())
    }

    @Test
    fun should_sell_hamburger() {
        assertEquals("Real-Hamburger-Cooker & Real-Hamburger", sellerService.sellHamburger())
    }
}
