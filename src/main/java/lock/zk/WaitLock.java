package com.example.lock.zk;

/**
 * @author Administrator
 */
public interface WaitLock {

    boolean waitLock(String node,int timeOut);

}
