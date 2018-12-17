package com.example.lock.zk;

import com.example.lock.zk.exclusive.DistributedLock;
import com.example.lock.zk.exclusive.ZkExclusive;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

/**
 * @author Administrator
 */
public final class NumberGenerator {

    public static HashMap map = new HashMap();

    public static long getNumber() {

        long number = System.currentTimeMillis();
        map.put(number,number);
        return number;

    }
}
