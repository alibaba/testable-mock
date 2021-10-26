package com.alibaba.demo.association;

/**
 * 被测类，会访问`CookerService`里的方法
 * Class to be tested, which will access methods in TargetService class
 */
public class SellerService {

    private CookerService cookerService = new CookerService();

    public String sellSandwich() {
        return cookerService.prepareSandwich();
    }

    public String sellHamburger() {
        return cookerService.prepareHamburger();
    }
}
