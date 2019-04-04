package org.aion.parallel;

import legacy_examples.foresttest.A;
import org.aion.avm.core.util.Helpers;
import org.aion.types.Address;
import org.junit.Test;

public class AddressResourceMonitorTest {
    
    byte[] addr1 = Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111");
    byte[] addr2 = Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222");
    byte[] addr3 = Helpers.hexStringToBytes("3333333333333333333333333333333333333333333333333333333333333333");
    byte[] addr4 = Helpers.hexStringToBytes("4444444444444444444444444444444444444444444444444444444444444444");

    class TestThread extends Thread{
        AddressResourceMonitor monitor;

        public TestThread(AddressResourceMonitor monitor){
            this.monitor = monitor;
        }
    }

    class TestThread1 extends TestThread{

        public TestThread1(AddressResourceMonitor monitor){
            super(monitor);
        }

        public void run(){
            TransactionTask task = new TransactionTask(null, null, 0, Address.ZERO_ADDRESS());

            monitor.acquire(addr1, task);
            monitor.testReleaseResourcesForTask(task);
        }
    }

    class TestThread2 extends TestThread{
        public TestThread2(AddressResourceMonitor monitor){
            super(monitor);
        }
        
        public void run(){
            TransactionTask task = new TransactionTask(null, null, 1, Address.ZERO_ADDRESS());
            monitor.acquire(addr1, task);

            while(false == task.inAbortState()){
            }

            System.out.println("Thread 2 Abort");
            monitor.testReleaseResourcesForTask(task);
        }
    }

    class TestThread3 extends TestThread{
        public TestThread3(AddressResourceMonitor monitor){
            super(monitor);
        }
        
        public void run(){
            TransactionTask task = new TransactionTask(null, null, 2, Address.ZERO_ADDRESS());
            monitor.acquire(addr1, task);

            while(false == task.inAbortState()){

            }

            System.out.println("Thread 3 Abort");
            monitor.testReleaseResourcesForTask(task);
        }
    }


    @Test
    public void testBasicContention() throws InterruptedException{
        AddressResourceMonitor monitor = new AddressResourceMonitor();

        TestThread t1 = new TestThread1(monitor);
        TestThread t2 = new TestThread2(monitor);
        TestThread t3 = new TestThread3(monitor);

        t3.start();
        Thread.sleep(100);
        t2.start();
        Thread.sleep(100);
        t1.start();

        t1.join();
        t2.join();
        t3.join();
    }

    class TestThread4 extends TestThread{
        public TestThread4(AddressResourceMonitor monitor){
            super(monitor);
        }
        
        public void run(){
            TransactionTask task = new TransactionTask(null, null, 0, Address.ZERO_ADDRESS());

            monitor.acquire(addr1, task);
            monitor.acquire(addr2, task);
            monitor.acquire(addr3, task);
            monitor.acquire(addr4, task);
            monitor.testReleaseResourcesForTask(task);
        }
    }

    class TestThread5 extends TestThread{
        public TestThread5(AddressResourceMonitor monitor){
            super(monitor);
        }
        
        public void run(){
            TransactionTask task = new TransactionTask(null, null, 1, Address.ZERO_ADDRESS());
            monitor.acquire(addr3, task);

            while(false == task.inAbortState()){
            }

            System.out.println("Thread 5 Abort");
            monitor.testReleaseResourcesForTask(task);
        }
    }

    class TestThread6 extends TestThread{
        public TestThread6(AddressResourceMonitor monitor){
            super(monitor);
        }
        
        public void run(){
            TransactionTask task = new TransactionTask(null, null, 2, Address.ZERO_ADDRESS());
            monitor.acquire(addr4, task);

            while(false == task.inAbortState()){
            }

            System.out.println("Thread 6 Abort");
            monitor.testReleaseResourcesForTask(task);
        }
    }

    @Test
    public void testMultiContention() throws InterruptedException{

        AddressResourceMonitor monitor = new AddressResourceMonitor();

        TestThread4 t1 = new TestThread4(monitor);
        TestThread5 t2 = new TestThread5(monitor);
        TestThread6 t3 = new TestThread6(monitor);

        t3.start();
        Thread.sleep(100);
        t2.start();
        Thread.sleep(100);
        t1.start();

        t1.join();
        t2.join();
        t3.join();
    }

    class TestThread7 extends TestThread{
        public TestThread7(AddressResourceMonitor monitor){
            super(monitor);
        }
        
        public void run(){
            TransactionTask task = new TransactionTask(null, null, 0, Address.ZERO_ADDRESS());

            monitor.acquire(addr1, task);
            monitor.acquire(addr2, task);
            monitor.acquire(addr3, task);
            monitor.acquire(addr4, task);
            monitor.testReleaseResourcesForTask(task);
        }
    }

    class TestThread8 extends TestThread{
        public TestThread8(AddressResourceMonitor monitor){
            super(monitor);
        }

        public void run(){
            TransactionTask task = new TransactionTask(null, null, 1, Address.ZERO_ADDRESS());
            monitor.acquire(addr4, task);
            monitor.acquire(addr3, task);
            monitor.acquire(addr2, task);
            monitor.acquire(addr1, task);

            while(false == task.inAbortState()){
            }

            System.out.println("Thread 8 Abort");
            monitor.testReleaseResourcesForTask(task);
        }
    }

    @Test
    public void testNestedContention() throws InterruptedException{
        AddressResourceMonitor monitor = new AddressResourceMonitor();

        TestThread7 t1 = new TestThread7(monitor);
        TestThread8 t2 = new TestThread8(monitor);

        t2.start();
        Thread.sleep(100);
        t1.start();

        t1.join();
        t2.join();
    }

}
