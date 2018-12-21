package com.example.lock.zk.exclusive;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 *
 * @author Administrator
 */
public class DistributedLock implements Lock,Watcher{
    private ZooKeeper zk;
    /**
     * zk node root path name
     */
    private String root = "/locks";
    /**
     * zk create node name lockName
     * 竞争资源的标志
     */
    private String lockName;
    /**
     * wait last lock
     * 等待前一个锁
     */
    private String waitNode;
    /**
     * current lock node
     * 当前锁
     */
    private String myZnode;
    /**
     * counter juc package
     * 计数器
     */
    private CountDownLatch latch;
    /**
     * request max latch number
     * CountDownLatch是一个同步工具类，它允许一个或多个线程一直等待，
     * 直到其他线程的操作执行完后再执行。在Java并发中，countdownlatch的概念是一个常见的面试题，所以一定要确保你很好的理解了它
     * @  www.importnew.com/15731.html 了解一下
     * CountDownLatch是在java1.5被引入的，跟它一起被引入的并发工具类还有CyclicBarrier、Semaphore、ConcurrentHashMap和BlockingQueue，
     * 它们都存在于java.util.concurrent包下。CountDownLatch这个类能够使一个线程等待其他线程完成各自的工作后再执行。
     * 例如，应用程序的主线程希望在负责启动框架服务的线程已经启动所有的框架服务之后再执行。
     * CountDownLatch是通过一个计数器来实现的，计数器的初始值为线程的数量。每当一个线程完成了自己的任务后，计数器的值就会减1。
     * 当计数器值到达0时，它表示所有的线程已经完成了任务，然后在闭锁上等待的线程就可以恢复执行任务。
     */
    private CountDownLatch connectedSignal=new CountDownLatch(100);
    /**
     * zk max sessionTimeOut
     */
    private int sessionTimeout = 30000;
    /**
     * 创建分布式锁,使用前请确认，config配置的zookeeper服务可用
     * @param config localhost:2181
     * @param lockName 竞争资源标志,lockName中不能包含单词_lock_
     */
    public DistributedLock(String config, String lockName){
        this.lockName = lockName;
        // 创建一个与服务器的连接
        try {
            zk = new ZooKeeper(config, sessionTimeout, this);
            //当number =0 的时候 所有请求全部释放 否则阻塞
            //此去不执行 Watcher
            Stat stat = zk.exists(root, false);
            if(stat == null){
                /**
                 * 创建主节点 主节点不能为临时节点 否则不能创建子节点
                 */
                zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            throw new LockException(e);
        } catch (KeeperException e) {
            throw new LockException(e);
        } catch (InterruptedException e) {
            throw new LockException(e);
        }
    }
    /**
     * zookeeper节点的监视器
     */
    @Override
    public void process(WatchedEvent event) {
        //建立连接用
        if(event.getState()== Watcher.Event.KeeperState.SyncConnected){
            //线程调用完成之后 调用此方法 number-1
            connectedSignal.countDown();
            return;
        }
        //其他线程放弃锁的标志
        if(this.latch != null) {
            this.latch.countDown();
        }
    }

    public void waitFor(){
        try {
            connectedSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void lock() {
        try {
            if(this.tryLock()){
                System.out.println("Thread " + Thread.currentThread().getId() + " " +myZnode + " get lock true");
                return;
            }
            else{
                //等待锁
                waitForLock(waitNode, sessionTimeout);
            }
        } catch (KeeperException e) {
            throw new LockException(e);
        } catch (InterruptedException e) {
            throw new LockException(e);
        }
    }

    /**
     * try lock
     * @return
     */
    @Override
    public boolean tryLock() {
        try {
            String splitStr = "zk_node";
            //LockName不能和split分割的字符串一样
            if(lockName.contains(splitStr))
                throw new LockException("lockName can not contains \\u000B");
            //创建临时子节点
            myZnode = zk.create(root + "/" + lockName +splitStr, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(myZnode + " 节点已经创建成功！");
            //取出所有子节点
            List<String> subNodes = zk.getChildren(root, false);
            List<String> lockObjNodes = new ArrayList<String>();
            for (String node : subNodes) {
                String _node = node.split(splitStr)[0];
                if(_node.equals(lockName)){
                    lockObjNodes.add(node);
                }
            }
            Collections.sort(lockObjNodes);

            if(myZnode.equals(root+"/"+lockObjNodes.get(0))){
                //如果是最小的节点,则表示取得锁
                System.out.println(myZnode + "==" + lockObjNodes.get(0));
                return true;
            }
            //如果不是最小的节点，找到比自己小1的节点
            String subMyZnode = myZnode.substring(myZnode.lastIndexOf("/") + 1);
            //找到前一个子节点
            waitNode = lockObjNodes.get(Collections.binarySearch(lockObjNodes, subMyZnode) - 1);
        } catch (KeeperException e) {
            throw new LockException(e);
        } catch (InterruptedException e) {
            throw new LockException(e);
        }
        return false;
    }

    /**
     * try lock method
     * @return
     */
    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        try {
            if(this.tryLock()){
                return true;
            }
            return waitForLock(waitNode,time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 等待监听
     * @param lower
     * @param waitTime
     */
    private boolean waitForLock(String lower, long waitTime) throws InterruptedException, KeeperException {
        //同时注册监听。
        Stat stat = zk.exists(root + "/" + lower,true);
        //判断比自己小一个数的节点是否存在,如果不存在则无需等待锁,同时注册监听
        if(stat != null){
            System.out.println("Thread " + Thread.currentThread().getId() + " waiting for " + root + "/" + lower);
            this.latch = new CountDownLatch(1);
            //等待，这里应该一直等待其他线程释放锁
            this.latch.await(waitTime, TimeUnit.MILLISECONDS);
            this.latch = null;
        }
        return true;
    }
    @Override
    public void unlock() {
        try {
            System.out.println("unlock " + myZnode);
            zk.delete(myZnode,-1);
            myZnode = null;
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.lock();
    }


    @Override
    public Condition newCondition() {
        return null;
    }

    public class LockException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public LockException(String e){
            super(e);
        }
        public LockException(Exception e){
            super(e);
        }
    }

}
