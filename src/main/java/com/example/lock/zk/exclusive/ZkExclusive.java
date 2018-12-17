package com.example.lock.zk.exclusive;

import com.example.lock.zk.AbstractLock;
import org.I0Itec.zkclient.ZkLock;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class ZkExclusive extends AbstractLock {


    private static final String ZK_ADDRESS = "localhost:2181";
    private static final String ZK_LOCK_PATH = "/zktest/lock0";

    /**
     * 100 request
     */
    private static CountDownLatch countDownLatch = new CountDownLatch(100);


    public ZkExclusive() {
        try {
            this.zooKeeper = new ZooKeeper("127.0.0.1:2181", 30000,
                event -> {
                    System.out.println("Path : "+event.getPath()+ "...."
                    +"State:  " + event.getState() + "   Type: " + event.getType()
                    + "Wrapper : " +event.getWrapper());
                    if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
                        countDownLatch.countDown();
                    }
                });
        } catch (IOException e) {
            System.out.println("链接失败。。。。。。。。。");
            e.printStackTrace();
            System.exit(-1);
        }
        //CountDown
        try {
            System.out.println("开始并发请求集合");
            //并发工具类  如果number不为0  就需要等待，此时会进入阻塞状态
            // 当number减到0的时候，就会释放一组线程并执行逻辑
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void dealLogic() {

    }

    @Override
    public void lock() {

    }
}
