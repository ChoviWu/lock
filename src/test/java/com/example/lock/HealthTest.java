package com.example.lock;

import com.example.lock.concurrent.countdown.ApplicationStartupUtil;
import org.junit.Test;

/**
 * @author ChoviWu
 * @date 2018/12/17
 * Description :
 */
public class HealthTest {

    @Test
    public void test1() {
        boolean result = false;
        try {
            result = ApplicationStartupUtil.checkExternalServices();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("External services validation completed !! Result was :: " + result);
    }
}
