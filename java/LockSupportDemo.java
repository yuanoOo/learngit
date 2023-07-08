package com.zz.threads;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

/**
 * 要求两个线程交替打印a和b，且都打印50次，且a必须先打印。
 * 实现两个线程之间的同步
 */
public class LockSupportDemo {

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[2];

        threads[0] = new Thread(() -> {
            int i = 51;
            while (i-- > 1) {
                System.out.printf("%s %d---> %s%n", Thread.currentThread().getName(), i, 'a');
                // 先释放b线程，然后阻塞a线程，否则a线程直接阻塞，无法向下执行
                LockSupport.unpark(threads[1]);
                LockSupport.park();
            }

        });

        threads[1] = new Thread(() -> {

            int i = 51;
            while (i-- > 1) {
                // 先阻塞次线程，防止此线程先打印出b
                LockSupport.park();
                System.out.printf("%s %d---> %s%n", Thread.currentThread().getName(), i, 'b');
                LockSupport.unpark(threads[0]);
            }
        });

        Arrays.stream(threads).forEach(Thread::start);
        Thread.currentThread().join(1_000L);
    }
}
