package com.example.lock;

import com.example.lock.zk.NumberGenerator;
import com.example.lock.zk.exclusive.ZkNodeLock;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ChoviWu
 * @date 2018/12/17
 * Description :
 */
public class ZkNodeLockTest {


    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i <= 100; i++) {
            //========================单线程调用========================
//            ZkNodeLock lock = new ZkNodeLock();
//            try {
//                lock.lock();
//                long key = NumberGenerator.getNumber();
////              System.out.println("生成的订单号："+NumberGenerator.getNumber());
//            }finally {
//                lock.unlock();
//            }
            //=============================多线程调用=============================
            ZkNodeLock lock = new ZkNodeLock();
            new Thread(() -> {
                try {
                    lock.lock();
                     NumberGenerator.getNumber();
                } finally {
                    lock.unlock();
                }
            }).start();
        }
        NumberGenerator.map.values().stream().forEach(c -> {
            System.out.println("生成的订单号：" + c);

        });

    }
}
