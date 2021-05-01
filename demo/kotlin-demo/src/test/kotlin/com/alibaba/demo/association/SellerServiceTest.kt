package com.alibaba.demo.association

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class SellerServiceTest {

    private val sellerService = SellerService()

    @Test
    fun should_sell_sandwich() {
        Assertions.assertEquals("Fake-Sandwich-Cooker & Faked-Sandwich", sellerService.sellSandwich())
    }

    @Test
    fun should_sell_hamburger() {
        Assertions.assertEquals("Real-Hamburger-Cooker & Real-Hamburger", sellerService.sellHamburger())
    }
}
