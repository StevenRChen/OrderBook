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
            }
        });
        Thread processThread = new Thread(() -> {
            orderBook.processOrderActions();
            LOGGER.info("Best bid price: " + orderBook.getLevelPrice('B', 0) + ", with size " + orderBook.getLevelTotalSize('B', 0));
            LOGGER.info("Best offer price: " + orderBook.getLevelPrice('O', 0) + ", with size " + orderBook.getLevelTotalSize('O', 0));
        });
        processThread.start();
        addThread.start();
        removeModifyThread.start();

    }
}
