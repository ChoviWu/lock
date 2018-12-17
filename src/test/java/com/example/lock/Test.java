package com.example.lock;

import com.example.lock.zk.NumberGenerator;
import com.example.lock.zk.exclusive.DistributedLock;
import com.example.lock.zk.exclusive.ZkExclusive;

public class Test {


    public static void main(String[] args) {
        int i = 1;
        do {
//            DistributedLock lock = new DistributedLock("127.0.0.1","haha");
//                try {
//                    lock.lock();
//                    System.out.println(NumberGenerator.getNumber());
//                }finally {
//                    if(lock != null){
//                        lock.unlock();
//
//                    }
//                }
//            i++;
            DistributedLock lock = new DistributedLock("127.0.0.1","haha");

                new Thread(()->{
                    try {
                        lock.lock();
                        System.out.println(NumberGenerator.getNumber());
                }finally {
                    if(lock != null){
                        lock.unlock();
                    }
                }
                }).start();
//
            i++;
        }while(i<100);

    }
}

