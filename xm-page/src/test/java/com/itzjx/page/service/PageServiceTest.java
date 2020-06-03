package com.itzjx.page.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

//@RunWith(SpringRunner.class)
//@SpringBootTest
class PageServiceTest {

    @Autowired
    private PageService pageService;

//    @Test
//    void createHtml() {
//        pageService.createHtml(142L);
//    }

    private static Object lock = new Object();
    //    private static Condition A = lock.newCondition();
//    private static Condition B = lock.newCondition();
//    private static Condition C = lock.newCondition();
    private static volatile Integer order = 1;
    private static AtomicInteger num = null;

    /**
     * 用于判断线程一是否执行，倒计时设置为1，执行后减1
     */
    private static CountDownLatch c1 = new CountDownLatch(1);

    /**
     * 用于判断线程二是否执行，倒计时设置为1，执行后减1
     */
    private static CountDownLatch c2 = new CountDownLatch(1);
    /**
     * 用于判断线程3是否执行，倒计时设置为1，执行后减1
     */
    private static CountDownLatch c3 = new CountDownLatch(1);


//    @Test
//    public void test() {
//
//        Thread thread1 = new Thread() {
//            @Override
//            public void run() {
//                while (num.get() > 0) {
//                    lock.lock();
//                    if (order != 1){
//                        try {
//                            C.await();
//                            if (num.get() ==  0) break;
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    System.out.print("a");
//                    A.signal();
//                    order++;
//                    lock.unlock();
//                }
//            }
//        };
//        Thread thread2 = new Thread() {
//            @Override
//            public void run() {
//                while (num.get() > 0 ){
//                    lock.lock();
//                    if (order != 2){
//                        try {
//                            A.await();
//                            if (num.get() ==  0) break;
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    System.out.print("l");
//                    B.signal();
//                    order++;
//                    lock.unlock();
//                }
//            }
//        };
//        Thread thread3 = new Thread() {
//            @Override
//            public void run() {
//                while (num.get() > 0) {
//                    lock.lock();
//                    if (order != 3){
//                        try {
//                            B.await();
//                            if (num.get() ==  0) break;
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    System.out.print("i");
//                    order = 1;
//                    C.signal();
//                    num.decrementAndGet();
//                    lock.unlock();
//
//                }
//            }
//        };
//        thread1.start();
//        thread2.start();
//        thread3.start();
//
//    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        num = new AtomicInteger(in.nextInt());

        Thread thread1 = new Thread() {
            @Override
            public void run() {
                while (num.get() > 0) {
                    try {
                        //等待c3倒计时，计时为0则往下运行
                        c3.await();
                        if (num.get() > 0) System.out.println("a");
                        //对c2倒计时-1
                        c1.countDown();
                        c3 = new CountDownLatch(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread2 = new Thread() {
            @Override
            public void run() {
                while (num.get() > 0) {
                    try {
                        //等待c3倒计时，计时为0则往下运行
                        c1.await();
                        if (num.get() > 0) System.out.println("l");
                        //对c2倒计时-1
                        c2.countDown();
                        c1 = new CountDownLatch(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        Thread thread3 = new Thread() {
            @Override
            public void run() {
                while (num.get() > 0) {
                    try {
                        //等待c3倒计时，计时为0则往下运行
                        c2.await();
                        if (num.get() > 0) System.out.println("i");
                        num.decrementAndGet();
                        //对c2倒计时-1
                        c3.countDown();
                        c2 = new CountDownLatch(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        thread1.start();
        thread2.start();
        thread3.start();
        c3.countDown();

    }


}