package com.example.lock.zk;

import com.example.lock.zk.exclusive.DistributedLock;
import com.example.lock.zk.exclusive.ZkExclusive;

import java.util.concurrent.locks.Lock;

public final class NumberGenerator {


    public static long getNumber() {

        long number = System.currentTimeMillis();
        return number;

    }
}
