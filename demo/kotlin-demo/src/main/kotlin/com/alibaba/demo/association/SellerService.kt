package com.alibaba.demo.association

/**
 * 被测类，会访问`CookerService`里的方法
 * Class to be tested, which will access methods in TargetService class
 */
class SellerService {

    private val cookerService = CookerService()

    fun sellSandwich(): String {
        return cookerService.prepareSandwich()
    }

    fun sellHamburger(): String {
        return cookerService.prepareHamburger()
    }
}
