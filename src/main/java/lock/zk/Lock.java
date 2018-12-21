package com.example.lock.zk;

public interface Lock extends WaitLock{

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
