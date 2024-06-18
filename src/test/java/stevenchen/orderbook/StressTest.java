package stevenchen.orderbook;

import stevenchen.orderbook.model.Order;

import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class StressTest {
    private static final Logger LOGGER = Logger.getLogger(StressTest.class.getName());

    public static void main(String[] args) throws InterruptedException {
        OrderBook orderBook = new OrderBook();
        for (int i = 0; i < 1000000; i++) {
            double price = Math.round(Math.random() * 10000) / 100.0;
            char side = Math.random() > 0.5 ? 'B' : 'O';
            long size = (long) (Math.random() * 1000);
            orderBook.addOrder(new Order(i, price, side, size));
        }

        Thread addThread = new Thread(() -> {
            for (int i = 0; i < 1000000; i++) {
                double price = Math.round(Math.random() * 10000) / 100.0;
                char side = Math.random() > 0.5 ? 'B' : 'O';
                long size = (long) (Math.random() * 1000);
                orderBook.addOrder(new Order(i + 1000000, price, side, size));
                if(i % 10000 == 0) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Thread removeModifyThread = new Thread(() -> {
            for (int i = 0; i < 1000000; i++) {
                if(Math.random() > 0.6) {
                    orderBook.removeOrder(i);
                }
                else {
                    long size = (long) (Math.random() * 1000);
                    orderBook.modifyOrder(i, size);
                }
                if(i % 10000 == 0) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
        Thread processThread = new Thread(() -> {
            // This is a continuous process that will run until the program is terminated.
            // This will cause the program to run indefinitely even after other threads have completed.
            try {
                orderBook.continuousProcessOrderActions();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread logThread = new Thread(() -> {
            for(int i = 0; i < 100; i++) {
                LOGGER.info("Best bid price: " + orderBook.getLevelPrice('B', 0) + ", with size " + orderBook.getLevelTotalSize('B', 0));
                LOGGER.info("Best offer price: " + orderBook.getLevelPrice('O', 0) + ", with size " + orderBook.getLevelTotalSize('O', 0));
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        processThread.start();
        addThread.start();
        removeModifyThread.start();
        logThread.start();
    }
}