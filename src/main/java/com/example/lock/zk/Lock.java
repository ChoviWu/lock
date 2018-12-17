package com.example.lock.zk;

/**
 * Lock
 * @author Administrator
 */
public interface Lock {

    /**
     * is get Lock  return boolean
     * @return
     */
    boolean getLock();

    /**
     * wait lock return getLock()
     */
    boolean waitLock(String node,int sessionTime);


    /**
     * unlock return
     */
    boolean unlock();

    /**
     * overWrite the lock methods
     */
    void lock();
}
