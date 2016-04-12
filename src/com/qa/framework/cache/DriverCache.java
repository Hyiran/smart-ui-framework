package com.qa.framework.cache;

import org.openqa.selenium.WebDriver;

/**
 * Created by kcgw001 on 2016/2/2.
 */
public class DriverCache {
    private static ThreadLocal<WebDriver> DriverCache = new ThreadLocal<WebDriver>();

    public static void set(WebDriver driver) {
        DriverCache.set(driver);
    }

    public static WebDriver get() {
        return DriverCache.get();
    }
}