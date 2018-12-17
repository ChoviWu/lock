package com.example.lock.zk.exclusive;

import com.example.lock.zk.Lock;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author ChoviWu
 * @date 2018/12/17
 * Description : 打开zkServer zkCli  create /root
 *
 */
public class ZkNodeLock implements Lock, Watcher {

    private static final String PATH = "/root";

    private static final int TIMEOUT = 30 * 1000;

    private static final String ADDRESS = "localhost:2181";

    private ZooKeeper zooKeeper;

    private static final String LOCKNAME = "locks";

    private String myZnode;

    private static final CountDownLatch COUNTDOWNLATCH = new CountDownLatch(100);

    private static CountDownLatch await;

    private static String waitNode;

    public ZkNodeLock() {
        this.init();
    }

    private void init() {
        try {
            try {
                //创建一个zk链接对象，并且启动监听watcher
                zooKeeper = new ZooKeeper(ADDRESS, TIMEOUT, this);
                //查看zk下是否有锁的根节点
                final Stat stat = zooKeeper.exists(PATH, false);
                if (Objects.isNull(stat)) {
                    /**
                     * 如果root节点不存在 创建一个root节点，root节点不能为临时节点 否则报错
                     */
                    zooKeeper.create(PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 链接成功 watcher监听回调
     * @param watchedEvent
     */
    @Override
    public void process(final WatchedEvent watchedEvent) {
        //如果连接上了 计数-1
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            COUNTDOWNLATCH.countDown();
        }

    }

    /**
     * 获取锁
     * @return
     */
    @Override
    public boolean getLock() {
        if (Objects.isNull(LOCKNAME)) {
            return false;
        }
        try {
            /**
             * 如果当前节点下的没有锁占用 即占用资源
             */
            final String splitStr = "zk_node";
            if (LOCKNAME.equals(splitStr))
                throw new RuntimeException("");
            //创建一个节点
            myZnode = zooKeeper.create(PATH + "/" + LOCKNAME + splitStr, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(myZnode + "节点创建成功....");
            //获取该路径下的节点
            final List<String> list = zooKeeper.getChildren(PATH, false);
            final List<String> sort = new LinkedList();
            //对子节点进行排序
            for (final String str : list) {
                String _node = str.split(splitStr)[0];
                //匹配出已经抢到资源的节点
                if (_node.equals(LOCKNAME)) {
                    sort.add(str);
                }
            }
            //然后排序
            Collections.sort(sort);
            //匹配出是否跟最小的节点路径相同
            if (myZnode.equals(PATH + "/" + sort.get(0))) {
                System.out.println("获得锁成功！");
                return true;
            }

            //如果不是最小的节点，找到比自己小1的节点
            final String subMyZnode = myZnode.substring(myZnode.lastIndexOf("/") + 1);
            //找到前一个子节点
            waitNode = sort.get(Collections.binarySearch(sort, subMyZnode) - 1);
            //其他线程countDown之后 当前线程解锁
        } catch (KeeperException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 当前的线程未抢到资源时，将等待
     * 并监听根节点是否被删除
     * @param node
     * @param sessionTime
     * @return
     */
    @Override
    public boolean waitLock(final String node, final int sessionTime) {
        try {
            //等待锁 并注册监听，一旦共享资源释放，将立即去抢资源 watch true
            final Stat stat = zooKeeper.exists(PATH + "/" + node, true);
            if (!Objects.isNull(stat)) {
                //不为空将等待释放 并且监听
                System.out.println("等待资源被释放之后去抢资源.." + node);
                await = new CountDownLatch(1);
                await.await(sessionTime, TimeUnit.MILLISECONDS);
                //完成之后 置空 并去抢资源
                await = null;
            }
        } catch (KeeperException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean unlock() {

        try {
            System.out.println("关闭.." +myZnode);
            zooKeeper.delete(myZnode, -1);
            myZnode = null;
            zooKeeper.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void lock() {
        if (this.getLock()) {
            System.out.println("Thread " + Thread.currentThread().getId() + " " + myZnode + " 拿到锁");
            return;
        } else {
            waitLock(waitNode, TIMEOUT);
        }
    }
}
