package com.example.lock.zk;

public interface Lock {

    /**
     * is get Lock  return boolean
     * @return
     */
    boolean getLock();

    /**
     * wait lock return getLock()
     */
    void waitLock();


    /**
     * unlock return
     */
    boolean unlock();


}
