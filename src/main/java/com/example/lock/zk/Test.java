package com.example.lock.zk;

import com.example.lock.zk.exclusive.DistributedLock;
import com.example.lock.zk.exclusive.ZkExclusive;

import java.util.concurrent.locks.Lock;

public class Test {


    public static void main(String[] args) {
        int i = 1;
        do {
                DistributedLock lock = new DistributedLock("127.0.0.1","haha");
                try {
                    lock.lock();
                    System.out.println(NumberGenerator.getNumber());
                }finally {
                    if(lock != null){
                        lock.unlock();
                    }
                }
            i++;
//            DistributedLock lock = new DistributedLock("127.0.0.1","haha");
//            try {
//                lock.lock();
//                System.out.println(NumberGenerator.getNumber());
//            }finally {
//                if(lock != null){
//                    lock.unlock();
//                }
//            }
//            i++;
        }while(i<100);

    }
}


class ZkExcluse{

    private final static ZkExclusive zkExclusive = new ZkExclusive();

    private final static ZkExcluse zkExcluse = new ZkExcluse();

    public static ZkExcluse getInstance(){
        return zkExcluse;
    }

    public  ZkExclusive getZkExclusive(){
        return zkExclusive;
    }


}

class  Distribute{


    private final static DistributedLock lock = new DistributedLock("127.0.0.1","haha");

    private final static Distribute zkExcluse = new Distribute();

    public static Distribute getInstance(){
        return zkExcluse;
    }

    public  DistributedLock getZkExclusive(){
        return lock;
    }


}
