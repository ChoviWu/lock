package com.example.lock.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public abstract class AbstractLock implements Lock {

    private static String PATH = "product-lock";

    protected ZooKeeper zooKeeper;

    @Override
    public boolean getLock() {
        try {
            System.out.println("开始加锁.....");
            String lock = zooKeeper.create(PATH, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("加锁失败重试ing....");
            waitLock();


        }
        return false;
    }

    @Override
    public void waitLock() {
        //计数器
        int count = 0;
        try {
            Thread.sleep(1000);
            if(!getLock()){
                System.out.println("占有锁成功 : " );
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            count++;
        }
    }

    @Override
    public boolean unlock() {
        try {
            zooKeeper.delete(PATH, -1);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public abstract void dealLogic();
}
