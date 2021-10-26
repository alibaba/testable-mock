package com.alibaba.demo.association;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SellerServiceTest {

    private SellerService sellerService = new SellerService();

    @Test
    void should_sell_sandwich() {
        assertEquals("Fake-Sandwich-Cooker & Faked-Sandwich", sellerService.sellSandwich());
    }

    @Test
    void should_sell_hamburger() {
        assertEquals("Real-Hamburger-Cooker & Real-Hamburger", sellerService.sellHamburger());
    }

}
