package com.example.lock.concurrent;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchTest {

    /**
     * 100个线程 先走 主线程等待
     */
    private static CountDownLatch countDownLatch = new CountDownLatch(100);

    public static void main(String[] args) {

        try {
                for (int i=0;i<100;i++){
                    new Thread(()->{
                        System.out.println(Thread.currentThread().getId());
                    }).start();
                }
            //主线程必须在启动其他线程后立即调用CountDownLatch.await()方法。
            // 这样主线程的操作就会在这个方法上阻塞，直到其他线程完成各自的任务。
            countDownLatch.await();
            System.out.println(Thread.currentThread().getName());
            countDownLatch = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void print(int i){
        System.out.println(i);
        countDownLatch.countDown();
    }


}
